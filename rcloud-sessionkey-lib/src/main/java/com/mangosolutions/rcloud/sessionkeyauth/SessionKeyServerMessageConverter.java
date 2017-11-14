/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StringUtils;

/**
 * Converts the authentication result from a request to a SessionKeyServer to a
 * standard SessionKeyServerResponse object.
 * 
 */
public class SessionKeyServerMessageConverter extends AbstractHttpMessageConverter<SessionKeyServerResponse>
        implements HttpMessageConverter<SessionKeyServerResponse> {

    private static final Logger logger = LoggerFactory.getLogger(SessionKeyServerMessageConverter.class);

    public SessionKeyServerMessageConverter() {
        super(MediaType.TEXT_PLAIN);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return SessionKeyServerResponse.class.isAssignableFrom(clazz);
    }

    @Override
    protected SessionKeyServerResponse readInternal(Class<? extends SessionKeyServerResponse> clazz,
            HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        if (this.supports(clazz)) {
            return this.convert(inputMessage);
        }
        throw new HttpMessageNotReadableException("Could not convert message to a " + clazz);
    }

    @Override
    protected void writeInternal(SessionKeyServerResponse t, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        // nothing to do here, this is not convertable.
    }

    private SessionKeyServerResponse convert(HttpInputMessage inputMessage) throws IOException {
        List<String> lines = IOUtils.readLines(inputMessage.getBody(), "UTF-8");
        logger.info("Received response from session key server: {}", lines);
        if (lines == null || lines.isEmpty()) {
            logger.error("Response from session key server is not valid, could not parse response {}", lines);
            throw new HttpMessageNotReadableException(
                    "Response from SessionKeyServer does not contain a valid result.");

        }
        SessionKeyServerResponse response = new SessionKeyServerResponse();
        for (int i = 0; i < lines.size(); i++) {
            switch (i) {
            case 0: // this is the YES/NO line
                parseResult(lines.get(i), response);
                break;
            case 1: // this is the name line (optional)
                parseName(lines.get(i), response);
                break;
            case 2: // this is the source line (optional)
                parseSource(lines.get(i), response);
                break;
            default:
                logLine(lines, i);
                break;
            }
        }

        return response;
    }

    private void logLine(List<String> lines, int i) {
        String line = lines.get(i);
        if (StringUtils.isEmpty(line)) {
            return;
        }
        logger.info("Found unexpected value '{}' in session key server response on line {}", line, i);
    }

    private void parseSource(String source, SessionKeyServerResponse response) {
        if (StringUtils.isEmpty(source)) {
            return;
        }
        response.setSource(source.trim());

    }

    private void parseName(String name, SessionKeyServerResponse response) {
        if (StringUtils.isEmpty(name)) {
            return;
        }
        response.setName(name.trim());
    }

    private void parseResult(String result, SessionKeyServerResponse response) throws HttpMessageNotReadableException {
        if (StringUtils.isEmpty(result)) {
            throw new HttpMessageNotReadableException(
                    "Response from SessionKeyServer does not contain a valid result.");
        }
        try {
            SessionKeyServerResult sessionKeyServerResult = SessionKeyServerResult.valueOf(result.trim().toUpperCase());
            response.setResult(sessionKeyServerResult);
        } catch (IllegalArgumentException e) {
            logger.error("Could not parse {} as a result from the SessionKeyServer", result);
            throw new HttpMessageNotReadableException(
                    "Could not parse " + result + " as a result from the SessionKeyServer");
        }
    }

}
