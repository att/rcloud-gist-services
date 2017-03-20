package com.mangosolutions.rcloud.rawgist.api;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;

@RestController()
@RequestMapping(value = "/gists", produces={ MediaType.APPLICATION_JSON_VALUE })
public class GistRestController {

	@Autowired
	private GistRepositoryService repository;
	
	@Autowired
	private ControllerUrlResolver resolver;
	
	@RequestMapping(method=RequestMethod.GET)
	public List<GistResponse> listAllGists(@AuthenticationPrincipal User activeUser) {
		List<GistResponse> responses = repository.listGists(activeUser);
		decorateUrls(responses, activeUser);
		return responses;
	}
	
	
	@RequestMapping(value = "/public", method=RequestMethod.GET)
	public List<GistResponse> listPublicGists(@AuthenticationPrincipal User activeUser) {
		List<GistResponse> responses = repository.listGists(activeUser);
		decorateUrls(responses, activeUser);
		return responses;
	}
	
	@RequestMapping(value = "/{gistId}", method=RequestMethod.GET)
	public GistResponse getGist(@PathVariable("gistId") String gistId, @AuthenticationPrincipal User activeUser) {
		GistResponse response = repository.getGist(gistId, activeUser); 
		decorateUrls(response, activeUser);
		return response;
	}
	
	
	@RequestMapping(value = "/{gistId}/{commitId}", method=RequestMethod.GET)
	public GistResponse getGistAtCommit(@PathVariable("gistId") String gistId, @PathVariable("commitId") String commitId, @AuthenticationPrincipal User activeUser) {
		
		GistResponse response = repository.getGist(gistId, commitId, activeUser); 
		decorateUrls(response, activeUser);
		return response;
	}
	
	@RequestMapping(method=RequestMethod.POST)
	@ResponseStatus( HttpStatus.CREATED )
	public GistResponse createGist(@RequestBody GistRequest request, HttpServletRequest httpRequest, @AuthenticationPrincipal User activeUser) {
		GistResponse response = repository.createGist(request, activeUser);
		decorateUrls(response, activeUser);
		return response;
	}
	
	@RequestMapping(value = "/{gistId}", method=RequestMethod.PATCH)
	public GistResponse editGist(@PathVariable("gistId") String gistId, @RequestBody GistRequest request, @AuthenticationPrincipal User activeUser) {
		GistResponse response = repository.editGist(gistId, request, activeUser);
		decorateUrls(response, activeUser);
		return response;
	}
	
	@RequestMapping(value = "/{gistId}", method=RequestMethod.DELETE)
	@ResponseStatus( HttpStatus.NO_CONTENT )
	public void deleteGist(@PathVariable("gistId") String gistId, @AuthenticationPrincipal User activeUser) {
		repository.deleteGist(gistId, activeUser);
	}
	
	private void decorateUrls(Collection<GistResponse> gistResponses, User activeUser) {
		if(gistResponses != null) {
			for(GistResponse gistResponse: gistResponses) {
				this.decorateUrls(gistResponse, activeUser);
			}
		}
	}
	
	private void decorateUrls(GistResponse gistResponse, User activeUser) {
		if(gistResponse != null) {
			gistResponse.setUrl(resolver.getGistUrl(gistResponse.getId(), activeUser));
			gistResponse.setCommentsUrl(resolver.getCommentsUrl(gistResponse.getId(), activeUser));
		}
	}
	
}
