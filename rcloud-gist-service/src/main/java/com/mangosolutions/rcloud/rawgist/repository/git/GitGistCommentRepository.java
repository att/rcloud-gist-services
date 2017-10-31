/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.model.GistComment;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.model.GistIdentity;
import com.mangosolutions.rcloud.rawgist.repository.GistCommentRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistErrorCode;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryException;

public class GitGistCommentRepository implements GistCommentRepository, Serializable {

    private static final long serialVersionUID = 414766810805325462L;

    private static final Logger logger = LoggerFactory.getLogger(GitGistCommentRepository.class);

    private File commentsFile;

    private CommentStore commentStore;

    public GitGistCommentRepository(File commentsFile, CommentStore commentStore) {
        this.commentsFile = commentsFile;
        this.commentStore = commentStore;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.mangosolutions.rcloud.rawgist.repository.IGistCommentRepository#
     * getComments()
     */
    @Override
    public List<GistCommentResponse> getComments(UserDetails activeUser) {
        return loadComments();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.mangosolutions.rcloud.rawgist.repository.IGistCommentRepository#
     * getComment(long)
     */
    @Override
    public GistCommentResponse getComment(long commentId, UserDetails activeUser) {
        List<GistCommentResponse> comments = this.loadComments();
        return this.findComment(comments, commentId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.mangosolutions.rcloud.rawgist.repository.IGistCommentRepository#
     * createComment(com.mangosolutions.rcloud.rawgist.model.GistComment,
     * org.springframework.security.core.userdetails.UserDetails)
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
        if (!comments.isEmpty()) {
            id = comments.get(comments.size() - 1).getId() + 1;
        }
        response.setId(id);
        comments.add(response);
        this.saveComments(comments);
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.mangosolutions.rcloud.rawgist.repository.IGistCommentRepository#
     * editComment(long, com.mangosolutions.rcloud.rawgist.model.GistComment,
     * org.springframework.security.core.userdetails.UserDetails)
     */
    @Override
    public GistCommentResponse editComment(long commentId, GistComment comment, UserDetails user) {
        List<GistCommentResponse> comments = this.loadComments();
        GistCommentResponse commentResponse = this.findComment(comments, commentId);
        if (commentResponse != null) {
            commentResponse.setBody(comment.getBody());
            commentResponse.setUpdatedAt(new DateTime());
        }
        this.saveComments(comments);
        return commentResponse;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.mangosolutions.rcloud.rawgist.repository.IGistCommentRepository#
     * deleteComment(long)
     */
    @Override
    public void deleteComment(long commentId, UserDetails activeUser) {
        List<GistCommentResponse> comments = this.loadComments();
        GistCommentResponse comment = this.findComment(comments, commentId);
        if (comment != null) {
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

        GistError error = new GistError(GistErrorCode.ERR_COMMENT_NOT_EXIST, "Comment with id {} does not exist", id);
        logger.warn(error.getFormattedMessage());
        throw new GistRepositoryException(error);

    }

    private List<GistCommentResponse> loadComments() {
        return this.commentStore.load(this.commentsFile);
    }

    private void saveComments(List<GistCommentResponse> comments) {
        this.commentStore.save(this.commentsFile, comments);
    }

}
