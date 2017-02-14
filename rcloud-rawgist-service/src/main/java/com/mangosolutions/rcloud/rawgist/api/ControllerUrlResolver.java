package com.mangosolutions.rcloud.rawgist.api;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.stereotype.Component;

@Component
public class ControllerUrlResolver {

	public String getGistUrl(String gistId) {
		String url = null;
		if(gistId != null) {
			url = linkTo(
					methodOn(GistRestController.class)
					.getGist(gistId))
					.withSelfRel()
					.getHref();
			}
		return url;
	}
	
	public String getCommentsUrl(String gistId) {
		String url = null;
		if(gistId != null) {
			url = linkTo(
					methodOn(GistCommentRestController.class)
					.getComments(gistId))
					.withSelfRel()
					.getHref();
			}
		return url;
	}
	
	public String getCommentUrl(String gistId, Long commentId) {
		String url = null;
		if(gistId != null && commentId != null) {
			url = linkTo(
					methodOn(GistCommentRestController.class)
					.getComment(gistId, commentId))
					.withSelfRel()
					.getHref();
			}
		return url;
	}
	
}
