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

import com.mangosolutions.rcloud.rawgist.http.GistGitServlet;
import com.mangosolutions.rcloud.rawgist.http.GistRepositoryResolver;
import com.mangosolutions.rcloud.rawgist.repository.git.AsymetricFourFolderRepositoryStorageLocator;
import com.mangosolutions.rcloud.rawgist.repository.git.RepositoryStorageLocator;
import com.mangosolutions.rcloud.rawgist.repository.git.SymetricFourPartRepositoryStorageLocator;

@Configuration
@EnableConfigurationProperties(GistServiceProperties.class)
public class GitHttpServerServletConfiguration {

    @Autowired
    private GistServiceProperties serviceProperties;

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        String root = serviceProperties.getRoot();
        File rootFolder = new File(root);
        String gitServerPath = serviceProperties.getGitServerPath();
        Collection<RepositoryStorageLocator> locators = Arrays.asList(
                new AsymetricFourFolderRepositoryStorageLocator(rootFolder),
                new SymetricFourPartRepositoryStorageLocator(rootFolder));

        GistRepositoryResolver<HttpServletRequest> resolver = new GistRepositoryResolver<>(locators);
        GistGitServlet gitGistServlet = new GistGitServlet(resolver);
        ServletRegistrationBean bean = new ServletRegistrationBean(gitGistServlet, gitServerPath + "/*");
        return bean;
    }
}
