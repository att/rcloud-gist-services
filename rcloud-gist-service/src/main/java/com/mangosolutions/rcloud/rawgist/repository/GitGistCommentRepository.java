/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangosolutions.rcloud.rawgist.model.GistComment;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.model.GistIdentity;
import com.mangosolutions.rcloud.rawgist.repository.GistError.GistErrorCode;

public class GitGistCommentRepository implements GistCommentRepository {

	public static final String COMMENT_REPOSITORY_FOLDER = "comments";

	public static final String COMMENTS_FILE = "comments.json";

	private Logger logger = LoggerFactory.getLogger(GitGistCommentRepository.class);

	private File commentRepositoryFolder;

	private File commentsFile;

	private String gistId;

	private ObjectMapper objectMapper;

	public GitGistCommentRepository(File gistRepository, String gistId, ObjectMapper objectMapper) {
		this.gistId = gistId;
		this.commentRepositoryFolder = new File(gistRepository, COMMENT_REPOSITORY_FOLDER);
		if (!commentRepositoryFolder.exists()) {
			try {
				FileUtils.forceMkdir(commentRepositoryFolder);
			} catch (IOException e) {
				throw new GistRepositoryError(new GistError(GistErrorCode.FATAL_GIST_INITIALISATION, "Could not create comment repository for gist {}", this.gistId), e);
			}
		}
		commentsFile = new File(commentRepositoryFolder, COMMENTS_FILE);
		this.objectMapper = objectMapper;
	}

	/* (non-Javadoc)
	 * @see com.mangosolutions.rcloud.rawgist.repository.IGistCommentRepository#getComments()
	 */
	@Override
	public List<GistCommentResponse> getComments(UserDetails activeUser) {
		return loadComments();
	}

	/* (non-Javadoc)
	 * @see com.mangosolutions.rcloud.rawgist.repository.IGistCommentRepository#getComment(long)
	 */
	@Override
	public GistCommentResponse getComment(long commentId, UserDetails activeUser) {
		List<GistCommentResponse> comments = this.loadComments();
		return this.findComment(comments, commentId);
	}

	/* (non-Javadoc)
	 * @see com.mangosolutions.rcloud.rawgist.repository.IGistCommentRepository#createComment(com.mangosolutions.rcloud.rawgist.model.GistComment, org.springframework.security.core.userdetails.UserDetails)
	 */
	@Override
	public GistCommentResponse createComment(GistComment comment, UserDetails user) {
		GistCommentResponse response = new GistCommentResponse();
		DateTime now = DateTime.now();
		response.setCreatedAt(now);
		response.setUpdatedAt(now);
		response.setBody(comment.getBody());
		GistIdentity userIdentity = new GistIdentity();
		userIdentity.setLogin(user.getUsername());
		response.setUser(userIdentity);
		List<GistCommentResponse> comments = loadComments();
		long id = 1L;
		if(!comments.isEmpty()) {
			id = comments.get(comments.size() - 1).getId() + 1;
		}
		response.setId(id);
		comments.add(response);
		this.saveComments(comments);
		return response;
	}

	/* (non-Javadoc)
	 * @see com.mangosolutions.rcloud.rawgist.repository.IGistCommentRepository#editComment(long, com.mangosolutions.rcloud.rawgist.model.GistComment, org.springframework.security.core.userdetails.UserDetails)
	 */
	@Override
	public GistCommentResponse editComment(long commentId, GistComment comment, UserDetails user) {
		List<GistCommentResponse> comments = this.loadComments();
		GistCommentResponse commentResponse = this.findComment(comments, commentId);
		if(commentResponse != null) {
			commentResponse.setBody(comment.getBody());
			commentResponse.setUpdatedAt(new DateTime());
		}
		this.saveComments(comments);
		return commentResponse;
	}

	/* (non-Javadoc)
	 * @see com.mangosolutions.rcloud.rawgist.repository.IGistCommentRepository#deleteComment(long)
	 */
	@Override
	public void deleteComment(long commentId, UserDetails activeUser) {
		List<GistCommentResponse> comments = this.loadComments();
		GistCommentResponse comment = this.findComment(comments, commentId);
		if(comment != null) {
			comments.remove(comment);
		}
		this.saveComments(comments);
	}

	private GistCommentResponse findComment(List<GistCommentResponse> comments, long id) {
		final long commentId = id;
		if (comments != null) {
			return IterableUtils.find(comments, new Predicate<GistCommentResponse>() {
				public boolean evaluate(GistCommentResponse comment) {
					return comment.getId().equals(commentId);
				}
			});
		}

		GistError error = new GistError(GistErrorCode.ERR_COMMENT_NOT_EXIST, "Comment with id {} on gist {} does not exist",
				this.gistId, id);
		logger.warn(error.getFormattedMessage());
		throw new GistRepositoryException(error);

	}

	private List<GistCommentResponse> loadComments() {
		List<GistCommentResponse> comments = new ArrayList<>();
		if (commentsFile.exists()) {
			try {
				comments = objectMapper.readValue(commentsFile, new TypeReference<List<GistCommentResponse>>() {
				});
			} catch (IOException e) {
				throw new GistRepositoryError(new GistError(GistErrorCode.ERR_COMMENTS_NOT_READABLE, "Could not read metadata for gist {}", this.gistId), e);
			}
		}
		return comments == null? new ArrayList<GistCommentResponse>(): comments;
	}

	private void saveComments(List<GistCommentResponse> comments) {
		try {
			objectMapper.writeValue(commentsFile, comments);
		} catch (IOException e) {
			throw new GistRepositoryError(new GistError(GistErrorCode.ERR_COMMENTS_NOT_WRITEABLE, "Could not save metadata for gist {}", this.gistId), e);
		}
	}

}
