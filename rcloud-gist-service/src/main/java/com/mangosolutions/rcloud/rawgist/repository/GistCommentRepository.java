package com.mangosolutions.rcloud.rawgist.repository;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.model.GistComment;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;

public interface GistCommentRepository {

	List<GistCommentResponse> getComments(UserDetails userDetails);

	GistCommentResponse getComment(long commentId, UserDetails userDetails);

	GistCommentResponse createComment(GistComment comment, UserDetails user);

	GistCommentResponse editComment(long commentId, GistComment comment, UserDetails user);

	void deleteComment(long commentId, UserDetails userDetails);

}