package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistErrorCode;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryError;

@Component
public class GistMetadataStore implements MetadataStore {

	private static final Logger logger = LoggerFactory.getLogger(GistMetadataStore.class);
	
	@Autowired
	private ObjectMapper objectMapper;
	
	
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	@Cacheable(value = "metadata", key = "#store.getAbsolutePath()")
	public GistMetadata load(File store) {
		GistMetadata metadata = null;
		if(store.exists()) {
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
	@CachePut(cacheNames = "metadata", key = "#store.getAbsolutePath()")
	public GistMetadata save(File store, GistMetadata metadata) {
		try {
			objectMapper.writeValue(store, metadata);
		} catch (IOException e) {
			GistError error = new GistError(GistErrorCode.ERR_METADATA_NOT_WRITEABLE, "Could not update metadata for gist {}", metadata.getId());
			logger.error(error.getFormattedMessage() + " with path {}", store);
			throw new GistRepositoryError(error, e);
		}
		return metadata;
	}

}
