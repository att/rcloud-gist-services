/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import com.hazelcast.core.HazelcastInstance;
import com.mangosolutions.rcloud.rawgist.repository.GistIdGenerator;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryFactory;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;
import com.mangosolutions.rcloud.rawgist.repository.GistSecurityManager;
import com.mangosolutions.rcloud.rawgist.repository.git.CollaborationDataStore;
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
    private GistRepositoryFactory repositoryFactory;

    @Bean
    public GistRepositoryService getGistRepository() throws IOException {
        GitGistRepositoryService repositoryService = new GitGistRepositoryService(serviceProperties.getRoot(),
                this.getGistIdGenerator(), hazelcastInstance);
        repositoryService.setLockTimeout(serviceProperties.getLockTimeout());
        repositoryService.setSecurityManager(getGistSecurityManager());
        repositoryService.setGistRepositoryFactory(repositoryFactory);
        return repositoryService;
    }
    
    
    @Bean
    @RefreshScope
    public CollaborationDataStore getCollaborationDataStore() {
        //Copies in the collaborator information into the data store, this then clears the values so that 
        //if/when a refresh occurs the map and values are built correctly from the configuration otherwise 
        //when a key/value is removed it isn't removed from the associated Map/List 
        Map<String, List<String>> config = new HashMap<>(serviceProperties.getUsers().getCollaborations());
        Map<String, List<String>> collaborations = new HashMap<>();
        for(Map.Entry<String, List<String>> entry: config.entrySet()) {
            List<String> values = entry.getValue();
            collaborations.put(entry.getKey(), new LinkedList<>(values));
            values.clear();
        }
        config.clear();
        logger.info("Loaded collaborations: {}", collaborations);
        
        return new CollaborationDataStore(collaborations);
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
                manager = new GrantedAuthorityGistSecurityManager(getCollaborationDataStore());
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
