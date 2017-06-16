/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

import com.mangosolutions.rcloud.sessionkeyauth.UserAuthorityResolver;

public class CollaborationGrantedAuthorityResolver extends UserAuthorityResolver {

    private Map<String, List<String>> collaborators = Collections.emptyMap();
    
    public CollaborationGrantedAuthorityResolver() {
        
    }
    
    public CollaborationGrantedAuthorityResolver(Map<String, List<String>> collaborators) {
        this.collaborators = collaborators;
    }
    
    @Override
    public GrantedAuthority resolve(String username) {
        GrantedAuthority authority = super.resolve(username);
        if(authority != null) {
            String[] aliases = resolveAliases(username);
            return new CollaborationGrantedAuthority(aliases);
        }
        return null;
    }

    private String[] resolveAliases(String username) {
        if(collaborators.containsKey(username)) {
            List<String> aliasList = collaborators.get(username);
            return aliasList.toArray(new String[aliasList.size()]);
        }
        return null;
    }

}
