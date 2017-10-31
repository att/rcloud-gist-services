/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.VndErrors;
import org.springframework.hateoas.VndErrors.VndError;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangosolutions.rcloud.rawgist.repository.GistAccessDeniedException;
import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryError;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryException;

@ControllerAdvice
public class ApplicationErrorsControllerAdvice {

    private final Logger logger = LoggerFactory.getLogger(ApplicationErrorsControllerAdvice.class);

    @Autowired
    private ObjectMapper objectMapper;

    @ResponseBody
    @ExceptionHandler(GistRepositoryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handle(GistRepositoryException ex) {
        logger.error(ex.getMessage(), ex);
        GistError gistError = ex.getGistError();
        VndError error = new VndError(gistError.getCode().toString(), gistError.toString());
        try {
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(gistError.getFormattedMessage());
        }
    }

    @ResponseBody
    @ExceptionHandler(GistAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String handle(GistAccessDeniedException ex) {
        logger.error(ex.getMessage(), ex);
        GistError gistError = ex.getGistError();
        VndError error = new VndError(gistError.getCode().toString(), gistError.toString());
        try {
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(gistError.getFormattedMessage());
        }
    }

    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String handle(AccessDeniedException ex) {
        logger.error(ex.getMessage(), ex);
        VndError error = new VndError("ACCESS_DENIED", "You do not have permission to access this resource.");
        try {
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @ResponseBody
    @ExceptionHandler(GistRepositoryError.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String handle(GistRepositoryError ex) {
        logger.error(ex.getMessage(), ex);
        GistError gistError = ex.getGistError();
        VndError error = new VndError(gistError.getCode().toString(), gistError.toString());
        try {
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(gistError.getFormattedMessage());
        }
    }

    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String handle(RuntimeException ex) {
        VndErrors error = new VndErrors("INTERNAL_SERVER_ERROR", "Application error.");
        if (!StringUtils.isEmpty(ex.getMessage())) {
            error.add(new VndErrors.VndError("INTERNAL_SERVER_ERROR", ex.getMessage()));
        }
        logger.error("Application error.", ex);
        try {
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            return "INTERNAL_SERVER_ERROR" + ex.getMessage();
        }
    }

}
