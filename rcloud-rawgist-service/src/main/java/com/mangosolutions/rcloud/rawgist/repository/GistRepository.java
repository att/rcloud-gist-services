package com.mangosolutions.rcloud.rawgist.repository;

import java.io.File;

import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;

public interface GistRepository {

	File getGistRepositoryFolder(UserDetails userDetails);

	GistResponse getGist(UserDetails userDetails);

	GistResponse createGist(GistRequest request, UserDetails userDetails);

	GistResponse editGist(GistRequest request, UserDetails userDetails);

}
