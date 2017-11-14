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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

public class SessionKeyServerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionKeyServerService.class);

    private RestTemplate restTemplate;
    
    private Map<String, KeyServerConfiguration> keyServers = new HashMap<>();
    
    public SessionKeyServerService(Map<String, KeyServerConfiguration> keyServers) {
        this(new RestTemplate(), keyServers);
    }
    
    public SessionKeyServerService(RestTemplate restTemplate,
            Map<String, KeyServerConfiguration> keyServers) {
        this.keyServers = keyServers;
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new SessionKeyServerMessageConverter());
        restTemplate.setMessageConverters(converters);
        this.restTemplate = restTemplate;
    }
    
    public SessionKeyServerResponse authenticate(String clientId, String sessionKey) {
        KeyServerConfiguration keyServer = getKeyServerConfiguration(clientId);
        logger.debug("Using key server for token authentication {}", keyServer);
        ResponseEntity<SessionKeyServerResponse> response = doAuthentication(sessionKey, keyServer);
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
    
    private ResponseEntity<SessionKeyServerResponse> doAuthentication(String sessionKey,
            KeyServerConfiguration keyServer) {
        Map<String, Object> params = buildParams(keyServer, sessionKey);
        HttpHeaders headers = buildHeaders();

        URI uri = buildUri(keyServer, params);
        RequestEntity<SessionKeyServerResponse> requestEntity = buildRequest(headers, uri);

        ResponseEntity<SessionKeyServerResponse> response = restTemplate.exchange(requestEntity,
                SessionKeyServerResponse.class);
        
        logger.debug("Received response {} for session key {}", response, sessionKey);

        if (!HttpStatus.OK.equals(response.getStatusCode())) {
            logger.error("Bad response from the Session Key Server: {}, response: {}", keyServer, response);
            throw new UsernameNotFoundException("Response from SessionKeyServer was not successful");
        }
        return response;
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

    
    private RequestEntity<SessionKeyServerResponse> buildRequest(HttpHeaders headers, URI uri) {
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
