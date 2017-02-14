package com.mangosolutions.rcloud.rawgist.repository;

import java.io.File;

import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;

public interface GistRepository {

	File getGistRepositoryFolder();

	GistResponse getGist();

	GistResponse createGist(GistRequest request);

	GistResponse editGist(GistRequest request);

}
