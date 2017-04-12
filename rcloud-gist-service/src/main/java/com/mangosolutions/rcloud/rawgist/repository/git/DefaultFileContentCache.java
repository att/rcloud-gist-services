package com.mangosolutions.rcloud.rawgist.repository.git;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.mangosolutions.rcloud.rawgist.model.FileContent;

@Component
public class DefaultFileContentCache implements FileContentCache {

	@Override
	@Cacheable(value = "filecontentcache", key = "#contentId")
	public FileContent load(String contentId) {
		return null;
	}

	@Override
	@CachePut(cacheNames = "filecontentcache", key = "#contentId")
	public FileContent save(String contentId, FileContent content) {
		return content;
	}

}
