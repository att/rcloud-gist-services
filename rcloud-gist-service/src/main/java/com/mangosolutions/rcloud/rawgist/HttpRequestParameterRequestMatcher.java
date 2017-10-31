package com.mangosolutions.rcloud.rawgist;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class HttpRequestParameterRequestMatcher implements RequestMatcher {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestParameterRequestMatcher.class);
    
    private String parameter = null;
    private String value = null;

    public HttpRequestParameterRequestMatcher(String parameter, String value) {
        this.parameter = parameter;
        this.value = value;
    }
    
    @Override
    public boolean matches(HttpServletRequest request) {
        boolean matches = false;
        String[] values = request.getParameterValues(parameter);
        if(values != null) {
            return Arrays.asList(values).contains(value);
        }
        return matches;
    }

    
    
}
