package com.mangosolutions.rcloud.rawgist.api;


public interface GistRepository {

	public void listGists();
	
	public GistResponse getGist(String gistId);
	
	public GistResponse createGist(GistRequest request);
	
	public void editGist();
	
	public void deleteGist(String gistId);
	
}
