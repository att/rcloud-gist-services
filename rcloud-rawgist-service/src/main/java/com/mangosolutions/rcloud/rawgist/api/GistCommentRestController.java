package com.mangosolutions.rcloud.rawgist.api;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class GistCommentRestController {

	@Autowired
	private GistRepositoryService repository;
	
	@Autowired
	private ControllerUrlResolver resolver;
	
	@RequestMapping(method=RequestMethod.GET)
	public List<GistCommentResponse> getComments(@PathVariable("gistId") String gistId) {
		List<GistCommentResponse> comments = repository.getComments(gistId);
		this.decorateUrls(comments, gistId);
		return comments;
	}
	
	@RequestMapping(value="/{commentId}", method=RequestMethod.GET)
	public GistCommentResponse getComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId) {
		GistCommentResponse response = repository.getComment(gistId, commentId);
		this.decorateUrls(response, gistId);
		return response;
	}
	
	@RequestMapping(method=RequestMethod.POST)
	@ResponseStatus( HttpStatus.CREATED )
	public GistCommentResponse createComment(@PathVariable("gistId") String gistId, @RequestBody GistComment comment) {
		GistCommentResponse response = repository.createComment(gistId, comment);
		this.decorateUrls(response, gistId);
		return response;
	}
	
	@RequestMapping(value="/{commentId}", method=RequestMethod.PATCH)
	public GistCommentResponse editComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId, @RequestBody GistComment comment) {
		GistCommentResponse response = repository.editComment(gistId, commentId, comment);
		this.decorateUrls(response, gistId);
		return response;
	}
	
	@RequestMapping(value="/{commentId}", method=RequestMethod.DELETE)
	@ResponseStatus( HttpStatus.NO_CONTENT )
	public void deleteComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId) {
		repository.deleteComment(gistId, commentId);
	}
	
	private void decorateUrls(Collection<GistCommentResponse> gistCommentResponses, String gistId) {
		if(gistCommentResponses != null) {
			for(GistCommentResponse gistResponse: gistCommentResponses) {
				this.decorateUrls(gistResponse, gistId);
			}
		}
	}
	
	private void decorateUrls(GistCommentResponse gistCommentResponse, String gistId) {
		if(gistCommentResponse != null) {
			gistCommentResponse.setUrl(resolver.getCommentUrl(gistId, gistCommentResponse.getId()));
		}
	}
	
}
