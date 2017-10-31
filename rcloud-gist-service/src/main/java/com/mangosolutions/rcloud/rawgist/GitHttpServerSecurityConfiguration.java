/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import com.mangosolutions.rcloud.rawgist.http.GitServiceAuthenticationManager;
import com.mangosolutions.rcloud.sessionkeyauth.GrantedAuthorityFactory;
import com.mangosolutions.rcloud.sessionkeyauth.SessionKeyServerService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER - 10)
@EnableConfigurationProperties(SessionKeyServerProperties.class)
public class GitHttpServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String REPOSITORY_ANT_MATCH = "/" + GitHttpServerServletConfiguration.REPOSITORY_PATH + "/**"; 
    
    private static final String GIT_RECEIVE_PACK = "git-receive-pack";
    
    private static final String GIT_RECEIEVE_PACK_PATH = "/**/" + GIT_RECEIVE_PACK;
    
    @Autowired
    private SessionKeyServerService sessionKeyServerService;
    
    @Autowired
    private GrantedAuthorityFactory grantedAuthorityFactory;

    @Override
    protected void configure(HttpSecurity http) throws Exception {


        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http
        .requestMatchers().requestMatchers(
            new AndRequestMatcher(
                new AntPathRequestMatcher(REPOSITORY_ANT_MATCH),
                new OrRequestMatcher(
                  new AndRequestMatcher(
                      new AntPathRequestMatcher(GIT_RECEIEVE_PACK_PATH),
                      new HttpMethodRequestMatcher("POST")
                  ),
                  new AndRequestMatcher(
                      new HttpRequestParameterRequestMatcher("service", GIT_RECEIVE_PACK),
                      new HttpMethodRequestMatcher("GET")
                  )
              )
            )
        )
        .and().addFilterBefore(getBasicAuthFilter(), RequestHeaderAuthenticationFilter.class)
        .csrf()
        .disable()
        .authorizeRequests()
        .anyRequest().authenticated()
        .and().httpBasic();
    }

    private Filter getBasicAuthFilter() {
        
        GitServiceAuthenticationManager manager = getGitServiceAuthenticationManager();
        
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter(manager);
        return filter;
    }
    
    @Bean
    public GitServiceAuthenticationManager getGitServiceAuthenticationManager() {
        return new GitServiceAuthenticationManager(this.sessionKeyServerService, grantedAuthorityFactory);
    }
    
}
