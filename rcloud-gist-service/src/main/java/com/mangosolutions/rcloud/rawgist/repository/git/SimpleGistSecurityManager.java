/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.repository.GistRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistSecurityManager;

public class SimpleGistSecurityManager implements GistSecurityManager {

	private static final Set<GistAccessRight> READ_RIGHTS = new HashSet<>(Arrays.asList(GistAccessRight.READ, GistAccessRight.WRITE));
	private static final Set<GistAccessRight> WRITE_RIGHTS = new HashSet<>(Arrays.asList(GistAccessRight.WRITE));

	@Override
	public boolean canRead(GistRepository repository, UserDetails userDetails) {
		return READ_RIGHTS.contains(this.getAccessRight(repository, userDetails));
	}

	@Override
	public boolean canWrite(GistRepository repository, UserDetails userDetails) {
		return WRITE_RIGHTS.contains(this.getAccessRight(repository, userDetails));
	}

	@Override
	public boolean isOwner(GistRepository repository, UserDetails userDetails) {
		return GistRole.OWNER.equals(this.getRole(repository, userDetails));
	}

	@Override
	public GistRole getRole(GistRepository repository, UserDetails userDetails) {
		return this.isOwner(this.getMetaData(repository), userDetails)? GistRole.OWNER: GistRole.COLLABORATOR;
	}

	@Override
	public GistAccessRight getAccessRight(GistRepository repository, UserDetails userDetails) {
		GistMetadata metadata = getMetaData(repository);

		if(this.canWrite(metadata, userDetails)) {
			return GistAccessRight.WRITE;
		}
		if(this.canRead(metadata, userDetails)) {
			return GistAccessRight.READ;
		}
		return GistAccessRight.NONE;
	}

	private boolean canRead(GistMetadata metadata, UserDetails userDetails) {
		return true;
	}

	private boolean canWrite(GistMetadata metadata, UserDetails userDetails) {
		return this.isOwner(metadata, userDetails);
	}

	private GistMetadata getMetaData(GistRepository repository) {
		return repository.getMetadata();
	}

	private boolean isOwner(GistMetadata metadata, UserDetails userDetails)  {
		return userDetails.getUsername().equals(metadata.getOwner());
	}

}
