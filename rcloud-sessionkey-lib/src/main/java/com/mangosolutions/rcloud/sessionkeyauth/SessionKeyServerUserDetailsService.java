/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
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
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

public class SessionKeyServerUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	private static final Logger logger = LoggerFactory.getLogger(SessionKeyServerUserDetailsService.class);

	private RestTemplate restTemplate;

	private Map<String, KeyServerConfiguration> keyServers = new HashMap<>();
	
	public SessionKeyServerUserDetailsService(Collection<KeyServerConfiguration> keyServers) {
		this(new RestTemplate(), keyServers);
	}

	public SessionKeyServerUserDetailsService(RestTemplate restTemplate, Collection<KeyServerConfiguration> keyServers) {
		for(KeyServerConfiguration keyServer: keyServers) {
			String clientId = keyServer.getClientId();
			this.keyServers.put(clientId, keyServer);
		}
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new SessionKeyServerMessageConverter());
		restTemplate.setMessageConverters(converters);
		this.restTemplate = restTemplate;
	}
	
	public SessionKeyServerUserDetailsService(RestTemplate restTemplate, Map<String, KeyServerConfiguration> keyServers) {
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new SessionKeyServerMessageConverter());
		restTemplate.setMessageConverters(converters);
		this.restTemplate = restTemplate;
	}

	@Override
//	@Cacheable(value="sessionkeys")
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
		//TODO add caching back in
		if(token == null) {
			throw new UsernameNotFoundException("SessionKey token not correctly defined.");
		}
		Object principal = token.getPrincipal();
		//Assume that the principal is not null and is a string
		if(!(principal instanceof String)) {
			logger.warn("SessionKey token not correctly defined.");
			throw new UsernameNotFoundException("SessionKey token not correctly defined.");
		}
		String sessionKey = (String) token.getPrincipal();
		Object details =  token.getDetails();
		String clientId = "default";
		if(details instanceof SessionKeyServerAuthenticationDetails) {
			clientId = ((SessionKeyServerAuthenticationDetails)details).getClientId();
		}
		KeyServerConfiguration keyServer = this.keyServers.get(clientId);
		if(keyServer == null) {
			throw new UsernameNotFoundException("SessionKeyServer configuration not found for client_id " + clientId);
		}
	

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

		return convertToUserDetails(response.getBody(), sessionKey);
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
		Collection<GrantedAuthority> authorities = Collections.emptyList();
		UserDetails details = new User(username, sessionKey, authorities);
		return details;
	}

}
