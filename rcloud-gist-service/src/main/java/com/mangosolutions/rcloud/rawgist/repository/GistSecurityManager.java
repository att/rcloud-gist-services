package com.mangosolutions.rcloud.rawgist.repository;

import org.springframework.security.core.userdetails.UserDetails;

public interface GistSecurityManager {

	public enum AccessRights {
		NONE, READ, WRITE
	}
	
	boolean canRead(GistRepository repository, UserDetails userDetails);
	
	boolean canWrite(GistRepository repository, UserDetails userDetails);
	
	AccessRights getAccessRights(GistRepository repository, UserDetails userDetails);
	
}
