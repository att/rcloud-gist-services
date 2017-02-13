package com.mangosolutions.rcloud.rawgist.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(value = "/gists/{gistId}/comments", produces={ MediaType.APPLICATION_JSON_VALUE })
public class GistCommentRestController {

	@RequestMapping(method=RequestMethod.GET)
	public void getComments(@PathVariable("gistId") String gistId) {
		
	}
	
	@RequestMapping(value="/{commentId}", method=RequestMethod.GET)
	public void getComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") String commentId) {
		
	}
	
	@RequestMapping(method=RequestMethod.POST)
	@ResponseStatus( HttpStatus.CREATED )
	public void createComment(@PathVariable("gistId") String gistId) {
		
	}
	
	@RequestMapping(value="/{commentId}", method=RequestMethod.PATCH)
	public void editComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") String commentId) {
		
	}
	
	@RequestMapping(value="/{commentId}", method=RequestMethod.DELETE)
	@ResponseStatus( HttpStatus.NO_CONTENT )
	public void deleteComment(@PathVariable("gistId") String gistId, @PathVariable("commentId") String commentId) {
		
	}
	
}
