/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class SessionKeyServerUserDetailsServiceTest {

    private KeyServerConfiguration defaultConfig;
    private KeyServerConfiguration remoteConfig;
    private Map<String, KeyServerConfiguration> configs = new HashMap<>();
    private SessionKeyServerUserDetailsService detailsService;
    private SessionKeyServerAuthenticationDetails details;
    private SessionKeyServerService sessionKeyServerService;

    @Before
    public void setUp() {

        defaultConfig = new KeyServerConfiguration();
        configs.put("default", defaultConfig);
        remoteConfig = new KeyServerConfiguration();
        remoteConfig.setHost("remote.example.com");
        remoteConfig.setPort(9090);
        remoteConfig.setRealm("anotherRealm");
        configs.put("remote", remoteConfig);
        sessionKeyServerService = new SessionKeyServerService(configs);

        detailsService = new SessionKeyServerUserDetailsService(sessionKeyServerService);
        details = new SessionKeyServerAuthenticationDetails("default");
    }

    @Test
    public void testYesResponse() {
        RestTemplate restTemplate = sessionKeyServerService.getRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("YES\ntheuser\nthesource", MediaType.TEXT_PLAIN));
        PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("abc", null);
        token.setDetails(details);
        UserDetails userDetails = detailsService.loadUserDetails(token);
        Assert.assertNotNull(userDetails);
        Assert.assertEquals("theuser", userDetails.getUsername());
        Assert.assertEquals("abc", userDetails.getPassword());
        Assert.assertEquals(2, userDetails.getAuthorities().size());
        Assert.assertEquals("ROLE_ANONYMOUS",
                userDetails.getAuthorities().toArray(new GrantedAuthority[0])[0].getAuthority());
        Assert.assertEquals("ROLE_USER",
                userDetails.getAuthorities().toArray(new GrantedAuthority[0])[1].getAuthority());
    }

    @Test
    public void testAnonymousResponse() {
        PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("", null);
        token.setDetails(details);
        UserDetails userDetails = detailsService.loadUserDetails(token);
        Assert.assertNotNull(userDetails);
        Assert.assertEquals("anonymous", userDetails.getUsername());
        Assert.assertEquals("", userDetails.getPassword());
        Assert.assertEquals(1, userDetails.getAuthorities().size());
        Assert.assertEquals("ROLE_ANONYMOUS",
                userDetails.getAuthorities().toArray(new GrantedAuthority[0])[0].getAuthority());
    }

    @Test
    public void testDifferentConfig() {
        details = new SessionKeyServerAuthenticationDetails("remote");
        RestTemplate restTemplate = sessionKeyServerService.getRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://remote.example.com:9090/valid?token=xyz&realm=anotherRealm"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("YES\ntheuser\nthesource", MediaType.TEXT_PLAIN));
        PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("xyz", null);
        token.setDetails(details);
        UserDetails userDetails = detailsService.loadUserDetails(token);
        Assert.assertNotNull(userDetails);
        Assert.assertEquals("theuser", userDetails.getUsername());
        Assert.assertEquals("xyz", userDetails.getPassword());
        Assert.assertEquals(2, userDetails.getAuthorities().size());
        Assert.assertEquals("ROLE_ANONYMOUS",
                userDetails.getAuthorities().toArray(new GrantedAuthority[0])[0].getAuthority());
        Assert.assertEquals("ROLE_USER",
                userDetails.getAuthorities().toArray(new GrantedAuthority[0])[1].getAuthority());
    }

    @Test
    public void testMissingConfig() {
        details = new SessionKeyServerAuthenticationDetails("missing");
        RestTemplate restTemplate = sessionKeyServerService.getRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://127.0.0.1:4301/valid?token=def&realm=rcloud")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("YES\ntheuser\nthesource", MediaType.TEXT_PLAIN));
        PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("def", null);
        token.setDetails(details);
        UserDetails userDetails = detailsService.loadUserDetails(token);
        Assert.assertNotNull(userDetails);
        Assert.assertEquals("theuser", userDetails.getUsername());
        Assert.assertEquals("def", userDetails.getPassword());
        Assert.assertEquals(2, userDetails.getAuthorities().size());
        Assert.assertEquals("ROLE_ANONYMOUS",
                userDetails.getAuthorities().toArray(new GrantedAuthority[0])[0].getAuthority());
        Assert.assertEquals("ROLE_USER",
                userDetails.getAuthorities().toArray(new GrantedAuthority[0])[1].getAuthority());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testNoRequest() {

        RestTemplate restTemplate = sessionKeyServerService.getRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("No", MediaType.TEXT_PLAIN));
        PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("abc", null);
        token.setDetails(details);
        UserDetails details = detailsService.loadUserDetails(token);
        Assert.fail("User should not have been found: " + details);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testSupercededRequest() {

        RestTemplate restTemplate = sessionKeyServerService.getRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("SUPERCEDED\ntheuser", MediaType.TEXT_PLAIN));
        PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("abc", null);
        token.setDetails(details);

        UserDetails details = detailsService.loadUserDetails(token);
        Assert.fail("User should not have been found: " + details);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testNullTokenRequest() {
        UserDetails details = detailsService.loadUserDetails(null);
        Assert.fail("User should not have been found: " + details);
    }

    @Test(expected = HttpClientErrorException.class)
    public void testServerErrorRequest() {
        RestTemplate restTemplate = sessionKeyServerService.getRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest().body("ERR: missing realm").contentType(MediaType.TEXT_PLAIN));
        PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("abc", null);
        token.setDetails(details);

        UserDetails details = detailsService.loadUserDetails(token);
        Assert.fail("User should not have been found: " + details);
    }

}
