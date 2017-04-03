package com.mangosolutions.rcloud.rawgist.repository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SimpleGistSecurityManager implements GistSecurityManager {

	@Override
	public boolean canRead(GistRepository repository, UserDetails userDetails) {
		return true;
	}

	@Override
	public boolean canWrite(GistRepository repository, UserDetails userDetails) {
		return AccessRights.WRITE == this.getAccessRights(repository, userDetails);
	}

	@Override
	public AccessRights getAccessRights(GistRepository repository, UserDetails userDetails) {
		boolean isOwner = userDetails.getUsername().equals(this.getOwner(repository));
		
		if(isOwner) {
			return AccessRights.WRITE;
		}
		
		return AccessRights.READ;
	}
	
	private String getOwner(GistRepository repository) {
		return repository.getMetadata().getOwner();
	}
	
	
}
