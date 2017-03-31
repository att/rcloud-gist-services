/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.api;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mangosolutions.rcloud.rawgist.model.GistComment;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;

@RestController()
@RequestMapping(value = "/gists/{gistId}/comments", produces={ MediaType.APPLICATION_JSON_VALUE })
@CacheConfig(cacheNames="comments")
public class GistCommentRestController {

	@Autowired
	private GistRepositoryService repository;

	@Autowired
	private ControllerUrlResolver resolver;

	@RequestMapping(method=RequestMethod.GET)
	@Cacheable(key="#gistId")
	public List<GistCommentResponse> getComments(@PathVariable("gistId") String gistId, @AuthenticationPrincipal User activeUser) {
		List<GistCommentResponse> comments = repository.getComments(gistId, activeUser);
		this.decorateUrls(comments, gistId, activeUser);
		return comments;
	}

	@RequestMapping(value="/{commentId}", method=RequestMethod.GET)
	@Cacheable(key="{#gistId, #commentId}")
	public GistCommentResponse getComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId, @AuthenticationPrincipal User activeUser) {
		GistCommentResponse response = repository.getComment(gistId, commentId, activeUser);
		this.decorateUrls(response, gistId, activeUser);
		return response;
	}

	@RequestMapping(method=RequestMethod.POST)
	@ResponseStatus( HttpStatus.CREATED )
	@CacheEvict(key="#gistId")
	public GistCommentResponse createComment(@PathVariable("gistId") String gistId, @RequestBody GistComment comment, @AuthenticationPrincipal User activeUser) {
		GistCommentResponse response = repository.createComment(gistId, comment, activeUser);
		this.decorateUrls(response, gistId, activeUser);
		return response;
	}

	@RequestMapping(value="/{commentId}", method=RequestMethod.PATCH)
	@CachePut(key="{#gistId, #commentId}")
	@Caching(evict = @CacheEvict(key="#gistId"), put = @CachePut(key="{#gistId, #commentId}"))
	public GistCommentResponse editComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId, @RequestBody GistComment comment, @AuthenticationPrincipal User activeUser) {
		GistCommentResponse response = repository.editComment(gistId, commentId, comment, activeUser);
		this.decorateUrls(response, gistId, activeUser);
		return response;
	}

	@RequestMapping(value="/{commentId}", method=RequestMethod.DELETE)
	@ResponseStatus( HttpStatus.NO_CONTENT )
	@Caching(evict = { @CacheEvict(key="#gistId"), @CacheEvict(key="{#gistId, #commentId}") })
	public void deleteComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId, @AuthenticationPrincipal User activeUser) {
		repository.deleteComment(gistId, commentId, activeUser);
	}

	private void decorateUrls(Collection<GistCommentResponse> gistCommentResponses, String gistId, User activeUser) {
		if(gistCommentResponses != null) {
			for(GistCommentResponse gistResponse: gistCommentResponses) {
				this.decorateUrls(gistResponse, gistId, activeUser);
			}
		}
	}

	private void decorateUrls(GistCommentResponse gistCommentResponse, String gistId, User activeUser) {
		if(gistCommentResponse != null) {
			gistCommentResponse.setUrl(resolver.getCommentUrl(gistId, gistCommentResponse.getId(), activeUser));
		}
	}

}
