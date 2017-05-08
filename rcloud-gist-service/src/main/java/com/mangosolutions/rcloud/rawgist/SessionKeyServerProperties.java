/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.mangosolutions.rcloud.sessionkeyauth.KeyServerConfiguration;

@ConfigurationProperties(prefix = "gists")
public class SessionKeyServerProperties {

	private static final String DEFAULT_ACCESS_TOKEN_NAME = "access_token";
	
	private static final String DEFAULT_CLIENT_ID_NAME = "client_id";
	
	private List<KeyServerConfiguration> keyservers;
	
	private String token = DEFAULT_ACCESS_TOKEN_NAME;
	private String clientId = DEFAULT_CLIENT_ID_NAME;
	
	public List<KeyServerConfiguration> getKeyservers() {
		return keyservers;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setKeyservers(List<KeyServerConfiguration> keyservers) {
		this.keyservers = keyservers;
	}

}
