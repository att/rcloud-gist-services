/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.util.StringUtils;

public class SessionKeyServerWebAuthenticationDetailsSource
        implements AuthenticationDetailsSource<HttpServletRequest, SessionKeyServerAuthenticationDetails> {

    private static final String DEFAULT_CLIENT_ID_PARAMETER = "client_id";

    private String clientIdParameter = DEFAULT_CLIENT_ID_PARAMETER;

    public SessionKeyServerWebAuthenticationDetailsSource() {

    }

    public SessionKeyServerWebAuthenticationDetailsSource(String clientIdParameter) {
        if (!StringUtils.isEmpty(clientIdParameter)) {
            this.clientIdParameter = clientIdParameter.trim();
        }
    }

    @Override
    public SessionKeyServerAuthenticationDetails buildDetails(HttpServletRequest context) {
        String clientId = getClientId(context);
        return new SessionKeyServerAuthenticationDetails(clientId);
    }

    private String getClientId(HttpServletRequest context) {
        return context.getParameter(clientIdParameter);
    }

}
