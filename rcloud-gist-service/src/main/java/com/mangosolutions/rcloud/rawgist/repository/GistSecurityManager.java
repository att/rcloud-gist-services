/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository;

import org.springframework.security.core.userdetails.UserDetails;

public interface GistSecurityManager {

    public enum GistAccessRight {
        NONE, READ, WRITE
    }

    public enum GistRole {
        NONE, OWNER, COLLABORATOR
    }

    boolean canCreate(UserDetails userDetails);

    boolean canCreateAs(UserDetails userDetails, String otherUser);

    boolean canRead(GistRepository repository, UserDetails userDetails);

    boolean canWrite(GistRepository repository, UserDetails userDetails);

    GistAccessRight getAccessRight(GistRepository repository, UserDetails userDetails);

    boolean isOwner(GistRepository repository, UserDetails userDetails);

    GistRole getRole(GistRepository repository, UserDetails userDetails);

}
