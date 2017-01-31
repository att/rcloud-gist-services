/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.gists.filters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;

public class HeaderUrlRewritingFilterTest {


	@Before
	public void setupHeaders() throws MalformedURLException {
		RequestContext context = RequestContext.getCurrentContext();
		//add link header
		context.addZuulResponseHeader("Link", "<https://api.github.com/repositories/1523106/issues?state=closed&page=2>; rel=\"next\", <https://api.github.com/repositories/1523106/issues?state=closed&page=19>; rel=\"last\"");
		//add location header
		context.addZuulResponseHeader("Location", "https://api.github.com");
		//add another header
		context.addZuulResponseHeader("Content-Type", "application/json");

		context.addZuulResponseHeader("Blacklist-Header", "https://api.github.com");

		context.addZuulRequestHeader("x-forwarded-host", "localhost:8080");
		context.addZuulRequestHeader("x-forwarded-proto", "http");
		context.addZuulRequestHeader("x-forwarded-port", "8080");
		context.setRouteHost(new URL("https://api.github.com"));
	}

	@Test
	public void filterHeaders() {
		RequestContext context = RequestContext.getCurrentContext();
		Assert.assertNotNull(context);
		HeaderUrlRewritingFilter filter = new HeaderUrlRewritingFilter(1);
		filter.run();
		List<Pair<String, String>> headers = context.getZuulResponseHeaders();
		for(Pair<String, String> header: headers) {
			if(header.first().startsWith("Blacklist")) {
				Assert.assertTrue(header.second().contains("https://api.github.com"));
			} else if(filter.getWhitelist().contains(header.first())) {
				Assert.assertTrue(header.second().contains("http://localhost:8080"));
				Assert.assertFalse(header.second().contains("https://api.github.com"));
			} else {
				Assert.assertFalse(header.second().contains("https://api.github.com"));
			}
		}

	}

}
