package com.mangosolutions.rcloud.rawgist;

import org.eclipse.jgit.http.server.GitServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GistServiceProperties.class)
public class GitHttpServerServletConfiguration {

    @Autowired
    private GistServiceProperties serviceProperties;
    
    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
        String root = serviceProperties.getRoot();
        String gitServerPath = serviceProperties.getGitServerPath();
        ServletRegistrationBean bean = new ServletRegistrationBean(new GitServlet(), gitServerPath + "/*");
        bean.addInitParameter("base-path", root);
        bean.addInitParameter("export-all", "1");
        return bean;
    }
}
