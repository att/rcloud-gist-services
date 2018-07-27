/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistErrorCode;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryError;

@Component
public class GistCommentStore implements CommentStore {

	private static final Logger logger = LoggerFactory.getLogger(GistCommentStore.class);

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${gists.commentstore.workingCopySuffix:.tmp}")
	private String workingCopySuffix = ".tmp";
	
	public GistCommentStore() {
		this.objectMapper = new ObjectMapper();
	}

	@Autowired
	public GistCommentStore(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	@Cacheable(value = "commentstore", key = "#store.getAbsolutePath()")
	public List<GistCommentResponse> load(File store) {
		List<GistCommentResponse> comments = new ArrayList<>();
		File workingCopy = workingCopyFor(store);
		if (store.exists() || restoreState(workingCopy, store)) {
			try {
				comments = objectMapper.readValue(store, new TypeReference<List<GistCommentResponse>>() {
				});
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.ERR_COMMENTS_NOT_READABLE, "Could not read comments");
				logger.error(error.getFormattedMessage() + " with path {}", store);
				throw new GistRepositoryError(error, e);
			}
		}
		return comments == null? new ArrayList<GistCommentResponse>(): comments;
	}

	@Override
	@CachePut(cacheNames = "commentstore", key = "#store.getAbsolutePath()")
	public List<GistCommentResponse> save(File store, List<GistCommentResponse> comments) {
		if(comments != null) {
			try {
				File workingCopy = new File(store.getParent(), store.getName() + workingCopySuffix);
				if(!store.exists()) {
					restoreState(workingCopy, store);
				}
				write(workingCopy, comments);
				if(store.exists()) {
					Files.delete(store.toPath());
				}
				Files.move(workingCopy.toPath(), store.toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.ERR_COMMENTS_NOT_WRITEABLE, "Could not save comments");
				logger.error(error.getFormattedMessage() + " with path {}", store);
				throw new GistRepositoryError(error, e);
			}
		}
		return comments;
	}

	private void write(File output, List<GistCommentResponse> comments) throws IOException {
		try {
			objectMapper.writeValue(output, comments);
		} catch(IOException e) {
			if(output.exists()) {
				Files.delete(output.toPath());
			}
			GistError error = new GistError(GistErrorCode.ERR_COMMENTS_NOT_WRITEABLE, "Could not save comments");
			logger.error(error.getFormattedMessage() + " with path {}", output);
			throw new GistRepositoryError(error, e);
		}
	}

	private File workingCopyFor(File store) {
		return new File(store.getParent(), store.getName() + workingCopySuffix);
	}
	
	/**
	 * Restores state of <code>to</code> file from <code>from</code> by performing atomic move.
	 * 
	 * @param from file
	 * @param to file
	 * @return <code>true</code> if the state was restored, <code>false</code> if from file does not exist.
	 * @throws GistRepositoryError if file could not be restored
	 * @throws IllegalStateException if to file already exists
	 */
	private boolean restoreState(File from, File to) {
		Preconditions.checkState(!to.exists(), "Target file '" + to.getAbsolutePath() + "' must not exist");
		if(from.exists()) {
			try {
				Files.move(from.toPath(), to.toPath(), StandardCopyOption.ATOMIC_MOVE);
				logger.warn("{} recreated state from working copy.", from);
				return true;
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.ERR_METADATA_NOT_READABLE, "Could not load comments from working copy for this gist");
				logger.error(error.getFormattedMessage() + " with path {}", to);
				throw new GistRepositoryError(error, e);
			}
		}
		return false;
	}

}
