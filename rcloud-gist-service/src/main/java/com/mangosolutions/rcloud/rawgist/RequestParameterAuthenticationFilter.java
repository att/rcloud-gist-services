package com.mangosolutions.rcloud.rawgist;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.util.Assert;

public class RequestParameterAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

	private String principalRequestParameter = "session_token";
	private String credentialsRequestParameter;
	private boolean exceptionIfHeaderMissing = true;

	/**
	 * Read and returns the header named by {@code principalRequestHeader} from the
	 * request.
	 *
	 * @throws PreAuthenticatedCredentialsNotFoundException if the header is missing and
	 * {@code exceptionIfHeaderMissing} is set to {@code true}.
	 */
	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		String principal = request.getParameter(principalRequestParameter);

		if (principal == null && exceptionIfHeaderMissing) {
			throw new PreAuthenticatedCredentialsNotFoundException(principalRequestParameter
					+ " parameter not found in request.");
		}

		return principal;
	}

	/**
	 * Credentials aren't usually applicable, but if a {@code credentialsRequestHeader} is
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

	public void setPrincipalRequestParameter(String principalRequestHeader) {
		Assert.hasText(principalRequestHeader,
				"principalRequestHeader must not be empty or null");
		this.principalRequestParameter = principalRequestHeader;
	}

	public void setCredentialsRequestParameter(String credentialsRequestHeader) {
		Assert.hasText(credentialsRequestHeader,
				"credentialsRequestHeader must not be empty or null");
		this.credentialsRequestParameter = credentialsRequestHeader;
	}

	/**
	 * Defines whether an exception should be raised if the principal header is missing.
	 * Defaults to {@code true}.
	 *
	 * @param exceptionIfHeaderMissing set to {@code false} to override the default
	 * behaviour and allow the request to proceed if no header is found.
	 */
	public void setExceptionIfHeaderMissing(boolean exceptionIfHeaderMissing) {
		this.exceptionIfHeaderMissing = exceptionIfHeaderMissing;
	}
	
}
