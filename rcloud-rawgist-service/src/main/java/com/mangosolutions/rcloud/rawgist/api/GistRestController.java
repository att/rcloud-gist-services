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
	
	@RequestMapping(value = "/public", method=RequestMethod.GET)
	public List<GistResponse> listGists() {
		List<GistResponse> responses = repository.listGists();
		decorateUrls(responses);
		return responses;
	}
	
	@RequestMapping(value = "/{gistId}", method=RequestMethod.GET)
	public GistResponse getGist(@PathVariable("gistId") String gistId) {
		GistResponse response = repository.getGist(gistId); 
		decorateUrls(response);
		return response;
	}
	
	@RequestMapping(method=RequestMethod.POST)
	@ResponseStatus( HttpStatus.CREATED )
	public GistResponse createGist(@RequestBody GistRequest request) {
		GistResponse response = repository.createGist(request);
		decorateUrls(response);
		return response;
	}
	
	@RequestMapping(value = "/{gistId}", method=RequestMethod.PATCH)
	public GistResponse editGist(@PathVariable("gistId") String gistId, @RequestBody GistRequest request) {
		GistResponse response = repository.editGist(gistId, request);
		decorateUrls(response);
		return response;
	}
	
	@RequestMapping(value = "/{gistId}", method=RequestMethod.DELETE)
	@ResponseStatus( HttpStatus.NO_CONTENT )
	public void deleteGist(@PathVariable("gistId") String gistId) {
		repository.deleteGist(gistId);
	}
	
	private void decorateUrls(Collection<GistResponse> gistResponses) {
		if(gistResponses != null) {
			for(GistResponse gistResponse: gistResponses) {
				this.decorateUrls(gistResponse);
			}
		}
	}
	
	private void decorateUrls(GistResponse gistResponse) {
		if(gistResponse != null) {
			gistResponse.setUrl(resolver.getGistUrl(gistResponse.getId()));
			gistResponse.setCommentsUrl(resolver.getCommentsUrl(gistResponse.getId()));
		}
	}
	
}
