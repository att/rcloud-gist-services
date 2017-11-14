/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.mangosolutions.rcloud.commons.spring.http.security.AnonymousUserAuthorityResolver;
import com.mangosolutions.rcloud.commons.spring.http.security.AuthorityResolver;
import com.mangosolutions.rcloud.commons.spring.http.security.GrantedAuthorityFactory;
import com.mangosolutions.rcloud.commons.spring.http.security.RequestParameterAuthenticationFilter;
import com.mangosolutions.rcloud.commons.spring.http.security.UserAuthorityResolver;
import com.mangosolutions.rcloud.rawgist.repository.git.CollaborationDataStore;
import com.mangosolutions.rcloud.rawgist.repository.security.CollaborationGrantedAuthorityResolver;
import com.mangosolutions.rcloud.sessionkeyauth.KeyServerConfiguration;
import com.mangosolutions.rcloud.sessionkeyauth.SessionKeyServerService;
import com.mangosolutions.rcloud.sessionkeyauth.SessionKeyServerUserDetailsService;
import com.mangosolutions.rcloud.sessionkeyauth.SessionKeyServerWebAuthenticationDetailsSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@EnableConfigurationProperties(SessionKeyServerProperties.class)
public class GistServiceSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String GIST_SERVICE_PATH = "/gists/**";
    private static final String USER_SERVICE_PATH = "/user/**";

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private SessionKeyServerProperties keyserverProperties;

    @Autowired
    private CollaborationDataStore collaborationDataStore;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        RequestMatcher matcher = new OrRequestMatcher(
                new AntPathRequestMatcher(GIST_SERVICE_PATH), new AntPathRequestMatcher(USER_SERVICE_PATH));

        http.requestMatchers().requestMatchers(matcher).and()
                .addFilterBefore(ssoFilter(matcher), RequestHeaderAuthenticationFilter.class)
                .authenticationProvider(preAuthAuthProvider()).csrf().disable().authorizeRequests().anyRequest()
                .authenticated();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(preAuthAuthProvider());
    }

    @Bean
    @RefreshScope
    public AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> getSessionKeyServerUserDetailsService() {
        SessionKeyServerService service = getSessionKeyServerService();
        SessionKeyServerUserDetailsService userDetailsService = new SessionKeyServerUserDetailsService(service);
        GrantedAuthorityFactory factory = getAuthorityFactory();
        userDetailsService.setGrantedAuthorityFactory(factory);
        return userDetailsService;
    }

    @Bean
    @RefreshScope
    public SessionKeyServerService getSessionKeyServerService() {
        Map<String, KeyServerConfiguration> config = new HashMap<>(this.keyserverProperties.getKeyservers());
        Map<String, KeyServerConfiguration> keyServers = new HashMap<>(config);
        logger.info("Configured key servers: {}", keyServers);
        config.clear();
        SessionKeyServerService service = new SessionKeyServerService(getSessionKeyServerTemplate(), keyServers);
        return service;
    }
    
    @Bean
    @RefreshScope
    public RestTemplate getSessionKeyServerTemplate() {
        return new RestTemplate();
    }
    

    @Bean
    public GrantedAuthorityFactory getAuthorityFactory() {
        GrantedAuthorityFactory factory = new GrantedAuthorityFactory();
        Collection<AuthorityResolver> authorityResolvers = getAuthorityResolvers();
        factory.setAuthorityResolvers(authorityResolvers);
        return factory;
    }

    @Bean
    public Collection<AuthorityResolver> getAuthorityResolvers() {

        return Arrays.asList(new AnonymousUserAuthorityResolver(), new UserAuthorityResolver(),
                new CollaborationGrantedAuthorityResolver(this.collaborationDataStore));
    }

    @Bean
    public PreAuthenticatedAuthenticationProvider preAuthAuthProvider() {
        PreAuthenticatedAuthenticationProvider preAuthAuthProvider = new PreAuthenticatedAuthenticationProvider();
        preAuthAuthProvider.setPreAuthenticatedUserDetailsService(getSessionKeyServerUserDetailsService());
        return preAuthAuthProvider;
    }

    @Bean
    public SessionKeyServerWebAuthenticationDetailsSource getDetailsSource() {
        return new SessionKeyServerWebAuthenticationDetailsSource(this.keyserverProperties.getClientIdParam());
    }

    // @Bean
    public AbstractPreAuthenticatedProcessingFilter ssoFilter(RequestMatcher matcher) throws Exception {
        RequestParameterAuthenticationFilter filter = new RequestParameterAuthenticationFilter();
        filter.setExceptionIfParameterMissing(false);
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationDetailsSource(getDetailsSource());
        String tokenParameter = this.keyserverProperties.getAccessTokenParam();
        if (!StringUtils.isEmpty(tokenParameter)) {
            filter.setPrincipalRequestParameter(tokenParameter);
        }
        return filter;
    }

}
