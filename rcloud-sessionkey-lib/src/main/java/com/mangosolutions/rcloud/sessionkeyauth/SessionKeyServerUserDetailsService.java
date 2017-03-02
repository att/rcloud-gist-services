package com.mangosolutions.rcloud.sessionkeyauth;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

public class SessionKeyServerUserDetailsService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(SessionKeyServerUserDetailsService.class);

	private static final String SESSION_KEY_SERVER_DEFAULT_URI = "http://127.0.0.1:4301/valid?token={token}&realm={realm}";

	//TODO I have just made this up, find out what it really is.
	private static String RCLOUD_SESSION_KEY_SERVER_DEFAULT_REALM = "rcloud";
	
	private String sessionKeyServerUrl = SESSION_KEY_SERVER_DEFAULT_URI;

	private RestTemplate restTemplate;
	
	private String realm = RCLOUD_SESSION_KEY_SERVER_DEFAULT_REALM;

	public SessionKeyServerUserDetailsService() {
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new SessionKeyServerMessageConverter());
		restTemplate = new RestTemplate();
		restTemplate.setMessageConverters(converters);
	}
	
	public SessionKeyServerUserDetailsService(RestTemplate restTemplate) {
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new SessionKeyServerMessageConverter());
		restTemplate.setMessageConverters(converters);
		this.restTemplate = restTemplate;
	}

	@Override
	public UserDetails loadUserByUsername(String token) throws UsernameNotFoundException {
		// TODO need a cache to prevent constant lookup
		
		Map<String, Object> params = buildParams(token);
		HttpHeaders headers = buildHeaders();

		URI uri = buildUri(params);
		RequestEntity<SessionKeyServerResponse> requestEntity = buildRequest(headers, uri);

		ResponseEntity<SessionKeyServerResponse> response = restTemplate.exchange(requestEntity,
				SessionKeyServerResponse.class);

		if (!HttpStatus.OK.equals(response.getStatusCode())) {
			logger.error("Bad response from the Session Key Server: {}", response);
			throw new UsernameNotFoundException("Response from SessionKeyServer was not successful");
		}

		return convertToUserDetails(response.getBody(), token);
	}

	public String getSessionKeyServerUrl() {
		return sessionKeyServerUrl;
	}

	public void setSessionKeyServerUrl(String sessionKeyServerUrl) {
		this.sessionKeyServerUrl = sessionKeyServerUrl;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	private RequestEntity<SessionKeyServerResponse> buildRequest(HttpHeaders headers, URI uri) {
		RequestEntity<SessionKeyServerResponse> requestEntity = new RequestEntity<>(new SessionKeyServerResponse(), headers, HttpMethod.GET, uri);
		return requestEntity;
	}

	private URI buildUri(Map<String, Object> params) {
		URI uri = new UriTemplate(sessionKeyServerUrl).expand(params);
		return uri;
	}

	private HttpHeaders buildHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
		return headers;
	}

	private Map<String, Object> buildParams(String token) {
		Map<String, Object> params = new HashMap<>();
		params.put("token", token);
		params.put("realm", realm);
		return params;
	}

	private UserDetails convertToUserDetails(SessionKeyServerResponse response, String token) {
		if (!SessionKeyServerResult.YES.equals(response.getResult())) {
			throw new UsernameNotFoundException(
					"Token provided is not valid. Response from SessionKeyServer is " + response.getResult());
		}
		String username = response.getName();
		Collection<GrantedAuthority> authorities = Collections.emptyList();
		UserDetails details = new User(username, token, authorities);
		return details;
	}

}
