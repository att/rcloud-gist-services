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
import org.springframework.web.HttpRequestMethodNotSupportedException;
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
    	logger.debug(ex.getMessage(), ex);
    	GistError gistError = ex.getGistError();
        VndError error = new VndError(gistError.getCode().toString(), gistError.toString());
        try {
            this.logError(ex, HttpStatus.BAD_REQUEST, error);
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(gistError.getFormattedMessage());
        }
    }

    
    @ResponseBody
    @ExceptionHandler(GistAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String handle(GistAccessDeniedException ex) {
    	logger.debug(ex.getMessage(), ex);
    	GistError gistError = ex.getGistError();
        VndError error = new VndError(gistError.getCode().toString(), gistError.toString());
        try {
            this.logError(ex, HttpStatus.FORBIDDEN, error);
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(gistError.getFormattedMessage());
        }
    }    
    
    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String handle(AccessDeniedException ex) {
    	logger.debug(ex.getMessage(), ex);
        VndError error = new VndError("ACCESS_DENIED", "You do not have permission to access this resource.");
        try {
            this.logError(ex, HttpStatus.FORBIDDEN, error);
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(ex.getMessage());
        }
    }  
    
    @ResponseBody
    @ExceptionHandler(GistRepositoryError.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String handle(GistRepositoryError ex) {
    	logger.debug(ex.getMessage(), ex);
    	GistError gistError = ex.getGistError();
        VndError error = new VndError(gistError.getCode().toString(), gistError.toString());
        try {
            this.logError(ex, HttpStatus.INTERNAL_SERVER_ERROR, error);
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(gistError.getFormattedMessage());
        }
    }
    
    @ResponseBody
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    String handle(HttpRequestMethodNotSupportedException ex) {
        logger.debug(ex.getMessage(), ex);
        
        VndErrors error = new VndErrors("METHOD_NOT_ALLOWED", "Application error.");
        if(!StringUtils.isEmpty(ex.getMessage())) {
            error.add(new VndErrors.VndError("METHOD_NOT_ALLOWED", ex.getMessage()));
        }
        try {
            this.logError(ex, HttpStatus.METHOD_NOT_ALLOWED, error);
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(ex);
        }
    }
    
    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String handle(RuntimeException ex) {
        VndErrors error = new VndErrors("INTERNAL_SERVER_ERROR", "Application error.");
        if(!StringUtils.isEmpty(ex.getMessage())) {
            error.add(new VndErrors.VndError("INTERNAL_SERVER_ERROR", ex.getMessage()));
        }
        logger.debug("Application error.", ex);
        try {
            this.logError(ex, HttpStatus.INTERNAL_SERVER_ERROR, error);
            return objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
        	return "INTERNAL_SERVER_ERROR" + ex.getMessage();
        }
    }
    
    private void logError(Exception ex, HttpStatus status, VndError error) {
        logger.error("Could not complete request. msg[{}], status[{}], err[{}]", ex.getMessage(), status, error, ex);
    }
    
    private void logError(Exception ex, HttpStatus status, VndErrors error) {
        logger.error("Could not complete request. msg[{}], status[{}], err[{}]", ex.getMessage(), status, error, ex);
    }

}
