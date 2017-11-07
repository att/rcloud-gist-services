/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;

import com.mangosolutions.rcloud.commons.spring.http.security.AnonymousUser;
import com.mangosolutions.rcloud.commons.spring.http.security.GrantedAuthorityFactory;

public class SessionKeyServerUserDetailsService
        implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(SessionKeyServerUserDetailsService.class);

    private SessionKeyServerService sessionKeyServerService;

    private GrantedAuthorityFactory grantedAuthorityFactory = new GrantedAuthorityFactory();

    public SessionKeyServerUserDetailsService(SessionKeyServerService sessionKeyServerService) {
        this.sessionKeyServerService = sessionKeyServerService;
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        if (token == null) {
            throw new UsernameNotFoundException("SessionKey token not correctly defined.");
        }
        UserDetails userDetails = createAnonymousUser();
        String sessionKey = getSessionKey(token);
        if (!isAnonymousUser(sessionKey)) {
            SessionKeyServerResponse response = querySessionKeyServer(token, sessionKey);
            userDetails = convertToUserDetails(response, sessionKey);
        }
        return userDetails;
    }

    private SessionKeyServerResponse querySessionKeyServer(PreAuthenticatedAuthenticationToken token,
            String sessionKey) {
        String clientId = getClientId(token);
        SessionKeyServerResponse response = this.sessionKeyServerService.validateToken(clientId, sessionKey);
        return response;
    }

    private boolean isAnonymousUser(String sessionKey) {
        return StringUtils.isEmpty(sessionKey);
    }

    private String getSessionKey(PreAuthenticatedAuthenticationToken token) {
        Object principal = token.getPrincipal();
        // Assume that the principal is not null and is a string
        if (!(principal instanceof String)) {
            logger.warn("SessionKey token not correctly defined.");
            throw new UsernameNotFoundException("SessionKey token not correctly defined.");
        }
        String sessionKey = (String) token.getPrincipal();
        return sessionKey;
    }

    private String getClientId(PreAuthenticatedAuthenticationToken token) {
        Object details = token.getDetails();
        String clientId = "default";
        if (details instanceof SessionKeyServerAuthenticationDetails) {
            clientId = ((SessionKeyServerAuthenticationDetails) details).getClientId();
        }
        return clientId;
    }

    public void setGrantedAuthorityFactory(GrantedAuthorityFactory factory) {
        this.grantedAuthorityFactory = factory;
    }

    private UserDetails convertToUserDetails(SessionKeyServerResponse response, String sessionKey) {
        if (!SessionKeyServerResult.YES.equals(response.getResult())) {
            throw new UsernameNotFoundException(
                    "Token provided is not valid. Response from SessionKeyServer is " + response.getResult());
        }
        String username = response.getName();
        Collection<GrantedAuthority> authorities = getAuthorities(username);
        UserDetails details = new User(username, sessionKey, authorities);
        return details;
    }

    private Collection<GrantedAuthority> getAuthorities(String username) {
        return this.grantedAuthorityFactory.resolve(username);
    }

    private UserDetails createAnonymousUser() {
        Collection<GrantedAuthority> authorities = getAuthorities(null);
        return new AnonymousUser(authorities);
    }

}
