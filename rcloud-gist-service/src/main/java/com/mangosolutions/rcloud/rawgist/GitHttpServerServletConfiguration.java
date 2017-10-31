package com.mangosolutions.rcloud.rawgist;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.mangosolutions.rcloud.rawgist.http.GistGitServlet;
import com.mangosolutions.rcloud.rawgist.http.GistRepositoryResolver;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;
import com.mangosolutions.rcloud.rawgist.repository.git.AsymetricFourFolderRepositoryStorageLocator;
import com.mangosolutions.rcloud.rawgist.repository.git.RepositoryStorageLocator;
import com.mangosolutions.rcloud.rawgist.repository.git.SymetricFourPartRepositoryStorageLocator;

@Configuration
@EnableConfigurationProperties(GistServiceProperties.class)
public class GitHttpServerServletConfiguration {
    
    public static final String REPOSITORY_PATH = "repositories";
    
    public static final String REPOSITORY_SERVLET_PATH = "/" + REPOSITORY_PATH + "/*";

    @Autowired
    private GistServiceProperties serviceProperties;
    
    @Autowired
    private GistRepositoryService gistRepositoryService;
    
    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        String root = serviceProperties.getRoot();
        File rootFolder = new File(root);
//        String gitServerPath = serviceProperties.getGitServerPath();
        Collection<RepositoryStorageLocator> locators = Arrays.asList(
                new AsymetricFourFolderRepositoryStorageLocator(rootFolder),
                new SymetricFourPartRepositoryStorageLocator(rootFolder));

        GistRepositoryResolver<HttpServletRequest> resolver = new GistRepositoryResolver<>(locators);
        GistGitServlet gitGistServlet = new GistGitServlet(resolver, gistRepositoryService, hazelcastInstance);
        ServletRegistrationBean bean = new ServletRegistrationBean(gitGistServlet, REPOSITORY_SERVLET_PATH);
        return bean;
    }
}
