/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.gists.filters;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

import com.netflix.zuul.context.RequestContext;

public class JsonContentUrlRewritingFilterTest {

	private static final String REMOTE_URL = "https://api.github.com";
	private static final String LOCAL_URL = "http://localhost:8080";

	@Before
	public void setup() throws MalformedURLException {
		setup(MediaType.APPLICATION_JSON_UTF8_VALUE);
	}

	public void setup(String contentType) throws MalformedURLException {
		RequestContext context = RequestContext.getCurrentContext();
		context.getZuulResponseHeaders().clear();
		context.addZuulRequestHeader("x-forwarded-host", "localhost:8080");
		context.addZuulRequestHeader("x-forwarded-proto", "http");
		context.addZuulRequestHeader("x-forwarded-port", "8080");
		context.addZuulResponseHeader("Content-Type", contentType);
		context.setRouteHost(new URL("https://api.github.com"));
	}

	public void setContent(String content) {
		RequestContext.getCurrentContext().setResponseBody(content);
	}

	@Test
	public void testShouldFilterJsonContent() {
		Assert.assertTrue(new JsonContentUrlRewritingFilter(1).shouldFilter());
	}

	@Test
	public void testShouldNotFilterXmlContent() throws MalformedURLException {
		setup(MediaType.APPLICATION_XML_VALUE);
		Assert.assertFalse(new JsonContentUrlRewritingFilter(1).shouldFilter());
	}

	@Test
	public void testShouldFilterJsonExtensionContent() throws MalformedURLException {
		setup("application/hal+json");
		Assert.assertTrue(new JsonContentUrlRewritingFilter(1).shouldFilter());
	}

	@Test
	public void replaceUrlInJsonArray() throws MalformedURLException {
		String jsonArray = "[ \"https://api.github.com\" ]";
		setContent(jsonArray);
		JsonContentUrlRewritingFilter filter = new JsonContentUrlRewritingFilter(1);
		filter.run();
		String responseBody = RequestContext.getCurrentContext().getResponseBody();
		Assert.assertFalse(responseBody.contains(REMOTE_URL));
		Assert.assertTrue(responseBody.contains(LOCAL_URL));
	}

	@Test
	public void replaceUrlInJsonObject() {
		String jsonArray = "{ \"url\": \"https://api.github.com\" }";
		setContent(jsonArray);
		JsonContentUrlRewritingFilter filter = new JsonContentUrlRewritingFilter(1);
		filter.run();
		String responseBody = RequestContext.getCurrentContext().getResponseBody();
		Assert.assertFalse(responseBody.contains(REMOTE_URL));
		Assert.assertTrue(responseBody.contains(LOCAL_URL));
	}

	@Test
	public void replaceUrlInNestedJsonObject() {
		String jsonArray = "{ \"obj\": { \"url\": \"https://api.github.com\" } }";
		setContent(jsonArray);
		JsonContentUrlRewritingFilter filter = new JsonContentUrlRewritingFilter(1);
		filter.run();
		String responseBody = RequestContext.getCurrentContext().getResponseBody();
		Assert.assertFalse(responseBody.contains(REMOTE_URL));
		Assert.assertTrue(responseBody.contains(LOCAL_URL));
	}

	@Test
	public void replaceUrlInSpecificJsonObject() {
		String jsonArray = "{ \"obj\": { \"url\": \"https://api.github.com\", \"noUrl\": \"I am not a url\" } }";
		setContent(jsonArray);
		JsonContentUrlRewritingFilter filter = new JsonContentUrlRewritingFilter(1);
		filter.run();
		String responseBody = RequestContext.getCurrentContext().getResponseBody();
		Assert.assertFalse(responseBody.contains(REMOTE_URL));
		Assert.assertTrue(responseBody.contains(LOCAL_URL));
		Assert.assertTrue(responseBody.contains("I am not a url"));
	}

}
