package com.mangosolutions.rcloud.rawgist.api;


public interface GistRepository {

	public void listGists();
	
	public void getGist(String gistId);
	
	public void createGist(GistRequest request);
	
	public void editGist();
	
	public void deleteGist(String gistId);
	
}
