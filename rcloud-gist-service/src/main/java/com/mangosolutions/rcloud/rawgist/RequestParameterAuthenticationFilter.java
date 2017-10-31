/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

public class RequestParameterAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

	private String principalRequestParameter = "access_token";
	private String credentialsRequestParameter;
	private boolean exceptionIfParameterMissing = true;
	
	private RequestMatcher matcher = AnyRequestMatcher.INSTANCE;
	
	public RequestParameterAuthenticationFilter() {
	    
	}
	
	public RequestParameterAuthenticationFilter(RequestMatcher matcher) {
	    this.matcher = matcher;
	}

	/**
	 * Read and returns the header named by {@code principalRequestParameter} from the
	 * request.
	 *
	 * @throws PreAuthenticatedCredentialsNotFoundException if the header is missing and
	 * {@code exceptionIfHeaderMissing} is set to {@code true}.
	 */
	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
	    String principal = null; 
	    if(matcher.matches(request)) {
		principal = request.getParameter(principalRequestParameter);

		if (principal == null && exceptionIfParameterMissing) {
			throw new PreAuthenticatedCredentialsNotFoundException(principalRequestParameter
					+ " parameter not found in request.");
		} else {
			principal = principal == null? "": principal;
		}
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

	/**
	 * Defines whether an exception should be raised if the principal paramater is missing.
	 * Defaults to {@code true}.
	 *
	 * @param exceptionIfParameterMissing set to {@code false} to override the default
	 * behaviour and allow the request to proceed if no header is found.
	 */
	public void setExceptionIfParameterMissing(boolean exceptionIfParameterMissing) {
		this.exceptionIfParameterMissing = exceptionIfParameterMissing;
	}

}
