/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.git.http;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;

@Configuration
public class GitHttpServerServletConfiguration {

    public static final String REPOSITORY_PATH = "repositories";

    public static final String REPOSITORY_SERVLET_PATH = "/" + REPOSITORY_PATH + "/*";

    @Autowired
    private GistRepositoryService gistRepositoryService;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        GistRepositoryResolver<HttpServletRequest> resolver = new GistRepositoryResolver<>(gistRepositoryService);
        GistGitServlet gitGistServlet = new GistGitServlet(resolver, gistRepositoryService, hazelcastInstance);
        ServletRegistrationBean bean = new ServletRegistrationBean(gitGistServlet, REPOSITORY_SERVLET_PATH);
        return bean;
    }
}
