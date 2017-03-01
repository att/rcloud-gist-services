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

	private String sessionKeyServerUrl = SESSION_KEY_SERVER_DEFAULT_URI;

	private RestTemplate restTemplate;

	public SessionKeyServerUserDetailsService() {
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new SessionKeyServerMessageConverter());
		restTemplate = new RestTemplate();
		restTemplate.setMessageConverters(converters);
	}

	@Override
	public UserDetails loadUserByUsername(String token) throws UsernameNotFoundException {
		// TODO need a cache to prevent constant lookup
		Map<String, Object> params = new HashMap<>();
		params.put("token", token);
		params.put("realm", "rcloud");
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
		// T body, MultiValueMap<String, String> headers, HttpMethod method, URI
		// url

		URI uri = new UriTemplate(sessionKeyServerUrl).expand(params);
		// URIBuilder builder = new URIBuilder(sessionKeyServerUrl);
		RequestEntity<SessionKeyServerResponse> requestEntity = new RequestEntity<>(new SessionKeyServerResponse(), headers, HttpMethod.GET, uri);

		ResponseEntity<SessionKeyServerResponse> response = restTemplate.exchange(requestEntity,
				SessionKeyServerResponse.class);
		// ResponseEntity<SessionKeyServerResponse> response =
		// restTemplate.getForEntity(sessionKeyServerUrl,
		// SessionKeyServerResponse.class, params);
		if (!HttpStatus.OK.equals(response.getStatusCode())) {
			logger.error("Bad response from the Session Key Server: {}", response);
			throw new UsernameNotFoundException("Response from SessionKeyServer was not successful");
		}

		return convertToUserDetails(response.getBody(), token);
	}

	private UserDetails convertToUserDetails(SessionKeyServerResponse response, String token) {
		if (!SessionKeyServerResult.YES.equals(response.getResult())) {
			throw new UsernameNotFoundException(
					"Token provided is not valid. Response from SessionKeyServer is " + response.getResult());
		}
		String username = response.getName();
		UserDetails details = new User(username, token,
				(Collection<? extends GrantedAuthority>) Collections.emptyList());
		return details;
	}

}
