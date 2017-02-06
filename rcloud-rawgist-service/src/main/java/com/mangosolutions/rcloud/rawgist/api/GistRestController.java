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
	private GistRepository repository;
	
	@RequestMapping(value = "/all", method=RequestMethod.GET)
	public void listGists() {
		repository.listGists();
	}
	
	@RequestMapping(value = "/{gistId}", method=RequestMethod.GET)
	public void getGist(@PathVariable("gistId") String gistId) {
		repository.getGist(gistId);
	}
	
	@RequestMapping(method=RequestMethod.POST)
	@ResponseStatus( HttpStatus.CREATED )
	public void createGist(@RequestBody GistRequest request) {
		repository.createGist(request);
	}
	
	@RequestMapping(method=RequestMethod.PATCH)
	public void editGist() {
		repository.editGist();
	}
	
	@RequestMapping(value = "/{gistId}", method=RequestMethod.DELETE)
	public void deleteGist(@PathVariable("gistId") String gistId) {
		repository.deleteGist(gistId);
	}
	
}
