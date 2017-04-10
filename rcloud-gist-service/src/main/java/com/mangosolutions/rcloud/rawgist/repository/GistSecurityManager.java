package com.mangosolutions.rcloud.rawgist.repository;

import org.springframework.security.core.userdetails.UserDetails;

public interface GistSecurityManager {

	public enum GistAccessRight {
		NONE, READ, WRITE
	}
	
	public enum GistRole {
		OWNER, COLLABORATOR
	}
	
	boolean canRead(GistRepository repository, UserDetails userDetails);
	
	boolean canWrite(GistRepository repository, UserDetails userDetails);
	
	GistAccessRight getAccessRight(GistRepository repository, UserDetails userDetails);
	
	boolean isOwner(GistRepository repository, UserDetails userDetails);

	GistRole getRole(GistRepository repository, UserDetails userDetails);

}
