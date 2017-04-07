/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.mangosolutions.rcloud.rawgist.repository.GistIdGenerator;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;
import com.mangosolutions.rcloud.rawgist.repository.GistSecurityManager;
import com.mangosolutions.rcloud.rawgist.repository.GitGistRepositoryService;
import com.mangosolutions.rcloud.rawgist.repository.PermissiveGistSecurityManager;
import com.mangosolutions.rcloud.rawgist.repository.SimpleGistSecurityManager;
import com.mangosolutions.rcloud.rawgist.repository.UUIDGistIdGenerator;

/**
 * Main Spring configuration
 *
 */
@Configuration()
@EnableConfigurationProperties(GistServiceProperties.class)
public class GistServiceConfiguration {

	private final Logger logger = LoggerFactory.getLogger(GistServiceConfiguration.class);
	
	@Autowired
	private GistServiceProperties serviceProperties;

	@Autowired
	private HazelcastInstance hazelcastInstance;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Bean
	public GistRepositoryService getGistRepository() throws IOException {
		GitGistRepositoryService repo = new GitGistRepositoryService(serviceProperties.getRoot(),
				this.getGistIdGenerator(), hazelcastInstance, objectMapper);
		repo.setLockTimeout(serviceProperties.getLockTimeout());
		repo.setSecurityManager(getGistSecurityManager());
		return repo;
	}
	
	@Bean
	public GistSecurityManager getGistSecurityManager() {
		if(GistServiceProperties.STRICT_SECURITY_MANAGER.equals(serviceProperties.getSecurity())) {
			logger.info("Using strict gist security manager.");
			return new SimpleGistSecurityManager();
		} else {
			logger.info("Using permissive gist security manager.");
			return new PermissiveGistSecurityManager();
		}
	}

	@Bean
	public GistIdGenerator getGistIdGenerator() {
		return new UUIDGistIdGenerator();
	}

	@Bean
	public CommonsRequestLoggingFilter requestLoggingFilter() {
	    CommonsRequestLoggingFilter crlf = new CommonsRequestLoggingFilter();
	    crlf.setIncludeClientInfo(true);
	    crlf.setIncludeQueryString(true);
	    crlf.setIncludePayload(true);
	    return crlf;
	}
	
}
