/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.util.StringUtils;

import com.mangosolutions.rcloud.sessionkeyauth.KeyServerConfiguration;
import com.mangosolutions.rcloud.sessionkeyauth.SessionKeyServerUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@EnableConfigurationProperties(SessionKeyServerProperties.class)
public class SessionKeyServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired
	private SessionKeyServerProperties keyserverProperties;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.addFilterBefore(ssoFilter(), RequestHeaderAuthenticationFilter.class)
			.authenticationProvider(preAuthAuthProvider())
			.csrf()
			.disable()
			.authorizeRequests()
			.anyRequest()
			.authenticated();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(preAuthAuthProvider());
	}
	
	@Bean 
	public AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> getSessionKeyServerUserDetailsService() {
		Collection<KeyServerConfiguration> config = this.keyserverProperties.getKeyservers();
		SessionKeyServerUserDetailsService service = new SessionKeyServerUserDetailsService(config);
		return service;
	}

	@Bean
	public PreAuthenticatedAuthenticationProvider preAuthAuthProvider() {
		PreAuthenticatedAuthenticationProvider preAuthAuthProvider = new PreAuthenticatedAuthenticationProvider();
		preAuthAuthProvider.setPreAuthenticatedUserDetailsService(getSessionKeyServerUserDetailsService());
		return preAuthAuthProvider;
	}

	@Bean
	public SessionKeyServerWebAuthenticationDetailsSource getDetailsSource() {
		return new SessionKeyServerWebAuthenticationDetailsSource(this.keyserverProperties.getClientId());
	}
	
	@Bean
	public AbstractPreAuthenticatedProcessingFilter ssoFilter() throws Exception {
		RequestParameterAuthenticationFilter filter = new RequestParameterAuthenticationFilter();
		filter.setAuthenticationManager(authenticationManager());
		filter.setAuthenticationDetailsSource(getDetailsSource());
		String tokenParameter = this.keyserverProperties.getToken();
		if(!StringUtils.isEmpty(tokenParameter)) {
			filter.setPrincipalRequestParameter(tokenParameter);
		}
		return filter;
	}

}
