package com.mangosolutions.rcloud.gists.filters;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.netflix.zuul.context.RequestContext;


public class ZuulResponseContentTest {

	private RequestContext context = null;
	
	
	@Before
	public void setup() {
		context = new RequestContext();
	}
	
	@Test
	public void testGetContentFromText() {
		context.setResponseBody("I am some test text");
		ZuulResponseContent content = new ZuulResponseContent(context);
		Assert.assertEquals("I am some test text", content.getContent());
	}
	
	@Test
	public void testGetContentFromStream() {
		context.setResponseDataStream(IOUtils.toInputStream("I am some test text"));
		ZuulResponseContent content = new ZuulResponseContent(context);
		Assert.assertEquals("I am some test text", content.getContent());
	}
	
	@Test
	public void testSetContent() throws IOException {
		ZuulResponseContent content = new ZuulResponseContent(context);
		content.setContent("I am some test text");
		Assert.assertNull(context.getResponseDataStream());
		Assert.assertEquals("I am some test text", context.getResponseBody());
	}
	
	@Test
	public void testClearContentWithNullTextAndStream() {
		context.setResponseBody(null);
		context.setResponseDataStream(null);
		ZuulResponseContent content = new ZuulResponseContent(context);
		content.clearContent();
		Assert.assertNull(content.getContent());
	}
	
	@Test
	public void testClearContentWithStream() {
		context.setResponseBody(null);
		context.setResponseDataStream(IOUtils.toInputStream("I am some test text"));
		ZuulResponseContent content = new ZuulResponseContent(context);
		content.clearContent();
		Assert.assertNull(content.getContent());
	}
	
	@Test
	public void testClearContentWithText() {
		context.setResponseBody("I am some test text");
		context.setResponseDataStream(null);
		ZuulResponseContent content = new ZuulResponseContent(context);
		content.clearContent();
		Assert.assertNull(content.getContent());
	}
}
