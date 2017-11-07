/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.commons.spring.http.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AnonymousUserAuthorityResolver implements AuthorityResolver {

    public static final GrantedAuthority ANONYMOUS_AUTHORITY = new SimpleGrantedAuthority("ROLE_ANONYMOUS");

    @Override
    public GrantedAuthority resolve(String username) {
        return ANONYMOUS_AUTHORITY;
    }

}
