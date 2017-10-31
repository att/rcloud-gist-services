/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import java.net.URI;
import java.util.ArrayList;
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
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

public class SessionKeyServerService {

    private static final Logger logger = LoggerFactory.getLogger(SessionKeyServerService.class);

    private RestTemplate restTemplate;

    private Map<String, KeyServerConfiguration> keyServers = new HashMap<>();

    public SessionKeyServerService(Map<String, KeyServerConfiguration> keyServers) {
        this(new RestTemplate(), keyServers);
    }

    public SessionKeyServerService(RestTemplate restTemplate, Map<String, KeyServerConfiguration> keyServers) {
        this.keyServers = keyServers;
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new SessionKeyServerMessageConverter());
        converters.add(new StringHttpMessageConverter());
        restTemplate.setMessageConverters(converters);
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = "sessionkeys")
    public SessionKeyServerResponse validateToken(String clientId, String sessionKey) {
        KeyServerConfiguration keyServer = getKeyServerConfiguration(clientId);
        ResponseEntity<SessionKeyServerResponse> response = doTokenValidation(sessionKey, keyServer);
        return response.getBody();
    }

    public String authenticate(String username, String password) {
        // TODO this needs to check all of the sessionkey servers?
        KeyServerConfiguration keyServer = getKeyServerConfiguration("default");
        ResponseEntity<String> response = doAuthentication(username, password, keyServer);
        return response.getBody();
    }

    private KeyServerConfiguration getKeyServerConfiguration(String clientId) {
        KeyServerConfiguration configuration = this.keyServers.get(clientId);
        if (configuration == null || !configuration.isActive()) {
            configuration = this.keyServers.get("default");
            logger.info("No active key server defined for client_id {}, attempting to use default.", clientId);
        }
        if (configuration == null) {
            logger.warn("No key server defined for client_id {}, and not fallback 'default' defined", clientId);
            throw new UsernameNotFoundException("SessionKeyServer configuration not found for client_id " + clientId);
        }
        return configuration;
    }

    private ResponseEntity<String> doAuthentication(String username, String password,
            KeyServerConfiguration keyServer) {
        try {

            Map<String, Object> params = buildParams(username, password, keyServer);
            HttpHeaders headers = buildHeaders();

            URI uri = buildUri(keyServer.getAuthUrl(), params);
            RequestEntity<String> requestEntity = buildAuthenticationRequest(headers, uri);

            ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                if (HttpStatus.FORBIDDEN.equals(response.getStatusCode())) {
                    logger.info("Credentials provided for {} are not correct", username);
                } else {
                    logger.error("Bad response from the Session Key Server: {}, response: {}", keyServer, response);
                }
                throw new UsernameNotFoundException("Response from SessionKeyServer was not successful");
            }
            return response;
        } catch (HttpClientErrorException e) {
            HttpStatus status = e.getStatusCode();
            if (HttpStatus.FORBIDDEN.equals(status)) {
                logger.info("Credentials provided for {} are not correct.", username);
                throw new UsernameNotFoundException("Response from SessionKeyServer was not successful");
            } else {
                throw e;
            }
        }
    }

    private Map<String, Object> buildParams(String username, String password, KeyServerConfiguration server) {
        Map<String, Object> params = new HashMap<>();

        params.put("user", username);
        params.put("pwd", password);
        params.put("realm", server.getRealm());
        params.put("host", server.getHost());
        params.put("port", server.getPort());
        return params;
    }

    private ResponseEntity<SessionKeyServerResponse> doTokenValidation(String sessionKey,
            KeyServerConfiguration keyServer) {
        Map<String, Object> params = buildParams(keyServer, sessionKey);
        HttpHeaders headers = buildHeaders();

        URI uri = buildUri(keyServer.getUrl(), params);
        RequestEntity<SessionKeyServerResponse> requestEntity = buildTokenValidationRequest(headers, uri);

        ResponseEntity<SessionKeyServerResponse> response = restTemplate.exchange(requestEntity,
                SessionKeyServerResponse.class);

        if (!HttpStatus.OK.equals(response.getStatusCode())) {
            logger.error("Bad response from the Session Key Server: {}, response: {}", keyServer, response);
            throw new UsernameNotFoundException("Response from SessionKeyServer was not successful");
        }
        return response;
    }

    private URI buildUri(String urlTemplate, Map<String, Object> params) {
        URI uri = new UriTemplate(urlTemplate).expand(params);
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

    private RequestEntity<String> buildAuthenticationRequest(HttpHeaders headers, URI uri) {
        RequestEntity<String> requestEntity = new RequestEntity<>(new String(), headers, HttpMethod.GET, uri);
        return requestEntity;
    }

    private RequestEntity<SessionKeyServerResponse> buildTokenValidationRequest(HttpHeaders headers, URI uri) {
        RequestEntity<SessionKeyServerResponse> requestEntity = new RequestEntity<>(new SessionKeyServerResponse(),
                headers, HttpMethod.GET, uri);
        return requestEntity;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

}
