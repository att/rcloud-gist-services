/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.repository.GistRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistSecurityManager;
import com.mangosolutions.rcloud.rawgist.repository.GistSecurityManager.GistAccessRight;
import com.mangosolutions.rcloud.rawgist.repository.GistSecurityManager.GistRole;
import com.mangosolutions.rcloud.rawgist.repository.git.GistMetadata;
import com.mangosolutions.rcloud.sessionkeyauth.AnonymousUser;
import com.mangosolutions.rcloud.sessionkeyauth.AnonymousUserAuthorityResolver;
import com.mangosolutions.rcloud.sessionkeyauth.UserAuthorityResolver;

public class GrantedAuthorityGistSecurityManagerTest {

    private static final String OWNER_NAME = "owner_name";
    private static final String NOT_OWNER_NAME = "not_owner_name";
    
    private GistRepository mockGistRepository;

    private GistMetadata metadata;
    
    private GistSecurityManager securityManager;

    @Before
    public void setup() {
        mockGistRepository = mock(GistRepository.class);
        metadata = new GistMetadata();
        when(mockGistRepository.getMetadata()).thenReturn(metadata);
        metadata.setOwner(OWNER_NAME);
        securityManager = new GrantedAuthorityGistSecurityManager();
    }
    
    //User tests

    @Test
    public void testUserNotOwnerIsNotOwner() {
        UserDetails userDetails = createNotOwnerUserDetails();
        boolean isOwner = securityManager.isOwner(mockGistRepository, userDetails);
        assertFalse(isOwner);
    }
    
    @Test
    public void testUserNotOwnerShouldHaveReadAccess() {
        UserDetails userDetails = createNotOwnerUserDetails();
        boolean canRead = securityManager.canRead(mockGistRepository, userDetails);
        assertTrue(canRead);
    }

    @Test
    public void testUserNotOwnerShouldNotHaveWriteAccess() {
        UserDetails userDetails = createNotOwnerUserDetails();
        boolean canWrite = securityManager.canWrite(mockGistRepository, userDetails);
        assertFalse(canWrite);
    }

    @Test
    public void testUserNotOwnerShouldHaveNoneRole() {
        UserDetails userDetails = createNotOwnerUserDetails();
        GistRole role = securityManager.getRole(mockGistRepository, userDetails);
        assertEquals(GistRole.NONE, role);
    }

    @Test
    public void testUserNotOwnerShouldHaveReadAccessRight() {
        UserDetails userDetails = createNotOwnerUserDetails();
        GistAccessRight accessRight = securityManager.getAccessRight(mockGistRepository, userDetails);
        assertEquals(GistAccessRight.READ, accessRight);
    }
    
    @Test
    public void testUserCanCreateGist() {
        UserDetails userDetails = createNotOwnerUserDetails();
        boolean canCreate = securityManager.canCreate(userDetails);
        assertTrue(canCreate);
    }
    
    //Owner tests
    
    @Test
    public void testUserIsOwnerIsOwner() {
        UserDetails userDetails = createOwnerUserDetails();
        boolean isOwner = securityManager.isOwner(mockGistRepository, userDetails);
        assertTrue(isOwner);
    }
    
    @Test
    public void testUserIsOwnerShouldHaveReadAccess() {
        UserDetails userDetails = createOwnerUserDetails();
        boolean canRead = securityManager.canRead(mockGistRepository, userDetails);
        assertTrue(canRead);
    }

    
    @Test
    public void testUserIsOwnerShouldHaveWriteAccess() {
        UserDetails userDetails = createOwnerUserDetails();
        boolean canWrite = securityManager.canWrite(mockGistRepository, userDetails);
        assertTrue(canWrite);
    }

    @Test
    public void testUserIsOwnerShouldHaveWriteAccessRight() {
        UserDetails userDetails = createOwnerUserDetails();
        GistAccessRight accessRight = securityManager.getAccessRight(mockGistRepository, userDetails);
        assertEquals(GistAccessRight.WRITE, accessRight);
    }

    @Test
    public void testUserIsOwnerShouldOwnerRole() {
        UserDetails userDetails = createOwnerUserDetails();
        GistRole role = securityManager.getRole(mockGistRepository, userDetails);
        assertEquals(GistRole.OWNER, role);
    }
    
    //Collaborator tests
    
    @Test
    public void testUserIsCollaboratorIsNotOwner() {
        UserDetails userDetails = createCollaboratorUserDetails();
        boolean isOwner = securityManager.isOwner(mockGistRepository, userDetails);
        assertFalse(isOwner);
    }
    
    @Test
    public void testUserIsCollaboratorShouldHaveReadAccess() {
        UserDetails userDetails = createCollaboratorUserDetails();
        boolean canRead = securityManager.canRead(mockGistRepository, userDetails);
        assertTrue(canRead);
    }

    
    @Test
    public void testUserIsCollaboratorShouldHaveWriteAccess() {
        UserDetails userDetails = createCollaboratorUserDetails();
        boolean canWrite = securityManager.canWrite(mockGistRepository, userDetails);
        assertTrue(canWrite);
    }

    @Test
    public void testUserIsCollaboratorShouldHaveWriteAccessRight() {
        UserDetails userDetails = createCollaboratorUserDetails();
        GistAccessRight accessRight = securityManager.getAccessRight(mockGistRepository, userDetails);
        assertEquals(GistAccessRight.WRITE, accessRight);
    }

    @Test
    public void testUserIsCollaboratorShouldHaveCollaboratorRole() {
        UserDetails userDetails = createCollaboratorUserDetails();
        GistRole role = securityManager.getRole(mockGistRepository, userDetails);
        assertEquals(GistRole.COLLABORATOR, role);
    }
    
    @Test
    public void testCollaboratorCanCreateGist() {
        UserDetails userDetails = createCollaboratorUserDetails();
        boolean canCreate = securityManager.canCreate(userDetails);
        assertTrue(canCreate);
    }
    
    @Test
    public void testCollaboratorCanCreateGistAsUser() {
        UserDetails userDetails = createCollaboratorUserDetails();
        boolean canCreate = securityManager.canCreateAs(userDetails, OWNER_NAME);
        assertTrue(canCreate);
    }
    
    @Test
    public void testCollaboratorCannotCreateGistAsUser() {
        UserDetails userDetails = createCollaboratorUserDetails();
        boolean canCreate = securityManager.canCreateAs(userDetails, "non_collab_user");
        assertFalse(canCreate);
    }
    
    @Test
    public void testCollaboratorCanCreateGistAsThemself() {
        UserDetails userDetails = createCollaboratorUserDetails();
        boolean canCreate = securityManager.canCreateAs(userDetails, NOT_OWNER_NAME);
        assertTrue(canCreate);
    }
    
    //Anonymous tests

    @Test
    public void testAnonymousUserShouldHaveReadAccess() {
        UserDetails userDetails = createAnonymousUserDetails();
        boolean canRead = securityManager.canRead(mockGistRepository, userDetails);
        assertTrue(canRead);
    }

    @Test
    public void testAnonymousUserShouldNotHaveWriteAccess() {
        UserDetails userDetails = createAnonymousUserDetails();
        boolean canWrite = securityManager.canWrite(mockGistRepository, userDetails);
        assertFalse(canWrite);
    }
    
    @Test
    public void testAnonymousUserShouldHaveNoneRole() {
        UserDetails userDetails = createAnonymousUserDetails();
        GistRole role = securityManager.getRole(mockGistRepository, userDetails);
        assertEquals(GistRole.NONE, role);
    }
    
    @Test
    public void testAnonymousUserShouldHaveReadAccessRight() {
        UserDetails userDetails = createAnonymousUserDetails();
        GistAccessRight accessRight = securityManager.getAccessRight(mockGistRepository, userDetails);
        assertEquals(GistAccessRight.READ, accessRight);
    }
    
    @Test
    public void testAnonymousUserWithOwnerNameAnonymousShouldNotHaveWriteAccess() {
        UserDetails userDetails = createAnonymousUserDetails();
        this.metadata.setOwner(AnonymousUser.ANONYMOUS_USER_NAME);
        boolean canWrite = securityManager.canWrite(mockGistRepository, userDetails);
        assertFalse(canWrite);
    }
    
    @Test
    public void testAnonymousUserWithOwnerNameAnonymousShouldHaveReadAccessRight() {
        UserDetails userDetails = createAnonymousUserDetails();
        this.metadata.setOwner(AnonymousUser.ANONYMOUS_USER_NAME);
        GistAccessRight accessRight = securityManager.getAccessRight(mockGistRepository, userDetails);
        assertEquals(GistAccessRight.READ, accessRight);
    }
    
    @Test
    public void testAnonymousUserWithOwnerNameAnonymousShouldHaveNoneRole() {
        UserDetails userDetails = createAnonymousUserDetails();
        this.metadata.setOwner(AnonymousUser.ANONYMOUS_USER_NAME);
        GistRole role = securityManager.getRole(mockGistRepository, userDetails);
        assertEquals(GistRole.NONE, role);
    }
    
    @Test
    public void testAnonymousUserCannotCreateGist() {
        UserDetails userDetails = createAnonymousUserDetails();
        boolean canCreate = securityManager.canCreate(userDetails);
        assertFalse(canCreate);
    }
    
    // negative testing
    
    @Test
    public void anonymousUserWithCollaborationAuthorityShouldNotHaveWriteAccess() {
        CollaborationGrantedAuthority collaboratorGrantedAuthority = new CollaborationGrantedAuthority(new String[]{OWNER_NAME});
        Collection<GrantedAuthority> authorities = Arrays.asList(collaboratorGrantedAuthority, AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
        UserDetails userDetails = createUserDetails("anonymous", authorities);
        boolean canWrite = securityManager.canWrite(mockGistRepository, userDetails);
        assertFalse(canWrite);
    }
    
    @Test
    public void anonymousUserWithCollaborationAuthorityShouldHaveReadAccess() {
        CollaborationGrantedAuthority collaboratorGrantedAuthority = new CollaborationGrantedAuthority(new String[]{OWNER_NAME});
        Collection<GrantedAuthority> authorities = Arrays.asList(collaboratorGrantedAuthority, AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
        UserDetails userDetails = createUserDetails("anonymous", authorities);
        boolean canRead = securityManager.canRead(mockGistRepository, userDetails);
        assertTrue(canRead);
    }
    
    @Test
    public void anonymousUserWithCollaborationAuthorityShouldHaveNoneRole() {
        CollaborationGrantedAuthority collaboratorGrantedAuthority = new CollaborationGrantedAuthority(new String[]{OWNER_NAME});
        Collection<GrantedAuthority> authorities = Arrays.asList(collaboratorGrantedAuthority, AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
        UserDetails userDetails = createUserDetails("anonymous", authorities);
        GistRole role = securityManager.getRole(mockGistRepository, userDetails);
        assertEquals(GistRole.NONE, role);
    }
    
    @Test
    public void testNullUserCannotRead() {
        boolean canRead = securityManager.canRead(mockGistRepository, null);
        assertFalse(canRead);
    }
    
    public void testNullUserCannotWrite() {
        boolean canWrite = securityManager.canWrite(mockGistRepository, null);
        assertFalse(canWrite);
    }
    
    public void testNullUserHasNoneAccessRight() {
        GistAccessRight accessRight = securityManager.getAccessRight(mockGistRepository, null);
        assertEquals(GistAccessRight.NONE, accessRight);
    }
    
    public void testNullUserHasNoneRole() {
        GistRole role = securityManager.getRole(mockGistRepository, null);
        assertEquals(GistRole.NONE, role);
    }
    
    private UserDetails createOwnerUserDetails() {
        return createUserDetails(OWNER_NAME);
    }
    
    private UserDetails createNotOwnerUserDetails() {
        return createUserDetails(NOT_OWNER_NAME);
    }

    private UserDetails createUserDetails(String name) {
        Collection<GrantedAuthority> authorities = Arrays.asList(UserAuthorityResolver.USER_AUTHORITY, AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
        return createUserDetails(name, authorities);
    }
    
    private UserDetails createUserDetails(String name, Collection<GrantedAuthority> authorities) {
        return new User(name, "", authorities);
    }
    
    private UserDetails createAnonymousUserDetails() {
        return new AnonymousUser(Arrays.asList((GrantedAuthority)AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY));
    }
    
    private UserDetails createCollaboratorUserDetails() {
        CollaborationGrantedAuthority collaboratorGrantedAuthority = new CollaborationGrantedAuthority(new String[]{OWNER_NAME});
        Collection<GrantedAuthority> authorities = Arrays.asList(UserAuthorityResolver.USER_AUTHORITY, collaboratorGrantedAuthority, AnonymousUserAuthorityResolver.ANONYMOUS_AUTHORITY);
        return createUserDetails(NOT_OWNER_NAME, authorities);
    }
    
}
