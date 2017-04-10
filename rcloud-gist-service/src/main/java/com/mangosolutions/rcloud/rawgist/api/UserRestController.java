/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.api;

import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mangosolutions.rcloud.rawgist.model.GistIdentity;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;

@RestController()
@RequestMapping(value = "/user", produces = { 
		MediaType.APPLICATION_JSON_VALUE,
		"application/vnd.github.beta+json",
		"application/vnd.github.v3+json"
		})
public class UserRestController {

	@RequestMapping(method = RequestMethod.GET)
	public GistIdentity getUser(@AuthenticationPrincipal User activeUser) {
		GistIdentity response = new GistIdentity();
		String username = activeUser.getUsername();
		response.setLogin(username);
		return response;
	}
	
	@RequestMapping(value = "/{username}/gists", method = RequestMethod.GET)
	public List<GistResponse> getUsersPublicGists() {
		return Collections.emptyList();
	}

}
