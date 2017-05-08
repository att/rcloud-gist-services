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
		if(!StringUtils.isEmpty(clientId)) {
			this.clientId = clientId;
		}
	}

	public String getClientId() {
		return clientId;
	}

}
