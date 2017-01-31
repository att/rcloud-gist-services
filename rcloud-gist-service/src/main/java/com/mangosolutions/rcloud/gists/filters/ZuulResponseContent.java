/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.gists.filters;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.zuul.context.RequestContext;

/**
 * Resolves the content of a response going through Zuul
 *
 */
public class ZuulResponseContent {

	private static final Logger logger = LoggerFactory.getLogger(ZuulResponseContent.class);

	private RequestContext context;

	public ZuulResponseContent(RequestContext context) {
		this.context = context;
	}

	/**
	 * Get the content from the response.
	 * @return the content obtained from the response.
	 */
	public String getContent() {
		String content = context.getResponseBody();
		if (content == null) {
			content = getBodyFromStream(context);
		}
		return content;
	}

	/**
	 * Clears the response content
	 */
	public void clearContent() {
		if(context.getResponseDataStream() != null) {
			IOUtils.closeQuietly(context.getResponseDataStream());
		}
		context.setResponseDataStream(null);
		context.setResponseBody(null);
	}

	/**
	 * Sets the content onto the response.
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.clearContent();
		context.setResponseBody(content);
	}

	private String getBodyFromStream(RequestContext context) {
		InputStream bodyStream = context.getResponseDataStream();
		if (bodyStream != null) {
			return extractAndReplaceBody(context, bodyStream);
		}
		return null;
	}

	private String extractAndReplaceBody(RequestContext context, InputStream bodyStream) {
		String body = null;
		try {
			body = IOUtils.toString(bodyStream);
			InputStream newBodyStream = IOUtils.toInputStream(body, "UTF-8");
			context.setResponseDataStream(newBodyStream);
		} catch (IOException e) {
			logger.warn("Could not extract body.", e);
		} finally {
			IOUtils.closeQuietly(bodyStream);
		}
		return body;
	}
}
