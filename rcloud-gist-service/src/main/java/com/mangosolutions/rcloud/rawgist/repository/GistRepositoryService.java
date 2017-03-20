package com.mangosolutions.rcloud.rawgist.repository;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.model.GistComment;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;

public interface GistRepositoryService {

	public List<GistResponse> listGists(UserDetails activeUser);
	
	public GistResponse getGist(String gistId, UserDetails activeUser);
	
	public GistResponse getGist(String gistId, String commitId, UserDetails activeUser);
	
	public GistResponse createGist(GistRequest request, UserDetails user);
	
	public GistResponse editGist(String gistId, GistRequest request, UserDetails activeUser);
	
	public void deleteGist(String gistId, UserDetails activeUser);
	
	public List<GistCommentResponse> getComments(String gistId, UserDetails activeUser);

	public GistCommentResponse getComment(String gistId, long commentId, UserDetails activeUser);
	
	public GistCommentResponse createComment(String gistId, GistComment comment, UserDetails activeUser);
	
	public GistCommentResponse  editComment(String gistId, long commentId, GistComment comment, UserDetails activeUser);
	
	public void deleteComment(String gistId, long commentId, UserDetails activeUser);
	
}
