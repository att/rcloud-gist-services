/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.gists.filters;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public final class AcceptContentTypeReWritingFilter extends ZuulFilter {

	private static final Logger logger = LoggerFactory.getLogger(AcceptContentTypeReWritingFilter.class);

	private int order = 100;


	public AcceptContentTypeReWritingFilter(int order) {
		this.order = order;
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
			rewriteHeaders(RequestContext.getCurrentContext());
		} catch (final Exception e) {
			Throwables.propagate(e);
		}
		return null;
	}

	private static void rewriteHeaders(final RequestContext context) {
		Map<String, String> requestHeaders = context.getZuulRequestHeaders();
		replaceHeader(requestHeaders, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		replaceHeader(requestHeaders, HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
	}

	private static void replaceHeader(Map<String, String> requestHeaders, String headerName, String headerValue) {
		String originalValue = requestHeaders.get(headerName);
		if(StringUtils.isNotBlank(originalValue)) {
			logger.debug("Request header {} has value {}", headerName, originalValue);
			requestHeaders.remove(headerName);
			requestHeaders.put("X-Original-" + headerName, originalValue);
		}
		logger.debug("Replacing header {} with value {}", headerName, headerValue);
		requestHeaders.put(headerName, headerValue);
	}

}
