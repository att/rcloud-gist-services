/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.model.Fork;
import com.mangosolutions.rcloud.rawgist.model.GistIdentity;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistCommentRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistRepository;

public class GitGistRepository implements GistRepository, Serializable {

	private static final long serialVersionUID = -8235501365399798269L;

	private static final Logger logger = LoggerFactory.getLogger(GitGistRepository.class);

	static final String B64_BINARY_EXTENSION = "b64";

	private GistOperationFactory gistOperationFactory;
	
	private RepositoryLayout layout;
	
	private MetadataStore metadataStore;
	
	private CommentStore commentStore;

	public GitGistRepository(File repositoryFolder) {
		this(repositoryFolder, new GistOperationFactory());
	}
	
	
	public GitGistRepository(File repositoryFolder, GistOperationFactory gistOperationFactory) {
		this.gistOperationFactory = gistOperationFactory; 
		this.metadataStore = gistOperationFactory.getMetadataStore();
		this.commentStore = gistOperationFactory.getCommentStore();
		InitRepositoryLayoutOperation op = new InitRepositoryLayoutOperation(repositoryFolder);
		this.layout = op.call();
	}
	
	
	@Override
	public String getId() {
		return this.getMetadata().getId();
	}

	@Override
	public File getGistRepositoryFolder(UserDetails owner) {
		return layout.getRootFolder();
	}

	public File getGistGitRepositoryFolder(UserDetails owner) {
		return layout.getBareFolder();
	}

	@Override
	public GistResponse readGist(UserDetails userDetails) {
		return readGistInternal(userDetails);
	}

	@Override
	public GistResponse readGist(String commitId, UserDetails userDetails) {
		return readGistInternal(commitId, userDetails);
	}

	@Override
	public GistResponse createGist(GistRequest request, String gistId, UserDetails userDetails) {
		CreateOrUpdateGistOperation op = gistOperationFactory.getCreateOrUpdateOperation(layout, gistId, request, userDetails);
		return op.call();
	}
	
	@Override
	public GistResponse forkGist(GistRepository originalRepository, String gistId, UserDetails userDetails) {
		
		ForkGistOperation op = gistOperationFactory.getForkOperation(layout, gistId, originalRepository, this, userDetails);
		return op.call();

	}

	@Override
	public GistResponse updateGist(GistRequest request, UserDetails userDetails) {
		CreateOrUpdateGistOperation op = gistOperationFactory.getCreateOrUpdateOperation(layout, this.getId(), request, userDetails);
		return op.call();
	}


	@Override
	public GistMetadata getMetadata() {
		return metadataStore.load(layout.getMetadataFile());
	}

	@Override
	public void registerFork(GistRepository forkedRepository) {
		this.updateForkInformation(forkedRepository);
	}

	@Override
	public GistCommentRepository getCommentRepository() {
		return new GitGistCommentRepository(this.layout.getCommentsFile(), this.commentStore);
	}

	private void saveMetadata(GistMetadata metadata) {
		metadataStore.save(this.layout.getMetadataFile(), metadata);
	}

	private GistResponse readGistInternal(String commitId, UserDetails activeUser) {
		ReadGistOperation op = gistOperationFactory.getReadOperation(layout, this.getId(), activeUser, commitId);
		return op.call();
	}
	
	private GistResponse readGistInternal(UserDetails activeUser) {
		return this.readGistInternal(null, activeUser);
	}

	private void updateForkInformation(GistRepository forkedRepository) {
		GistMetadata metadata = this.getMetadata();
		GistMetadata forksMetadata = forkedRepository.getMetadata();
		Fork fork = new Fork();
		fork.setId(forksMetadata.getId());
		metadata.addOrUpdateFork(fork);
		this.saveMetadata(metadata);
	}




}
