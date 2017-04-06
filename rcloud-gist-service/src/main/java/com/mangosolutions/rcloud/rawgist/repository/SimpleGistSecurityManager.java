package com.mangosolutions.rcloud.rawgist.repository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SimpleGistSecurityManager implements GistSecurityManager {

	private static final Set<AccessRights> READ_RIGHTS = new HashSet<>(Arrays.asList(AccessRights.READ, AccessRights.WRITE));
	private static final Set<AccessRights> WRITE_RIGHTS = new HashSet<>(Arrays.asList(AccessRights.WRITE));
	
	@Override
	public boolean canRead(GistRepository repository, UserDetails userDetails) {
		return READ_RIGHTS.contains(this.getAccessRights(repository, userDetails));
	}

	@Override
	public boolean canWrite(GistRepository repository, UserDetails userDetails) {
		return WRITE_RIGHTS.contains(this.getAccessRights(repository, userDetails));
	}

	@Override
	public AccessRights getAccessRights(GistRepository repository, UserDetails userDetails) {
		GistMetadata metadata = getMetaData(repository);
		
		if(this.canWrite(metadata, userDetails)) {
			return AccessRights.WRITE;
		}
		if(this.canRead(metadata, userDetails)) {
			return AccessRights.READ;
		}
		return AccessRights.NONE;
		
	}
	
	private boolean canRead(GistMetadata metadata, UserDetails userDetails) {
		boolean canRead = true;
		if(!canWrite(metadata, userDetails)) {
			canRead = metadata.isPublic();
		}
		return canRead;
	}
	
	private boolean canWrite(GistMetadata metadata, UserDetails userDetails) {
		return userDetails.getUsername().equals(metadata.getOwner());
	}

	private GistMetadata getMetaData(GistRepository repository) {
		return repository.getMetadata();
	}
	
	
}
