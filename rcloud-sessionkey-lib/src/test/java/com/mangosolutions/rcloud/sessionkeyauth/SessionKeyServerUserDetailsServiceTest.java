/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


public class SessionKeyServerUserDetailsServiceTest {

	private KeyServerConfiguration config;
	private KeyServerConfiguration remoteConfig;
	
	SessionKeyServerUserDetailsService detailsService;
	SessionKeyServerAuthenticationDetails details;
	
	@Before
	public void setUp() {
		config = new KeyServerConfiguration();
		remoteConfig = new KeyServerConfiguration();
		remoteConfig.setClientId("remote");
		remoteConfig.setHost("remote.example.com");
		remoteConfig.setPort(9090);
		remoteConfig.setRealm("anotherRealm");
		detailsService = new SessionKeyServerUserDetailsService(Arrays.asList(config, remoteConfig));
		details = new SessionKeyServerAuthenticationDetails("default");
	}
	
	@Test
	public void testYesResponse() {
		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
				     .andRespond(withSuccess("YES\ntheuser\nthesource", MediaType.TEXT_PLAIN));
		PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("abc", null);
		token.setDetails(details);
		UserDetails userDetails = detailsService.loadUserDetails(token);
		Assert.assertNotNull(userDetails);
		Assert.assertEquals("theuser", userDetails.getUsername());
		Assert.assertEquals("abc", userDetails.getPassword());
	}
	
	@Test
	public void testDifferentConfig() {
		details = new SessionKeyServerAuthenticationDetails("remote");
		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://remote.example.com:9090/valid?token=xyz&realm=anotherRealm")).andExpect(method(HttpMethod.GET))
				     .andRespond(withSuccess("YES\ntheuser\nthesource", MediaType.TEXT_PLAIN));
		PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("xyz", null);
		token.setDetails(details);
		UserDetails userDetails = detailsService.loadUserDetails(token);
		Assert.assertNotNull(userDetails);
		Assert.assertEquals("theuser", userDetails.getUsername());
		Assert.assertEquals("xyz", userDetails.getPassword());
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void testMissingConfig() {
		details = new SessionKeyServerAuthenticationDetails("missing");
		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://remote.example.com:9090/valid?token=xyz&realm=anotherRealm")).andExpect(method(HttpMethod.GET))
				     .andRespond(withSuccess("YES\ntheuser\nthesource", MediaType.TEXT_PLAIN));
		PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("xyz", null);
		token.setDetails(details);
		detailsService.loadUserDetails(token);
	}

	@Test(expected=UsernameNotFoundException.class)
	public void testNoRequest() {

		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
				     .andRespond(withSuccess("No", MediaType.TEXT_PLAIN));
		PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("abc", null);
		token.setDetails(details);
		UserDetails details = detailsService.loadUserDetails(token);
		Assert.fail("User should not have been found: " + details);
	}

	@Test(expected=UsernameNotFoundException.class)
	public void testSupercededRequest() {

		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
				     .andRespond(withSuccess("SUPERCEDED\ntheuser", MediaType.TEXT_PLAIN));
		PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("abc", null);
		token.setDetails(details);
		
		UserDetails details = detailsService.loadUserDetails(token);
		Assert.fail("User should not have been found: " + details);
	}

	@Test(expected=UsernameNotFoundException.class)
	public void testNullTokenRequest() {

		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://127.0.0.1:4301/valid?token=&realm=rcloud")).andExpect(method(HttpMethod.GET))
				     .andRespond(withSuccess("NO\n", MediaType.TEXT_PLAIN));

		UserDetails details = detailsService.loadUserDetails(null);
		Assert.fail("User should not have been found: " + details);
	}

	@Test(expected=HttpClientErrorException.class)
	public void testServerErrorRequest() {
		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
				     .andRespond(withBadRequest().body("ERR: missing realm").contentType(MediaType.TEXT_PLAIN));
		PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken("abc", null);
		token.setDetails(details);
		
		UserDetails details = detailsService.loadUserDetails(token);
		Assert.fail("User should not have been found: " + details);
	}

}
