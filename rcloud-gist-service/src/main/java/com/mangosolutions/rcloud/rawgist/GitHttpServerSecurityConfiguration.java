/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.mangosolutions.rcloud.rawgist.http.GitServiceAuthenticationManager;
import com.mangosolutions.rcloud.rawgist.repository.git.CollaborationDataStore;
import com.mangosolutions.rcloud.sessionkeyauth.GrantedAuthorityFactory;
import com.mangosolutions.rcloud.sessionkeyauth.SessionKeyServerService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER - 10)
@EnableConfigurationProperties(SessionKeyServerProperties.class)
public class GitHttpServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String REPOSITORY_ANT_MATCH = "/" + GitHttpServerServletConfiguration.REPOSITORY_PATH + "/**"; 
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    @Autowired
    private GistServiceProperties gistServiceProperties;
    
    @Autowired
    private SessionKeyServerService sessionKeyServerService;
    
    @Autowired
    private GrantedAuthorityFactory grantedAuthorityFactory;

    @Autowired
    private SessionKeyServerProperties keyserverProperties;

    @Autowired
    private CollaborationDataStore collaborationDataStore;

    @Override
    protected void configure(HttpSecurity http) throws Exception {


        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        
//        String gitServerPath = getGitServerPath();
//
//        new AndRequestMatcher(
//                new AntPathRequestMatcher(REPOSITORY_ANT_MATCH),
//                new NegatedRequestMatcher(new HttpMethodRequestMatcher("GET"))
//            )
//        
        http
        .requestMatchers().requestMatchers(
            new AndRequestMatcher(
                new AntPathRequestMatcher(REPOSITORY_ANT_MATCH)
//                ,
//                new OrRequestMatcher(
//                    new HttpRequestParameterRequestMatcher("service", "git-receieve-pack"),
//                    new HttpMethodRequestMatcher("GET", "PUT", "POST", "PATCH", "DELETE")
//                )
            )
//                add in a request parameter request matcher
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
    
//    private String getGitServerPath() {
//        String gitServerPath = gistServiceProperties.getGitServerPath();
//        gitServerPath = StringUtils.startsWithIgnoreCase(gitServerPath, "/")? gitServerPath: "/" + gitServerPath;
//        gitServerPath = gitServerPath + "/**"; 
//        return gitServerPath; 
//    }
    

//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.authenticationProvider(preAuthAuthProvider());
//    }
//
//    @Bean
//    @RefreshScope
//    public AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> getSessionKeyServerUserDetailsService() {
//        SessionKeyServerService service = getSessionKeyServerService();
//        SessionKeyServerUserDetailsService userDetailsService = new SessionKeyServerUserDetailsService(service);
//        GrantedAuthorityFactory factory = getAuthorityFactory();
//        userDetailsService.setGrantedAuthorityFactory(factory);
//        return userDetailsService;
//    }
//
//    @Bean
//    @RefreshScope
//    public SessionKeyServerService getSessionKeyServerService() {
//        Map<String, KeyServerConfiguration> config = new HashMap<>(this.keyserverProperties.getKeyservers());
//        Map<String, KeyServerConfiguration> keyServers = new HashMap<>(config);
//        logger.info("Configured key servers: {}", keyServers);
//        config.clear();
//        SessionKeyServerService service = new SessionKeyServerService(keyServers);
//        return service;
//    }
//
//    @Bean
//    public GrantedAuthorityFactory getAuthorityFactory() {
//        GrantedAuthorityFactory factory = new GrantedAuthorityFactory();
//        Collection<AuthorityResolver> authorityResolvers = getAuthorityResolvers();
//        factory.setAuthorityResolvers(authorityResolvers);
//        return factory;
//    }
//
//    @Bean
//    public Collection<AuthorityResolver> getAuthorityResolvers() {
//
//        return Arrays.asList(new AnonymousUserAuthorityResolver(), new UserAuthorityResolver(),
//                new CollaborationGrantedAuthorityResolver(this.collaborationDataStore));
//    }
//
//    @Bean
//    public PreAuthenticatedAuthenticationProvider preAuthAuthProvider() {
//        PreAuthenticatedAuthenticationProvider preAuthAuthProvider = new PreAuthenticatedAuthenticationProvider();
//        preAuthAuthProvider.setPreAuthenticatedUserDetailsService(getSessionKeyServerUserDetailsService());
//        return preAuthAuthProvider;
//    }
//
//    @Bean
//    public SessionKeyServerWebAuthenticationDetailsSource getDetailsSource() {
//        return new SessionKeyServerWebAuthenticationDetailsSource(this.keyserverProperties.getClientIdParam());
//    }
//
//    @Bean
//    public AbstractPreAuthenticatedProcessingFilter ssoFilter() throws Exception {
//        RequestParameterAuthenticationFilter filter = new RequestParameterAuthenticationFilter();
//        filter.setExceptionIfParameterMissing(false);
//        filter.setAuthenticationManager(authenticationManager());
//        filter.setAuthenticationDetailsSource(getDetailsSource());
//        String tokenParameter = this.keyserverProperties.getAccessTokenParam();
//        if (!StringUtils.isEmpty(tokenParameter)) {
//            filter.setPrincipalRequestParameter(tokenParameter);
//        }
//        return filter;
//    }

}
