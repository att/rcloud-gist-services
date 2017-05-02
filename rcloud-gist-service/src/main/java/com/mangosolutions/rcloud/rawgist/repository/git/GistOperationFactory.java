/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangosolutions.rcloud.rawgist.model.FileContent;
import com.mangosolutions.rcloud.rawgist.model.GistHistory;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.repository.GistCommentRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistRepository;

@Component
public class GistOperationFactory {


	@Autowired
	private HistoryCache historyCache = new HistoryCache() {

		@Override
		public List<GistHistory> load(String commitId) {
			return new LinkedList<>();
		}

		@Override
		public List<GistHistory> save(String commitId, List<GistHistory> history) {
			return history;
		}

	};


	@Autowired
	private FileContentCache fileContentCache = new FileContentCache() {

		@Override
		public FileContent load(String contentId, String path) {
			return null;
		}

		@Override
		public FileContent save(String contentId, String path, FileContent content) {
			return content;
		}

	};

	@Autowired
	private MetadataStore metadataStore;

	@Autowired
	private CommentStore commentStore;

	public GistOperationFactory() {
		this(new ObjectMapper());
	}

	public GistOperationFactory(ObjectMapper objectMapper) {
		this.metadataStore = new GistMetadataStore(objectMapper);
		this.commentStore = new GistCommentStore(objectMapper);
	}

	@Autowired
	public GistOperationFactory(MetadataStore metadataStore, CommentStore commentStore, HistoryCache historyCache, FileContentCache fileContentCache) {
		this.metadataStore = metadataStore;
		this.commentStore = commentStore;
		this.historyCache = historyCache;
		this.fileContentCache = fileContentCache;
	}

	public HistoryCache getHistoryCache() {
		return historyCache;
	}

	public void setHistoryCache(HistoryCache historyCache) {
		this.historyCache = historyCache;
	}

	public MetadataStore getMetadataStore() {
		return metadataStore;
	}

	public void setMetadataStore(MetadataStore metadataStore) {
		this.metadataStore = metadataStore;
	}

	public CommentStore getCommentStore() {
		return commentStore;
	}

	public void setCommentStore(CommentStore commentStore) {
		this.commentStore = commentStore;
	}

	public ReadGistOperation getReadOperation(RepositoryLayout layout, String gistId, UserDetails user, String commitId) {
		GistCommentRepository repository = new GitGistCommentRepository(layout.getCommentsFile(), commentStore);
		ReadGistOperation op = new ReadGistOperation(layout, gistId, user);
		if(!StringUtils.isEmpty(commitId)) {
			op.setCommitId(commitId);
		}
		op.setCommentRepository(repository);
		op.setCommitId(commitId);
		op.setHistorycache(historyCache);
		op.setMetadataStore(this.metadataStore);
		op.setFileContentCache(fileContentCache);
		return op;
	}

	public CreateOrUpdateGistOperation getCreateOrUpdateOperation(RepositoryLayout layout, String gistId, GistRequest gistRequest, UserDetails user) {
		GistCommentRepository repository = new GitGistCommentRepository(layout.getCommentsFile(), commentStore);
		CreateOrUpdateGistOperation op = new CreateOrUpdateGistOperation(layout, gistId, gistRequest, user);
		op.setCommentRepository(repository);
		op.setHistorycache(historyCache);
		op.setMetadataStore(this.metadataStore);
		op.setFileContentCache(fileContentCache);
		return op;
	}

	public ForkGistOperation getForkOperation(RepositoryLayout layout, String gistId, GistRepository originalRepository, GistRepository newRepository, UserDetails user) {
		GistCommentRepository repository = new GitGistCommentRepository(layout.getCommentsFile(), commentStore);
		ForkGistOperation op = new ForkGistOperation(layout, originalRepository, newRepository, gistId, user);
		op.setCommentRepository(repository);
		op.setHistorycache(historyCache);
		op.setMetadataStore(this.metadataStore);
		op.setFileContentCache(fileContentCache);
		return op;
	}

	public InitRepositoryLayoutOperation getInitRepositoryLayoutOperation(File repositoryRoot) {
		return new InitRepositoryLayoutOperation(repositoryRoot);
	}

}
