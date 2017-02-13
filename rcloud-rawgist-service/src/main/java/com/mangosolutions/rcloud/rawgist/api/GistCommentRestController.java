package com.mangosolutions.rcloud.rawgist.api;

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

@RestController()
@RequestMapping(value = "/gists/{gistId}/comments", produces={ MediaType.APPLICATION_JSON_VALUE })
public class GistCommentRestController {

	@Autowired
	private GistRepositoryService repository;
	
	@RequestMapping(method=RequestMethod.GET)
	public List<GistCommentResponse> getComments(@PathVariable("gistId") String gistId) {
		return repository.getComments(gistId);
	}
	
	@RequestMapping(value="/{commentId}", method=RequestMethod.GET)
	public GistCommentResponse getComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId) {
		return repository.getComment(gistId, commentId);
	}
	
	@RequestMapping(method=RequestMethod.POST)
	@ResponseStatus( HttpStatus.CREATED )
	public GistCommentResponse createComment(@PathVariable("gistId") String gistId, @RequestBody GistComment comment) {
		return repository.createComment(gistId, comment);
	}
	
	@RequestMapping(value="/{commentId}", method=RequestMethod.PATCH)
	public GistCommentResponse editComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId, @RequestBody GistComment comment) {
		return repository.editComment(gistId, commentId, comment);
	}
	
	@RequestMapping(value="/{commentId}", method=RequestMethod.DELETE)
	@ResponseStatus( HttpStatus.NO_CONTENT )
	public void deleteComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") long commentId) {
		repository.deleteComment(gistId, commentId);
	}
	
}
