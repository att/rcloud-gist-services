/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.security;

import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.repository.GistRepository;

public class PermissiveGistSecurityManager extends SimpleGistSecurityManager {

	@Override
	public boolean canRead(GistRepository repository, UserDetails userDetails) {
		return true;
	}

	@Override
	public boolean canWrite(GistRepository repository, UserDetails userDetails) {
		return true;
	}

	@Override
	public GistAccessRight getAccessRight(GistRepository repository, UserDetails userDetails) {
		return GistAccessRight.WRITE;
	}

}
