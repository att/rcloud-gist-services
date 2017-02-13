package com.mangosolutions.rcloud.rawgist.api;

import java.util.List;

public interface GistRepositoryService {

	public List<GistResponse> listGists();
	
	public GistResponse getGist(String gistId);
	
	public GistResponse createGist(GistRequest request);
	
	public GistResponse editGist(String gistId, GistRequest request);
	
	public void deleteGist(String gistId);
	
	public List<GistCommentResponse> getComments(String gistId);

	public GistCommentResponse getComment(String gistId, long commentId);
	
	public GistCommentResponse createComment(String gistId, GistComment comment);
	
	public GistCommentResponse  editComment(String gistId, long commentId, GistComment comment);
	
	public void deleteComment(String gistId, long commentId);
	
}
