/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import java.io.Serializable;

import org.springframework.util.StringUtils;

public class SessionKeyServerAuthenticationDetails implements Serializable {

    private static final long serialVersionUID = 6663642339898628671L;

    private String clientId = "default";

    public SessionKeyServerAuthenticationDetails() {
        this("default");
    }

    public SessionKeyServerAuthenticationDetails(String clientId) {
        if (!StringUtils.isEmpty(clientId)) {
            this.clientId = clientId;
        }
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SessionKeyServerAuthenticationDetails other = (SessionKeyServerAuthenticationDetails) obj;
        if (clientId == null) {
            if (other.clientId != null)
                return false;
        } else if (!clientId.equals(other.clientId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SessionKeyServerAuthenticationDetails [clientId=" + clientId + "]";
    }

}
