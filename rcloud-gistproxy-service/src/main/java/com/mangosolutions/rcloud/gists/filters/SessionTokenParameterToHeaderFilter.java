/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.gists.filters;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public final class SessionTokenParameterToHeaderFilter extends ZuulFilter {

	private static final Logger logger = LoggerFactory.getLogger(SessionTokenParameterToHeaderFilter.class);

	private int order = 100;

	private String path = "/";

	public SessionTokenParameterToHeaderFilter(String path, int order) {
		this.order = order;
		this.path = path;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return order;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
		try {
			rewriteToken(RequestContext.getCurrentContext());
		} catch (final Exception e) {
			Throwables.propagate(e);
		}
		return null;
	}

	private void rewriteToken(final RequestContext context) {
		if (context.getRequest().getRequestURI().startsWith(this.path)) {
			
			Map<String, List<String>> params = context.getRequestQueryParams();
			Map<String, String> requestHeaders = context.getZuulRequestHeaders();

			if (params != null && params.containsKey("access_token")) {
				List<String> tokens = params.get("access_token");
				if (!tokens.isEmpty()) {
					String token = tokens.get(0);
					logger.debug("Setting session key token to " + token);
					requestHeaders.put("x-sessionkey-token", token);
				}
			} else {
				logger.debug("No access_token specified");
			}
		}
	}
}
