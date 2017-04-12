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

	private RepositoryLayout layout;
	
	private MetadataStore metadataStore;
	
	private CommentStore commentStore;

	private HistoryStore historyStore;

	public GitGistRepository(File repositoryFolder, MetadataStore metadataStore, CommentStore commentStore, HistoryStore historyStore) {
		this.metadataStore = metadataStore;
		this.commentStore = commentStore;
		this.historyStore = historyStore;
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
		CreateOrUpdateGistOperation op = new CreateOrUpdateGistOperation();
		op.setGistId(gistId);
		op.setCommentRepository(this.getCommentRepository());
		op.setHistoryStore(this.historyStore);
		op.setLayout(this.layout);
		op.setMetadataStore(this.metadataStore);
		op.setUser(userDetails);
		op.setGistRequest(request);
		return op.call();
	}
	
	@Override
	public GistResponse forkGist(GistRepository originalRepository, String gistId, UserDetails userDetails) {
		ForkGistOperation op = new ForkGistOperation();
		op.setGistId(gistId);
		op.setCommentRepository(this.getCommentRepository());
		op.setHistoryStore(this.historyStore);
		op.setLayout(this.layout);
		op.setMetadataStore(this.metadataStore);
		op.setUser(userDetails);
		op.setOriginalRepository(originalRepository);
		op.setNewRepository(this);
		return op.call();
	}

	@Override
	public GistResponse updateGist(GistRequest request, UserDetails userDetails) {
		CreateOrUpdateGistOperation op = new CreateOrUpdateGistOperation();
		op.setGistId(this.getId());
		op.setCommentRepository(this.getCommentRepository());
		op.setHistoryStore(this.historyStore);
		op.setLayout(this.layout);
		op.setMetadataStore(this.metadataStore);
		op.setUser(userDetails);
		op.setGistRequest(request);
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
		ReadGistOperation op = new ReadGistOperation();
		op.setGistId(this.getId());
		op.setCommentRepository(this.getCommentRepository());
		op.setHistoryStore(this.historyStore);
		op.setLayout(this.layout);
		op.setMetadataStore(this.metadataStore);
		op.setUser(activeUser);
		op.setCommitId(commitId);
		return op.call();
	}
	
	private GistResponse readGistInternal(UserDetails activeUser) {
		ReadGistOperation op = new ReadGistOperation();
		op.setGistId(this.getId());
		op.setCommentRepository(this.getCommentRepository());
		op.setHistoryStore(this.historyStore);
		op.setLayout(this.layout);
		op.setMetadataStore(this.metadataStore);
		op.setUser(activeUser);
		return op.call();
	}



	private void updateForkInformation(GistRepository forkedRepository) {
		GistMetadata metadata = this.getMetadata();
		GistMetadata forksMetadata = forkedRepository.getMetadata();
		Fork fork = new Fork();
		fork.setCreatedAt(forksMetadata.getCreatedAt());
		fork.setUpdatedAt(forksMetadata.getUpdatedAt());
		fork.setId(forksMetadata.getId());
		String forkOwner = forksMetadata.getOwner(); 
		GistIdentity forkOwnerIdentity = new GistIdentity();
		forkOwnerIdentity.setLogin(forkOwner);
		fork.setUser(forkOwnerIdentity);
		metadata.addFork(fork);
		this.saveMetadata(metadata);
	}




}
