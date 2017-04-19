package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;

public interface RepositoryStorageLocator {

	File getStoragePath(String gistId);
	
}
