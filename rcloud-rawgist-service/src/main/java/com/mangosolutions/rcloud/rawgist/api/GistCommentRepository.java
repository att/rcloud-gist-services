package com.mangosolutions.rcloud.rawgist.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GistCommentRepository {

	public static final String COMMENT_REPOSITORY_FOLDER = "comments";

	public static final String COMMENTS_FILE = "comments.json";

	private File commentRepositoryFolder;

	private File commentsFile;

	private String gistId;

	private ObjectMapper objectMapper;

	public GistCommentRepository(File gistRepository, String gistId, ObjectMapper objectMapper) {
		this.commentRepositoryFolder = new File(gistRepository, COMMENT_REPOSITORY_FOLDER);
		if (!commentRepositoryFolder.exists()) {
			try {
				FileUtils.forceMkdir(commentRepositoryFolder);
			} catch (IOException e) {
				throw new RuntimeException(
						"Could not initialise the comment folder" + commentRepositoryFolder.getAbsolutePath(), e);
			}
		}
		this.gistId = gistId;
		commentsFile = new File(commentRepositoryFolder, COMMENTS_FILE);
		this.objectMapper = objectMapper;
	}

	public List<GistCommentResponse> getComments() {
		return loadComments();
	}

	public GistCommentResponse getComment(long commentId) {
		List<GistCommentResponse> comments = this.loadComments();
		return this.findComment(comments, commentId);
	}

	public GistCommentResponse createComment(GistComment comment) {
		GistCommentResponse response = new GistCommentResponse();
		DateTime now = DateTime.now();
		response.setCreatedAt(now);
		response.setUpdatedAt(now);
		response.setBody(comment.getBody());
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

	public GistCommentResponse editComment(long commentId, GistComment comment) {
		List<GistCommentResponse> comments = this.loadComments();
		GistCommentResponse commentResponse = this.findComment(comments, commentId);
		if(commentResponse != null) {
			commentResponse.setBody(comment.getBody());
			commentResponse.setUpdatedAt(new DateTime());
		}
		this.saveComments(comments);
		return commentResponse;
	}

	public void deleteComment(long commentId) {
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
		return null;

	}

	private List<GistCommentResponse> loadComments() {
		List<GistCommentResponse> comments = new ArrayList<>();
		if (commentsFile.exists()) {
			try {
				comments = objectMapper.readValue(commentsFile, new TypeReference<List<GistCommentResponse>>() {
				});
			} catch (IOException e) {
				throw new RuntimeException("Could not read metadata file");
			}
		}
		return comments;
	}

	private void saveComments(List<GistCommentResponse> comments) {
		try {
			objectMapper.writeValue(commentsFile, comments);
		} catch (IOException e) {
			throw new RuntimeException("Could not read metadata file");
		}
	}

}
