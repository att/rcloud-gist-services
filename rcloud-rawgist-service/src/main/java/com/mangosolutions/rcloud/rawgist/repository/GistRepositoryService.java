package com.mangosolutions.rcloud.rawgist.repository;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.model.GistComment;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;

public interface GistRepositoryService {

	public List<GistResponse> listGists();
	
	public GistResponse getGist(String gistId);
	
	public GistResponse createGist(GistRequest request, UserDetails user);
	
	public GistResponse editGist(String gistId, GistRequest request);
	
	public void deleteGist(String gistId);
	
	public List<GistCommentResponse> getComments(String gistId);

	public GistCommentResponse getComment(String gistId, long commentId);
	
	public GistCommentResponse createComment(String gistId, GistComment comment);
	
	public GistCommentResponse  editComment(String gistId, long commentId, GistComment comment);
	
	public void deleteComment(String gistId, long commentId);
	
}
