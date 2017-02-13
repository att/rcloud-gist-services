package com.mangosolutions.rcloud.rawgist.api;

import java.util.List;

public interface GistRepositoryService {

	public List<GistResponse> listGists();
	
	public GistResponse getGist(String gistId);
	
	public GistResponse createGist(GistRequest request);
	
	public GistResponse editGist(String gistId, GistRequest request);
	
	public void deleteGist(String gistId);
	
}
