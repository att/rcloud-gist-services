/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository;

import java.io.File;

import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.git.GistMetadata;

public interface GistRepository {

	File getGistRepositoryFolder(UserDetails userDetails);

	GistResponse getGist(UserDetails userDetails);

	GistResponse getGist(String commitId, UserDetails userDetails);

	GistResponse createGist(GistRequest request, String gistId, UserDetails userDetails);

	GistResponse editGist(GistRequest request, UserDetails userDetails);

	GistResponse fork(GistRepository forkedRepository, String gistId, UserDetails userDetails);

	String getId();

	void registerFork(GistRepository forkedRepository);
	
	GistMetadata getMetadata();
	
	GistCommentRepository getCommentRepository();

}
