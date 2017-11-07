/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

import com.mangosolutions.rcloud.commons.spring.http.security.AnonymousUserAuthorityResolver;
import com.mangosolutions.rcloud.commons.spring.http.security.AuthorityResolver;
import com.mangosolutions.rcloud.commons.spring.http.security.GrantedAuthorityFactory;
import com.mangosolutions.rcloud.commons.spring.http.security.UserAuthorityResolver;
import com.mangosolutions.rcloud.rawgist.repository.security.CollaborationGrantedAuthority;
import com.mangosolutions.rcloud.rawgist.repository.security.CollaborationGrantedAuthorityResolver;

public class GrantedAuthorityFactoryTest {

    GrantedAuthorityFactory factory = new GrantedAuthorityFactory();

    @Before
    public void setup() {
        Collection<AuthorityResolver> authorityResolvers = Arrays.asList((AuthorityResolver) new AnonymousUserAuthorityResolver(),
                (AuthorityResolver) new UserAuthorityResolver(), (AuthorityResolver) new CollaborationGrantedAuthorityResolver());
        factory.setAuthorityResolvers(authorityResolvers);
    }

    @Test
    public void nullUsernameAnonymousAuthority() {
        Collection<GrantedAuthority> authorities = factory.resolve(null);
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        authorities.contains(AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
    }

    @Test
    public void emptyUsernameAnonymousAuthority() {
        Collection<GrantedAuthority> authorities = factory.resolve("");
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        authorities.contains(AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
    }

    @Test
    public void blankUsernameAnonymousAuthority() {
        Collection<GrantedAuthority> authorities = factory.resolve("   ");
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        authorities.contains(AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
    }

    @Test
    public void usernameUserAuthority() {
        Collection<GrantedAuthority> authorities = factory.resolve("adam");
        assertNotNull(authorities);
        assertEquals(3, authorities.size());
        authorities.contains(AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
        authorities.contains(UserAuthorityResolver.USER_AUTHORITY);
        authorities.contains(new CollaborationGrantedAuthority());
    }

}
