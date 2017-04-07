package com.mangosolutions.rcloud.rawgist.repository;

import java.io.File;

public interface GistRepositoryFactory {

	GistRepository getRepository(File folder);
	
}
