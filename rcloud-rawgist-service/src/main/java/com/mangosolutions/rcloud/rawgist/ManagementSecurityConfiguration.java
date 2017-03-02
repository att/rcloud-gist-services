package com.mangosolutions.rcloud.rawgist;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;

//@Order(ManagementServerProperties.BASIC_AUTH_ORDER)
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(ManagementServerProperties.ACCESS_OVERRIDE_ORDER)
public class ManagementSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private ManagementServerProperties managementProperties;

	@Autowired
	private SecurityProperties securityProperties;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.requestMatchers().requestMatchers(new RequestMatcher() {

			@Override
			public boolean matches(HttpServletRequest request) {
				return managementProperties.getPort() == request.getLocalPort();
			}
		}).and().authorizeRequests().anyRequest().hasRole("ADMIN").and().httpBasic();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.apply(new DefaultInMemoryUserDetailsManagerConfigurer(
				this.securityProperties));
	}

	private static class DefaultInMemoryUserDetailsManagerConfigurer
			extends InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> {

		private final SecurityProperties securityProperties;

		private static final Logger logger = LoggerFactory.getLogger(DefaultInMemoryUserDetailsManagerConfigurer.class);
		
		DefaultInMemoryUserDetailsManagerConfigurer(SecurityProperties securityProperties) {
			this.securityProperties = securityProperties;
		}

		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
//			if (auth.isConfigured()) {
//				return;
//			}
			User user = this.securityProperties.getUser();
			if (user.isDefaultPassword()) {
				logger.info("\n\nUsing default security password: " + user.getPassword() + "\n");
			}
			Set<String> roles = new LinkedHashSet<String>(user.getRole());
			withUser(user.getName()).password(user.getPassword()).roles(roles.toArray(new String[roles.size()]));
			setField(auth, "defaultUserDetailsService", getUserDetailsService());
			super.configure(auth);
		}

		private void setField(Object target, String name, Object value) {
			try {
				Field field = ReflectionUtils.findField(target.getClass(), name);
				ReflectionUtils.makeAccessible(field);
				ReflectionUtils.setField(field, target, value);
			} catch (Exception ex) {
				logger.info("Could not set " + name);
			}
		}

	}
}
