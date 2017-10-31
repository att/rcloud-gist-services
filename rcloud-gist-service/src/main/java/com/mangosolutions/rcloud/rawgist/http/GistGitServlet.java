/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.http;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;
import com.mangosolutions.rcloud.sessionkeyauth.AnonymousUser;

public class GistGitServlet extends GitServlet {

    private static final long serialVersionUID = -5653111519355846054L;
    private static final Logger logger = LoggerFactory.getLogger(GistGitServlet.class);
    private GistRepositoryService gistRepositoryService;
    private HazelcastInstance hazelcastInstance;

    public GistGitServlet(RepositoryResolver<HttpServletRequest> resolver, GistRepositoryService gistRepositoryService,
            HazelcastInstance hazelcastInstance) {
        super();
        super.setRepositoryResolver(resolver);
        this.gistRepositoryService = gistRepositoryService;
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String method = req.getMethod();
        boolean authorized = true;
        boolean clearCaches = false;
        switch (method) {
        case "POST":
            ;
        case "PUT":
            ;
        case "PATCH":
            ;
        case "DELETE":
            clearCaches = true;
        default:
            authorized = authorizeRequest(req);
            ;
        }
        if (!authorized) {
            res.sendError(SC_FORBIDDEN, "You are not authorized to access this repository");
        } else {
            // obtain a lock
            String gistId = this.getGistId(req);
            Lock lock = gistRepositoryService.acquireGistLock(gistId);
            try {
                super.service(req, res);
                // clear caches
                if (clearCaches) {
                    this.clearCaches(gistId);
                }
            } finally {
                // release lock
                lock.unlock();
            }
        }
    }

    private void clearCaches(String gistId) {
        Config config = hazelcastInstance.getConfig();
        Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        for (MapConfig mapConfig : mapConfigs.values()) {
            String cacheName = mapConfig.getName();
            logger.debug("Clearing {} from cache {}", gistId, cacheName);

            IMap<Object, Object> mapCache = hazelcastInstance.getMap(cacheName);
            if (mapCache.containsKey(gistId)) {
                Object oldValue = mapCache.remove(gistId);
                if (oldValue == null) {
                    logger.debug("No matching key for {} in cache {}", gistId, cacheName);
                }
            }
        }
    }

    private boolean authorizeRequest(HttpServletRequest req) {
        if (req.getUserPrincipal() == null) {
            return this.authorizeRequestForRead(req);
        } else {
            return this.authorizeRequestForWrite(req);
        }
    }

    private boolean authorizeRequestForWrite(HttpServletRequest req) {
        UserDetails user = getUser(req);
        String gistId = getGistId(req);
        return gistRepositoryService.isWritable(gistId, user);
    }

    private boolean authorizeRequestForRead(HttpServletRequest req) {
        UserDetails user = getUser(req);
        String gistId = getGistId(req);
        return gistRepositoryService.isReadable(gistId, user);
    }

    private String getGistId(HttpServletRequest req) {
        String pathParts = req.getPathInfo();
        String[] parts = StringUtils.split(pathParts, "/");
        // should be the last part
        String id = null;
        if (parts.length > 0) {
            id = parts[0];
        }

        return id;
    }

    private UserDetails getUser(HttpServletRequest req) {
        Principal principal = req.getUserPrincipal();
        if (principal != null && principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
            UserDetails userDetails = new User(authToken.getPrincipal().toString(),
                    authToken.getCredentials().toString(), authToken.getAuthorities());
            return userDetails;
        } else if (principal == null) {
            // Anonymous user
            return new AnonymousUser();
        } else {
            throw new UsernameNotFoundException("An error occured creating user details, expected "
                    + UsernamePasswordAuthenticationToken.class.getSimpleName() + " but got "
                    + principal.getClass().getSimpleName());
        }
    }

}
