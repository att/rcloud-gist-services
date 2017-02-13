package com.mangosolutions.rcloud.rawgist.api;

import java.io.File;

public interface GistRepository {

	File getGistRepositoryFolder();

	GistResponse getGist();

	GistResponse createGist(GistRequest request);

	GistResponse editGist(GistRequest request);

}
