/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "gists")
public class GistServiceProperties {

    public static final String STRICT_SECURITY_MANAGER = "strict";
    public static final String PERMISSIVE_SECURITY_MANAGER = "permissive";

    @NestedConfigurationProperty
    private GistUsersProperties users = new GistUsersProperties();
    
    private String security = "default";

    private String root;

    private String cache = "gists";

    private int lockTimeout = 30;

    private String sessionKeyServerUrl = null;

    private String sessionKeyServerRealm = "rcloud";
    
    private String gitServerPath = "/repositories";

    public String getGitServerPath() {
        return gitServerPath;
    }

    public void setGitServerPath(String gitServerPath) {
        this.gitServerPath = gitServerPath;
    }

    private List<String> mediatypes = new ArrayList<>();


    public String getSessionKeyServerRealm() {
        return sessionKeyServerRealm;
    }

    public void setSessionKeyServerRealm(String sessionKeyServerRealm) {
        this.sessionKeyServerRealm = sessionKeyServerRealm;
    }

    public int getLockTimeout() {
        return lockTimeout;
    }

    public void setLockTimeout(int lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cacheName) {
        this.cache = cacheName;
    }

    public String getSessionKeyServerUrl() {
        return sessionKeyServerUrl;
    }

    public void setSessionKeyServerUrl(String sessionKeyServerUrl) {
        this.sessionKeyServerUrl = sessionKeyServerUrl;
    }

    public List<String> getMediatypes() {
        return mediatypes;
    }

    public void setMediatypes(List<String> mediatypes) {
        this.mediatypes = mediatypes;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String securityManager) {
        this.security = securityManager;
    }

    public GistUsersProperties getUsers() {
        return users;
    }

    public void setUsers(GistUsersProperties users) {
        this.users = users;
    }

}
