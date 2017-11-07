/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import com.mangosolutions.rcloud.sessionkeyauth.KeyServerConfiguration;

@ConfigurationProperties(prefix = "gists")
@RefreshScope
public class SessionKeyServerProperties {

    private static final String DEFAULT_ACCESS_TOKEN_NAME = "access_token";

    private static final String DEFAULT_CLIENT_ID_NAME = "client_id";

    private Map<String, KeyServerConfiguration> keyservers = new HashMap<String, KeyServerConfiguration>();

    private String accessTokenParam = DEFAULT_ACCESS_TOKEN_NAME;
    private String clientIdParam = DEFAULT_CLIENT_ID_NAME;

    public String getAccessTokenParam() {
        return accessTokenParam;
    }

    public Map<String, KeyServerConfiguration> getKeyservers() {
        return keyservers;
    }

    public void setKeyservers(Map<String, KeyServerConfiguration> keyservers) {
        this.keyservers = keyservers;
    }

    public void setAccessTokenParam(String accessTokenParam) {
        this.accessTokenParam = accessTokenParam;
    }

    public String getClientIdParam() {
        return clientIdParam;
    }

    public void setClientIdParam(String clientIdParam) {
        this.clientIdParam = clientIdParam;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessTokenParam == null) ? 0 : accessTokenParam.hashCode());
        result = prime * result + ((clientIdParam == null) ? 0 : clientIdParam.hashCode());
        result = prime * result + ((keyservers == null) ? 0 : keyservers.hashCode());
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
        SessionKeyServerProperties other = (SessionKeyServerProperties) obj;
        if (accessTokenParam == null) {
            if (other.accessTokenParam != null)
                return false;
        } else if (!accessTokenParam.equals(other.accessTokenParam))
            return false;
        if (clientIdParam == null) {
            if (other.clientIdParam != null)
                return false;
        } else if (!clientIdParam.equals(other.clientIdParam))
            return false;
        if (keyservers == null) {
            if (other.keyservers != null)
                return false;
        } else if (!keyservers.equals(other.keyservers))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SessionKeyServerProperties [keyservers=" + keyservers + ", accessTokenParam=" + accessTokenParam
                + ", clientIdParam=" + clientIdParam + "]";
    }

}
