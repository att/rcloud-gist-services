/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import com.mangosolutions.rcloud.rawgist.repository.git.CollaborationDataStore;
import com.mangosolutions.rcloud.sessionkeyauth.UserAuthorityResolver;

public class CollaborationGrantedAuthorityResolver extends UserAuthorityResolver {

    private CollaborationDataStore collaborationDataStore = new CollaborationDataStore();

    public CollaborationGrantedAuthorityResolver() {

    }

    public CollaborationGrantedAuthorityResolver(CollaborationDataStore collaborationDataStore) {
        this.collaborationDataStore = collaborationDataStore;
    }

    @Override
    public GrantedAuthority resolve(String username) {
        GrantedAuthority authority = super.resolve(username);
        if (authority != null) {
            String[] aliases = resolveAliases(username);
            return new CollaborationGrantedAuthority(aliases);
        }
        return null;
    }

    private String[] resolveAliases(String username) {
        Collection<String> aliasList = collaborationDataStore.getCollaborators(username);
        return aliasList.toArray(new String[aliasList.size()]);
    }

}
