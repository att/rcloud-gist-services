package com.mangosolutions.rcloud.sessionkeyauth;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;


public class SessionKeyServerMessageConverterTest {

	@Test
	public void testYesResponse() throws IOException {
		HttpInputMessage mockInputMessage = Mockito.mock(HttpInputMessage.class);
		Mockito.when(mockInputMessage.getBody()).thenReturn(IOUtils.toInputStream("YES\ntheuser\nthesource\n", "UTF-8"));
		
		SessionKeyServerMessageConverter converter = new SessionKeyServerMessageConverter();
		SessionKeyServerResponse response = converter.readInternal(SessionKeyServerResponse.class, mockInputMessage);
		Assert.assertNotNull(response);
		Assert.assertEquals(SessionKeyServerResult.YES, response.getResult());
		Assert.assertEquals("theuser", response.getName());
		Assert.assertEquals("thesource", response.getSource());
	}
	
	@Test
	public void testNoResponse() throws IOException {
		HttpInputMessage mockInputMessage = Mockito.mock(HttpInputMessage.class);
		Mockito.when(mockInputMessage.getBody()).thenReturn(IOUtils.toInputStream("NO", "UTF-8"));
		
		SessionKeyServerMessageConverter converter = new SessionKeyServerMessageConverter();
		SessionKeyServerResponse response = converter.readInternal(SessionKeyServerResponse.class, mockInputMessage);
		Assert.assertNotNull(response);
		Assert.assertEquals(SessionKeyServerResult.NO, response.getResult());
		Assert.assertNull(response.getName());
		Assert.assertNull(response.getSource());
	}
	
	@Test
	public void testSuperceededResponse() throws IOException {
		HttpInputMessage mockInputMessage = Mockito.mock(HttpInputMessage.class);
		Mockito.when(mockInputMessage.getBody()).thenReturn(IOUtils.toInputStream("SUPERCEEDED\ntheuser", "UTF-8"));
		
		SessionKeyServerMessageConverter converter = new SessionKeyServerMessageConverter();
		SessionKeyServerResponse response = converter.readInternal(SessionKeyServerResponse.class, mockInputMessage);
		Assert.assertNotNull(response);
		Assert.assertEquals(SessionKeyServerResult.SUPERCEEDED, response.getResult());
		Assert.assertEquals("theuser", response.getName());
		Assert.assertNull(response.getSource());
	}
	
	@Test
	public void testSuperceededResponseWithExtraLines() throws IOException {
		HttpInputMessage mockInputMessage = Mockito.mock(HttpInputMessage.class);
		Mockito.when(mockInputMessage.getBody()).thenReturn(IOUtils.toInputStream("SUPERCEEDED\ntheuser\n\n\n\n\n", "UTF-8"));
		
		SessionKeyServerMessageConverter converter = new SessionKeyServerMessageConverter();
		SessionKeyServerResponse response = converter.readInternal(SessionKeyServerResponse.class, mockInputMessage);
		Assert.assertNotNull(response);
		Assert.assertEquals(SessionKeyServerResult.SUPERCEEDED, response.getResult());
		Assert.assertEquals("theuser", response.getName());
		Assert.assertNull(response.getSource());
	}
	
	@Test
	public void testNotSupportMapObject() throws IOException {
		SessionKeyServerMessageConverter converter = new SessionKeyServerMessageConverter();
		Assert.assertFalse(converter.supports(Map.class));
	}
	
	@Test
	public void testNotSupportSessionKeyServerResponseObject() throws IOException {
		SessionKeyServerMessageConverter converter = new SessionKeyServerMessageConverter();
		Assert.assertTrue(converter.supports(SessionKeyServerResponse.class));
	}
	
	@Test(expected=HttpMessageNotReadableException.class)
	public void testBadEnumValue() throws IOException {
		HttpInputMessage mockInputMessage = Mockito.mock(HttpInputMessage.class);
		Mockito.when(mockInputMessage.getBody()).thenReturn(IOUtils.toInputStream("BANANA\ntheuser", "UTF-8"));
		
		SessionKeyServerMessageConverter converter = new SessionKeyServerMessageConverter();
		converter.readInternal(SessionKeyServerResponse.class, mockInputMessage);
		Assert.fail("Should have failed to parse the response enum.");
	}
	
	@Test(expected=HttpMessageNotReadableException.class)
	public void testEmptyBodyContent() throws IOException {
		HttpInputMessage mockInputMessage = Mockito.mock(HttpInputMessage.class);
		Mockito.when(mockInputMessage.getBody()).thenReturn(IOUtils.toInputStream("", "UTF-8"));
		
		SessionKeyServerMessageConverter converter = new SessionKeyServerMessageConverter();
		converter.readInternal(SessionKeyServerResponse.class, mockInputMessage);
		Assert.fail("Should have failed to parse the response enum.");
	}
	
	@Test(expected=HttpMessageNotReadableException.class)
	public void testEmptyBodyLinesContent() throws IOException {
		HttpInputMessage mockInputMessage = Mockito.mock(HttpInputMessage.class);
		Mockito.when(mockInputMessage.getBody()).thenReturn(IOUtils.toInputStream("\n\n\n\n\n\n\n", "UTF-8"));
		
		SessionKeyServerMessageConverter converter = new SessionKeyServerMessageConverter();
		converter.readInternal(SessionKeyServerResponse.class, mockInputMessage);
		Assert.fail("Should have failed to parse the response enum.");
	}
	
	
}
