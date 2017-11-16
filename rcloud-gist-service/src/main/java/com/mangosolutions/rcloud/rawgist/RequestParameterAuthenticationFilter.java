/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.util.Assert;

public class RequestParameterAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

        private static final Logger logger = LoggerFactory.getLogger(RequestParameterAuthenticationFilter.class);
    
	private String principalRequestParameter = "access_token";
	private String credentialsRequestParameter;

	/**
	 * Read and returns the header named by {@code principalRequestParameter} from the
	 * request.
	 *
	 * @throws PreAuthenticatedCredentialsNotFoundException if the header is missing and
	 * {@code exceptionIfHeaderMissing} is set to {@code true}.
	 */
	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		
	    String principal = request.getParameter(principalRequestParameter);
	    principal = StringUtils.trimToEmpty(principal);
	    if(HttpMethod.GET.equals(HttpMethod.resolve(request.getMethod()))) {
                logger.info("Ignoring the access token on a GET request, setting it to an empty string");
               principal = ""; 
            }
            return principal;
        }

    /**
	 * Credentials aren't usually applicable, but if a {@code credentialsRequestParameter} is
	 * set, this will be read and used as the credentials value. Otherwise a dummy value
	 * will be used.
	 */
	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		if (credentialsRequestParameter != null) {
			return request.getParameter(credentialsRequestParameter);
		}

		return "N/A";
	}

	public void setPrincipalRequestParameter(String principalRequestParameter) {
		Assert.hasText(principalRequestParameter,
				"principalRequestHeader must not be empty or null");
		this.principalRequestParameter = principalRequestParameter;
	}

	public void setCredentialsRequestParameter(String credentialsRequestParameter) {
		Assert.hasText(credentialsRequestParameter,
				"credentialsRequestHeader must not be empty or null");
		this.credentialsRequestParameter = credentialsRequestParameter;
	}

}
