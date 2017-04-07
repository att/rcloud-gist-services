package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;

public interface MetadataStore {

	GistMetadata load(File store);
	
	GistMetadata save(File store, GistMetadata metadata);
	
}
