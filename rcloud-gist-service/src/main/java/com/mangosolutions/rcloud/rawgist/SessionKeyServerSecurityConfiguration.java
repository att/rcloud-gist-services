/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

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
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.util.StringUtils;

import com.mangosolutions.rcloud.sessionkeyauth.SessionKeyServerUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@EnableConfigurationProperties(SessionKeyServerProperties.class)
public class SessionKeyServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired
	private SessionKeyServerProperties keyserverProperties;

	
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.addFilterBefore(ssoFilter(), RequestHeaderAuthenticationFilter.class)
			.authenticationProvider(preauthAuthProvider())
			.csrf()
			.disable()
			.authorizeRequests()
			.anyRequest()
			.authenticated();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(preauthAuthProvider());
	}
	
	@Bean 
	public UserDetailsService getSessionKeyServerUserDetailsService() {
		SessionKeyServerUserDetailsService service = new SessionKeyServerUserDetailsService();
		String serverUrl = keyserverProperties.getUrl();
		if(!StringUtils.isEmpty(serverUrl)) {
			logger.info("Setting the session key URL to {}", serverUrl);
			service.setSessionKeyServerUrl(serverUrl.trim());
		}
		String realm = keyserverProperties.getRealm();
		if(!StringUtils.isEmpty(realm)) {
			logger.info("Setting the session key URL to {}", serverUrl);
			service.setRealm(realm.trim());
		}
		return service;
	}

	@Bean
	public UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> userDetailsServiceWrapper() {
		UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> wrapper =
				new UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken>();

		wrapper.setUserDetailsService(this.getSessionKeyServerUserDetailsService());
		return wrapper;
	}

	@Bean
	public PreAuthenticatedAuthenticationProvider preauthAuthProvider() {
		PreAuthenticatedAuthenticationProvider preauthAuthProvider = new PreAuthenticatedAuthenticationProvider();
		preauthAuthProvider.setPreAuthenticatedUserDetailsService(userDetailsServiceWrapper());
		return preauthAuthProvider;
	}

	@Bean
	public RequestHeaderAuthenticationFilter ssoFilter() throws Exception {
		RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
		filter.setAuthenticationManager(authenticationManager());
		filter.setPrincipalRequestHeader("x-sessionkey-token");
		return filter;
	}

}
