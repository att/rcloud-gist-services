/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.git.http;

import java.util.Collection;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import com.mangosolutions.rcloud.commons.spring.http.security.GrantedAuthorityFactory;
import com.mangosolutions.rcloud.sessionkeyauth.SessionKeyServerService;

public class GitServiceAuthenticationManager implements AuthenticationManager {

    private SessionKeyServerService sessionKeyServerService;

    private GrantedAuthorityFactory grantedAuthorityFactory;

    public GitServiceAuthenticationManager(SessionKeyServerService sessionKeyServerService,
            GrantedAuthorityFactory grantedAuthorityFactory) {
        this.sessionKeyServerService = sessionKeyServerService;
        this.grantedAuthorityFactory = grantedAuthorityFactory;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getPrincipal() != null ? authentication.getPrincipal().toString() : "";
        String password = authentication.getCredentials() != null ? authentication.getCredentials().toString() : "";
        this.sessionKeyServerService.authenticate(username, password);

        Collection<GrantedAuthority> authorities = grantedAuthorityFactory.resolve(authentication.getName());
        Authentication trustedAuth = new UsernamePasswordAuthenticationToken(username, password, authorities);
        return trustedAuth;
    }

    public void setSessionKeyServerService(SessionKeyServerService sessionKeyServerService) {
        this.sessionKeyServerService = sessionKeyServerService;
    }
}
