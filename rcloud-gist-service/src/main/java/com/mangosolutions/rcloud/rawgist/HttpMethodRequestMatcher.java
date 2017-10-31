/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.util.Collection;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class HttpMethodRequestMatcher implements RequestMatcher {

    private static final Logger logger = LoggerFactory.getLogger(HttpMethodRequestMatcher.class);

    private Collection<String> methods = new HashSet<>();

    public HttpMethodRequestMatcher(String... methods) {
        for (String method : methods) {
            logger.debug("Adding method {} as http method request filter", method);
            this.methods.add(method.toUpperCase());
        }
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String method = request.getMethod();
        logger.debug("Testing method {}", method);
        boolean match = methods.contains(method.toUpperCase());
        logger.debug("Matched {}: {}", method, match);
        return match;
    }

}
