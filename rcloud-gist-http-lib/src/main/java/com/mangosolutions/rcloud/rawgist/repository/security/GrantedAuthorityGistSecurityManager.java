/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.commons.spring.http.security.UserAuthorityResolver;
import com.mangosolutions.rcloud.rawgist.repository.GistRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistSecurityManager;
import com.mangosolutions.rcloud.rawgist.repository.git.CollaborationDataStore;

public class GrantedAuthorityGistSecurityManager implements GistSecurityManager {

    private static final Set<GistAccessRight> READ_RIGHTS = new HashSet<>(
            Arrays.asList(GistAccessRight.READ, GistAccessRight.WRITE));
    private static final Set<GistAccessRight> WRITE_RIGHTS = new HashSet<>(Arrays.asList(GistAccessRight.WRITE));

    private CollaborationDataStore collaborationDataStore = new CollaborationDataStore();

    public GrantedAuthorityGistSecurityManager(CollaborationDataStore collaborationDataStore) {
        this.collaborationDataStore = collaborationDataStore;
    }

    @Override
    public boolean canRead(GistRepository repository, UserDetails userDetails) {
        return READ_RIGHTS.contains(this.getAccessRight(repository, userDetails));
    }

    @Override
    public boolean canWrite(GistRepository repository, UserDetails userDetails) {
        return WRITE_RIGHTS.contains(this.getAccessRight(repository, userDetails));
    }

    @Override
    public boolean isOwner(GistRepository repository, UserDetails userDetails) {
        return GistRole.OWNER.equals(this.getRole(repository, userDetails));
    }

    @Override
    public GistAccessRight getAccessRight(GistRepository repository, UserDetails userDetails) {
        GistAccessRight right = GistAccessRight.NONE;
        if (userDetails != null && repository != null) {
            right = calculateAccessRight(repository, userDetails);
        }
        return right;
    }

    private GistAccessRight calculateAccessRight(GistRepository repository, UserDetails userDetails) {
        GistAccessRight right = GistAccessRight.NONE;
        GistRole role = getRole(repository, userDetails);
        switch (role) {
        case OWNER:
            right = GistAccessRight.WRITE;
            break;
        case COLLABORATOR:
            right = GistAccessRight.WRITE;
            break;
        default:
            right = GistAccessRight.READ;
        }
        return right;
    }

    @Override
    public GistRole getRole(GistRepository repository, UserDetails userDetails) {
        GistRole role = GistRole.NONE;
        if (userDetails != null && repository != null) {
            role = calculateRole(repository, userDetails);
        }
        return role;
    }

    @Override
    public boolean canCreate(UserDetails userDetails) {
        return this.hasUserAuthority(userDetails);
    }

    @Override
    public boolean canCreateAs(UserDetails userDetails, String otherUser) {
        if (userDetails == null || otherUser == null) {
            return false;
        }
        return this.hasUserAuthority(userDetails)
                && (this.getCollaborators(otherUser).contains(userDetails.getUsername())
                        || userDetails.getUsername().equals(otherUser));
    }

    private GistRole calculateRole(GistRepository repository, UserDetails userDetails) {
        GistRole role = GistRole.NONE;
        if (hasOwnerRole(repository, userDetails)) {
            role = GistRole.OWNER;
        } else if (hasCollaboratorRole(repository, userDetails)) {
            role = GistRole.COLLABORATOR;
        }
        return role;
    }

    private boolean hasCollaboratorRole(GistRepository repository, UserDetails userDetails) {
        boolean collaborator = false;
        Collection<String> collaborations = getCollaborators(repository);
        if (hasUserAuthority(userDetails) && collaborations.contains(userDetails.getUsername())) {
            collaborator = true;
        }
        return collaborator;
    }

    private Collection<String> getCollaborators(GistRepository repository) {
        return this.getCollaborators(repository.getMetadata().getOwner());
    }

    private Collection<String> getCollaborators(String user) {
        return this.collaborationDataStore.getCollaborators(user);
    }

    private boolean hasOwnerRole(GistRepository repository, UserDetails userDetails) {
        boolean owner = false;
        if (hasUserAuthority(userDetails)) {
            String repositoryOwner = repository.getMetadata().getOwner();
            String username = userDetails.getUsername();
            owner = repositoryOwner.equals(username);
        }
        return owner;
    }

    private boolean hasUserAuthority(UserDetails userDetails) {
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        return authorities.contains(UserAuthorityResolver.USER_AUTHORITY);
    }

}
