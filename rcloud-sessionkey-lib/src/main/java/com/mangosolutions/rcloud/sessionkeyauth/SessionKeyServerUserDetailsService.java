/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

public class SessionKeyServerUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	private static final Logger logger = LoggerFactory.getLogger(SessionKeyServerUserDetailsService.class);
	
	private static final GrantedAuthority ANONYMOUS_AUTHORITY = new SimpleGrantedAuthority("ROLE_ANONYMOUS");
	private static final GrantedAuthority USER_AUTHORITY = new SimpleGrantedAuthority("ROLE_USER");
	
	private static final Collection<GrantedAuthority> ANONYMOUS_AUTHORITIES = Arrays.asList(ANONYMOUS_AUTHORITY);
	
	private static final Collection<GrantedAuthority> USER_AUTHORITIES = Arrays.asList(USER_AUTHORITY, ANONYMOUS_AUTHORITY);
	
	private static final UserDetails ANONYMOUS_USER = new User("anonymous", "", ANONYMOUS_AUTHORITIES);

	private RestTemplate restTemplate;

	private Map<String, KeyServerConfiguration> keyServers = new HashMap<>();
	
	public SessionKeyServerUserDetailsService(Map<String, KeyServerConfiguration> keyServers) {
		this(new RestTemplate(), keyServers);
	}

	public SessionKeyServerUserDetailsService(RestTemplate restTemplate, Map<String, KeyServerConfiguration> keyServers) {
		this.keyServers = keyServers;
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new SessionKeyServerMessageConverter());
		restTemplate.setMessageConverters(converters);
		this.restTemplate = restTemplate;
	}
	
	@Override
	@Cacheable(value="sessionkeys", key = "{#token.getPrincipal(), #token.getDetails().getClientId()}")
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
		if(token == null) {
			throw new UsernameNotFoundException("SessionKey token not correctly defined.");
		}
		UserDetails userDetails = createAnonymousUser();
		String sessionKey = getSessionKey(token);
		if(!isAnonymousUser(sessionKey)) {
			
			String clientId = getClientId(token);
			KeyServerConfiguration keyServer = getKeyServerConfiguration(clientId);
			ResponseEntity<SessionKeyServerResponse> response = doAuthentication(sessionKey, keyServer);
			userDetails = convertToUserDetails(response.getBody(), sessionKey);
			
		}
		return userDetails;
	}



	private boolean isAnonymousUser(String sessionKey) {
		return StringUtils.isEmpty(sessionKey);
	}

	private String getSessionKey(PreAuthenticatedAuthenticationToken token) {
		Object principal = token.getPrincipal();
		//Assume that the principal is not null and is a string
		if(!(principal instanceof String)) {
			logger.warn("SessionKey token not correctly defined.");
			throw new UsernameNotFoundException("SessionKey token not correctly defined.");
		}
		String sessionKey = (String) token.getPrincipal();
		return sessionKey;
	}

	private ResponseEntity<SessionKeyServerResponse> doAuthentication(String sessionKey,
			KeyServerConfiguration keyServer) {
		Map<String, Object> params = buildParams(keyServer, sessionKey);
		HttpHeaders headers = buildHeaders();

		URI uri = buildUri(keyServer, params);
		RequestEntity<SessionKeyServerResponse> requestEntity = buildRequest(headers, uri);

		ResponseEntity<SessionKeyServerResponse> response = restTemplate.exchange(requestEntity,
				SessionKeyServerResponse.class);

		if (!HttpStatus.OK.equals(response.getStatusCode())) {
			logger.error("Bad response from the Session Key Server: {}, response: {}", keyServer, response);
			throw new UsernameNotFoundException("Response from SessionKeyServer was not successful");
		}
		return response;
	}

	private String getClientId(PreAuthenticatedAuthenticationToken token) {
		Object details =  token.getDetails();
		String clientId = "default";
		if(details instanceof SessionKeyServerAuthenticationDetails) {
			clientId = ((SessionKeyServerAuthenticationDetails)details).getClientId();
		}
		return clientId;
	}

	private KeyServerConfiguration getKeyServerConfiguration(String clientId) {
		KeyServerConfiguration configuration = this.keyServers.get(clientId);
		if(configuration == null || !configuration.isActive()) {
			configuration = this.keyServers.get("default");
			logger.info("No active key server defined for client_id {}, attempting to use default.", clientId);
		}
		if(configuration == null) {
			logger.warn("No key server defined for client_id {}, and not fallback 'default' defined", clientId);
			throw new UsernameNotFoundException("SessionKeyServer configuration not found for client_id " + clientId);
		}
		return configuration;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	private RequestEntity<SessionKeyServerResponse> buildRequest(HttpHeaders headers, URI uri) {
		RequestEntity<SessionKeyServerResponse> requestEntity = new RequestEntity<>(new SessionKeyServerResponse(), headers, HttpMethod.GET, uri);
		return requestEntity;
	}

	private URI buildUri(KeyServerConfiguration server, Map<String, Object> params) {
		URI uri = new UriTemplate(server.getUrl()).expand(params);
		return uri;
	}

	private HttpHeaders buildHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
		return headers;
	}

	private Map<String, Object> buildParams(KeyServerConfiguration server, String token) {
		Map<String, Object> params = new HashMap<>();
		
		params.put("token", token);
		params.put("realm", server.getRealm());
		params.put("host", server.getHost());
		params.put("port", server.getPort());
		return params;
	}

	private UserDetails convertToUserDetails(SessionKeyServerResponse response, String sessionKey) {
		if (!SessionKeyServerResult.YES.equals(response.getResult())) {
			throw new UsernameNotFoundException(
					"Token provided is not valid. Response from SessionKeyServer is " + response.getResult());
		}
		String username = response.getName();
		UserDetails details = new User(username, sessionKey, USER_AUTHORITIES);
		return details;
	}
	
	private UserDetails createAnonymousUser() {
		return ANONYMOUS_USER;
	}

}
