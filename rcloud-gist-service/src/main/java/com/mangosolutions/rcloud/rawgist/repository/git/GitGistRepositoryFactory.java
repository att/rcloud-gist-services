package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangosolutions.rcloud.rawgist.repository.GistRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryFactory;

@Component
public class GitGistRepositoryFactory implements GistRepositoryFactory {

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private MetadataStore metadataStore; 
	
	@Autowired
	private CommentStore commentStore; 
	
	@Autowired
	private HistoryStore historyStore; 
	
	public ObjectMapper getMapper() {
		return mapper;
	}

	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public GistRepository getRepository(File folder) {
		return new GitGistRepository(folder, metadataStore, commentStore, historyStore);
	}
	
}
