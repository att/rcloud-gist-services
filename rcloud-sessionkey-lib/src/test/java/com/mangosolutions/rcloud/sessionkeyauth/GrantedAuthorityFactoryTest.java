/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import java.util.Collection;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

public class GrantedAuthorityFactoryTest {

    @Test
    public void nullUsernameAnonymousAuthority() {
        Collection<GrantedAuthority> authorities = new GrantedAuthorityFactory().resolve(null);
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        authorities.contains(AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
    }

    @Test
    public void emptyUsernameAnonymousAuthority() {
        Collection<GrantedAuthority> authorities = new GrantedAuthorityFactory().resolve("");
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        authorities.contains(AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
    }

    @Test
    public void blankUsernameAnonymousAuthority() {
        Collection<GrantedAuthority> authorities = new GrantedAuthorityFactory().resolve("   ");
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        authorities.contains(AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
    }

    @Test
    public void usernameUserAuthority() {
        Collection<GrantedAuthority> authorities = new GrantedAuthorityFactory().resolve("adam");
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        authorities.contains(AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
        authorities.contains(UserAuthorityResolver.USER_AUTHORITY);
    }

}
