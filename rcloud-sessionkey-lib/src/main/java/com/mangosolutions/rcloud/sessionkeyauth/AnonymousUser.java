/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public final class AnonymousUser extends User {

    private static final long serialVersionUID = 4410141395566525064L;

    public static final String ANONYMOUS_USER_NAME = "anonymous";

    public AnonymousUser() {
        this(Arrays.asList(AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY));
    }
    
    public AnonymousUser(Collection<? extends GrantedAuthority> authorities) {
        super(ANONYMOUS_USER_NAME, "", authorities);
    }
    
}
