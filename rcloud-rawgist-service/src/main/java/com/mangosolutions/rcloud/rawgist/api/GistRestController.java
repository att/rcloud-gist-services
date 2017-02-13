package com.mangosolutions.rcloud.rawgist.api;

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
@RequestMapping(value = "/gists", produces={ MediaType.APPLICATION_JSON_VALUE })
public class GistRestController {

	@Autowired
	private GistRepositoryService repository;
	
	@RequestMapping(value = "/public", method=RequestMethod.GET)
	public void listGists() {
		repository.listGists();
	}
	
	@RequestMapping(value = "/{gistId}", method=RequestMethod.GET)
	public GistResponse getGist(@PathVariable("gistId") String gistId) {
		return repository.getGist(gistId);
	}
	
	@RequestMapping(method=RequestMethod.POST)
	@ResponseStatus( HttpStatus.CREATED )
	public GistResponse createGist(@RequestBody GistRequest request) {
		return repository.createGist(request);
	}
	
	@RequestMapping(value = "/{gistId}", method=RequestMethod.PATCH)
	public GistResponse editGist(@PathVariable("gistId") String gistId, @RequestBody GistRequest request) {
		return repository.editGist(gistId, request);
	}
	
	@RequestMapping(value = "/{gistId}", method=RequestMethod.DELETE)
	@ResponseStatus( HttpStatus.NO_CONTENT )
	public void deleteGist(@PathVariable("gistId") String gistId) {
		repository.deleteGist(gistId);
	}
	
}
