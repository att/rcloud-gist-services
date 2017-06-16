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
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryFactory;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;
import com.mangosolutions.rcloud.rawgist.repository.GistSecurityManager;
import com.mangosolutions.rcloud.rawgist.repository.git.GitGistRepositoryService;
import com.mangosolutions.rcloud.rawgist.repository.git.UUIDGistIdGenerator;
import com.mangosolutions.rcloud.rawgist.repository.security.GrantedAuthorityGistSecurityManager;
import com.mangosolutions.rcloud.rawgist.repository.security.PermissiveGistSecurityManager;
import com.mangosolutions.rcloud.rawgist.repository.security.SimpleGistSecurityManager;

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

    @Autowired
    private GistRepositoryFactory repositoryFactory;

    @Bean
    public GistRepositoryService getGistRepository() throws IOException {
        GitGistRepositoryService repo = new GitGistRepositoryService(serviceProperties.getRoot(),
                this.getGistIdGenerator(), hazelcastInstance);
        repo.setLockTimeout(serviceProperties.getLockTimeout());
        repo.setSecurityManager(getGistSecurityManager());
        repo.setGistRepositoryFactory(repositoryFactory);
        return repo;
    }

    @Bean
    public GistSecurityManager getGistSecurityManager() {
        GistSecurityManager manager = null;
        String securityType = serviceProperties.getSecurity();
        switch (securityType) {
            case GistServiceProperties.STRICT_SECURITY_MANAGER:
                manager = new SimpleGistSecurityManager();
                break;
            case GistServiceProperties.PERMISSIVE_SECURITY_MANAGER:
                manager = new PermissiveGistSecurityManager();
                break;
            default:
                manager = new GrantedAuthorityGistSecurityManager();
                break;
        }
        return manager;
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
