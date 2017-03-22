package com.mangosolutions.rcloud.sessionkeyauth;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


public class SessionKeyServerUserDetailsServiceTest {
	
	@Test
	public void testYesResponse() {
		
		SessionKeyServerUserDetailsService detailsService = new SessionKeyServerUserDetailsService();
		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
				     .andRespond(withSuccess("YES\ntheuser\nthesource", MediaType.TEXT_PLAIN));

		UserDetails details = detailsService.loadUserByUsername("abc");
		Assert.assertNotNull(details);
		Assert.assertEquals("theuser", details.getUsername());
		Assert.assertEquals("abc", details.getPassword());
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void testNoRequest() {
		
		SessionKeyServerUserDetailsService detailsService = new SessionKeyServerUserDetailsService();
		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
				     .andRespond(withSuccess("No", MediaType.TEXT_PLAIN));

		UserDetails details = detailsService.loadUserByUsername("abc");
		Assert.fail("User should not have been found: " + details);
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void testSupercededRequest() {
		
		SessionKeyServerUserDetailsService detailsService = new SessionKeyServerUserDetailsService();
		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
				     .andRespond(withSuccess("SUPERCEDED\ntheuser", MediaType.TEXT_PLAIN));

		UserDetails details = detailsService.loadUserByUsername("abc");
		Assert.fail("User should not have been found: " + details);
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void testNullTokenRequest() {
		
		SessionKeyServerUserDetailsService detailsService = new SessionKeyServerUserDetailsService();
		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://127.0.0.1:4301/valid?token=&realm=rcloud")).andExpect(method(HttpMethod.GET))
				     .andRespond(withSuccess("NO\n", MediaType.TEXT_PLAIN));

		UserDetails details = detailsService.loadUserByUsername(null);
		Assert.fail("User should not have been found: " + details);
	}
	
	@Test(expected=HttpClientErrorException.class)
	public void testServerErrorRequest() {
		SessionKeyServerUserDetailsService detailsService = new SessionKeyServerUserDetailsService();
		RestTemplate restTemplate = detailsService.getRestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("http://127.0.0.1:4301/valid?token=abc&realm=rcloud")).andExpect(method(HttpMethod.GET))
				     .andRespond(withBadRequest().body("ERR: missing realm").contentType(MediaType.TEXT_PLAIN));
		
		UserDetails details = detailsService.loadUserByUsername("abc");
		Assert.fail("User should not have been found: " + details);
	}
	
}
