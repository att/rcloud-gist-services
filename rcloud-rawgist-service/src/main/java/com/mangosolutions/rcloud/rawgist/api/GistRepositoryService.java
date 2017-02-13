package com.mangosolutions.rcloud.rawgist.api;


public interface GistRepositoryService {

	public void listGists();
	
	public GistResponse getGist(String gistId);
	
	public GistResponse createGist(GistRequest request);
	
	public GistResponse editGist(String gistId, GistRequest request);
	
	public void deleteGist(String gistId);
	
}
