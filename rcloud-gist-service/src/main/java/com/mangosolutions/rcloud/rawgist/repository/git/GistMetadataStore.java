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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistErrorCode;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryError;

@Component
public class GistMetadataStore implements MetadataStore {

	private static final Logger logger = LoggerFactory.getLogger(GistMetadataStore.class);

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${gists.metadatastore.workingCopySuffix:.tmp}")
	private String workingCopySuffix = ".tmp";

	public GistMetadataStore() {
		this.objectMapper = new ObjectMapper();
	}

	@Autowired
	public GistMetadataStore(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	@Override
	@Cacheable(value = "metadatastore", key = "#store.getAbsolutePath()")
	public GistMetadata load(File store) {
		GistMetadata metadata = null;
		File workingCopy = workingCopyFor(store);
		if(store.exists() || loadState(workingCopy, store)) {
			try {
				metadata = objectMapper.readValue(store, GistMetadata.class);
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.ERR_METADATA_NOT_READABLE, "Could not read metadata for this gist");
				logger.error(error.getFormattedMessage() + " with path {}", store);
				throw new GistRepositoryError(error, e);
			}
		} else {
			metadata = new GistMetadata();
		}
		return metadata;
	}

	@Override
	@CachePut(cacheNames = "metadatastore", key = "#store.getAbsolutePath()")
	public GistMetadata save(File store, GistMetadata metadata) {
		try {
			File workingCopy = workingCopyFor(store);
			if(!store.exists()) {
				loadState(workingCopy, store);
			}
			write(workingCopy, metadata);
			if(store.exists()) {
				Files.delete(store.toPath());
			}
			Files.move(workingCopy.toPath(), store.toPath(), StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			GistError error = new GistError(GistErrorCode.ERR_METADATA_NOT_WRITEABLE, "Could not update metadata for gist {}", metadata.getId());
			logger.error(error.getFormattedMessage() + " with path {}", store);
			throw new GistRepositoryError(error, e);
		}
		return metadata;
	}


	private File workingCopyFor(File store) {
		return new File(store.getParent(), store.getName() + workingCopySuffix);
	}
	
	private boolean loadState(File from, File to) {
		Preconditions.checkState(!to.exists(), "Target file '" + to.getAbsolutePath() + "' must not exist");
		if(from.exists()) {
			try {
				Files.move(from.toPath(), to.toPath(), StandardCopyOption.ATOMIC_MOVE);
				logger.warn("{} recreated state from working copy.", from);
				return true;
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.ERR_METADATA_NOT_READABLE, "Could not load metadata from working copy for this gist");
				logger.error(error.getFormattedMessage() + " with path {}", to);
				throw new GistRepositoryError(error, e);
			}
		}
		return false;
	}
	
	private void write(File output, GistMetadata metadata) throws IOException {
		try {
			objectMapper.writeValue(output, metadata);
		} catch(IOException e) {
			if(output.exists()) {
				Files.delete(output.toPath());
			}
			GistError error = new GistError(GistErrorCode.ERR_METADATA_NOT_WRITEABLE, "Could not update metadata for gist {}", metadata.getId());
			logger.error(error.getFormattedMessage() + " with path {}", output);
			throw new GistRepositoryError(error, e);
		}
	}
}
