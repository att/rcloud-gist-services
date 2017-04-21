/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mangosolutions.rcloud.rawgist.model.Fork;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;

@RestController()
@RequestMapping(value = "/gists", produces = { 
		MediaType.APPLICATION_JSON_VALUE,
		"application/vnd.github.beta+json",
		"application/vnd.github.v3+json" })
public class GistRestController {

	private final Logger logger = LoggerFactory.getLogger(GistRestController.class);
	
	@Autowired
	private GistRepositoryService repository;

	@Autowired
	private ControllerUrlResolver resolver;

	@RequestMapping(method = RequestMethod.GET)
	public List<GistResponse> listAllGistsForUser(@AuthenticationPrincipal User activeUser) {
		List<GistResponse> responses = repository.listGists(activeUser);
		decorateUrls(responses, activeUser);
		return responses;
	}

	@RequestMapping(value = "/public", method = RequestMethod.GET)
	public List<GistResponse> listAllPublicGists() {
		return Collections.emptyList();
	}

	@RequestMapping(value = "/{gistId}", method = RequestMethod.GET)
	@Cacheable(value = "gists", key = "#gistId")
	public GistResponse getGist(@PathVariable("gistId") String gistId, @AuthenticationPrincipal User activeUser) {
		GistResponse response = repository.getGist(gistId, activeUser);
		decorateUrls(response, activeUser);
		return response;
	}

	@RequestMapping(value = "/{gistId}/{commitId}", method = RequestMethod.GET)
	@Cacheable(value = "gists", key="{ #gistId, #commitId }")
	public GistResponse getGistAtCommit(@PathVariable("gistId") String gistId,
			@PathVariable("commitId") String commitId, @AuthenticationPrincipal User activeUser) {

		GistResponse response = repository.getGist(gistId, commitId, activeUser);
		decorateUrls(response, activeUser);
		return response;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public GistResponse createGist(@RequestBody GistRequest request, HttpServletRequest httpRequest,
			@AuthenticationPrincipal User activeUser) {
		GistResponse response = repository.createGist(request, activeUser);
		decorateUrls(response, activeUser);
		return response;
	}
	
	@RequestMapping(value = "/{gistId}/forks", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public List<Fork> getForks(@PathVariable("gistId") String gistId,
			@AuthenticationPrincipal User activeUser) {
		
		List<Fork> forks = repository.getForks(gistId, activeUser);
		
		decorateUrls(forks, activeUser);
		return forks;
	}
	
	/**
	 * Legacy github mapping
	 */
	@RequestMapping(value = "/{gistId}/fork", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@Deprecated
	public List<Fork> legacyGetForks(@PathVariable("gistId") String gistId,
			@AuthenticationPrincipal User activeUser) {
		return this.getForks(gistId, activeUser);
	}
	
	
	@RequestMapping(value = "/{gistId}/forks", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@CacheEvict(cacheNames = "gists", key="#gistId")
	public ResponseEntity<GistResponse> forkGist(@PathVariable("gistId") String gistId,
			@AuthenticationPrincipal User activeUser) {
		// TODO need to add Location header to response for the new Gist
		GistResponse response = repository.forkGist(gistId, activeUser);
		String location = resolver.getGistUrl(response.getId(), activeUser);
		HttpHeaders headers = new HttpHeaders();
		try {
			headers.setLocation(new URI(location));
		} catch (URISyntaxException e) {
			logger.warn("Unable to set the location header with value {} for fork with id {} with error {}.", location, gistId, e.getMessage());
		}
		decorateUrls(response, activeUser);
		ResponseEntity<GistResponse> responseEntity = new ResponseEntity<>(response, headers, HttpStatus.CREATED);

		return responseEntity;
	}

	/**
	 * Legacy github mapping
	 */
	@RequestMapping(value = "/{gistId}/fork", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@CacheEvict(cacheNames = "gists", key="#gistId")
	@Deprecated
	public ResponseEntity<GistResponse> legacyForkGist(@PathVariable("gistId") String gistId,
			@AuthenticationPrincipal User activeUser) {
		return this.forkGist(gistId, activeUser);
	}

	@RequestMapping(value = "/{gistId}", method = RequestMethod.PATCH)
	@CachePut(cacheNames = "gists", key = "#gistId")
	public GistResponse editGist(@PathVariable("gistId") String gistId, @RequestBody GistRequest request,
			@AuthenticationPrincipal User activeUser) {
		GistResponse response = repository.editGist(gistId, request, activeUser);
		decorateUrls(response, activeUser);
		return response;
	}

	@RequestMapping(value = "/{gistId}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@CacheEvict(cacheNames = "gists", key="#gistId")
	public void deleteGist(@PathVariable("gistId") String gistId, @AuthenticationPrincipal User activeUser) {
		repository.deleteGist(gistId, activeUser);
	}

	private void decorateUrls(Collection<GistResponse> gistResponses, User activeUser) {
		if (gistResponses != null) {
			for (GistResponse gistResponse : gistResponses) {
				this.decorateUrls(gistResponse, activeUser);
			}
		}
	}

	private void decorateUrls(GistResponse gistResponse, User activeUser) {
		if (gistResponse != null) {
			gistResponse.setUrl(resolver.getGistUrl(gistResponse.getId(), activeUser));
			gistResponse.setCommentsUrl(resolver.getCommentsUrl(gistResponse.getId(), activeUser));
			gistResponse.setForksUrl(resolver.getForksUrl(gistResponse.getId(), activeUser));
		}
	}
	
	private void decorateUrls(List<Fork> forks, User activeUser) {
		for(Fork fork: forks) {
			String forkUrl = resolver.getGistUrl(fork.getId(), activeUser);
			fork.setUrl(forkUrl);
		}
		
	}

}
