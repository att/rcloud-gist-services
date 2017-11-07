/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.commons.spring.http.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public class GrantedAuthorityFactory {

    private Collection<AuthorityResolver> authorityResolvers = Arrays.asList(new AnonymousUserAuthorityResolver(),
            new UserAuthorityResolver());

    public Collection<GrantedAuthority> resolve(String username) {
        Collection<GrantedAuthority> authorities = new ArrayList<>(authorityResolvers.size());
        for (AuthorityResolver resolver : authorityResolvers) {
            GrantedAuthority authority = resolver.resolve(username);
            if (authority != null) {
                authorities.add(authority);
            }
        }
        return authorities;
    }

    public void setAuthorityResolvers(Collection<AuthorityResolver> authorityResolvers) {
        this.authorityResolvers = authorityResolvers;
    }

}
