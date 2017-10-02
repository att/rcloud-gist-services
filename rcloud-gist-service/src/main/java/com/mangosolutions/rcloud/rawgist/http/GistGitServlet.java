package com.mangosolutions.rcloud.rawgist.http;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;

public class GistGitServlet extends GitServlet {

    private static final long serialVersionUID = -5653111519355846054L;

    public GistGitServlet(RepositoryResolver<HttpServletRequest> resolver) {
        super();
        super.setRepositoryResolver(resolver);
    }
    
}
