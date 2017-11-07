/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableConfigurationProperties(GistServiceProperties.class)
public class ContentNegotiationConfiguration extends WebMvcConfigurerAdapter {

	private static final List<MediaType> DEFAULT_GITHUB_MEDIA_TYPES = Arrays.asList(
			MediaType.parseMediaType("application/vnd.github.beta+json"),
			MediaType.parseMediaType("application/vnd.github.beta"),
			MediaType.parseMediaType("application/vnd.github.v3+json"),
			MediaType.parseMediaType("application/vnd.github.v3")
			);

	@Autowired
	private GistServiceProperties serviceProperties;

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.ignoreAcceptHeader(true);
		configurer.defaultContentType(MediaType.APPLICATION_JSON_UTF8);
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		super.extendMessageConverters(converters);
		for (HttpMessageConverter<?> converter : converters) {
			if (isJsonConverter(converter)) {
				updateMediaTypes((AbstractJackson2HttpMessageConverter) converter);
			}
		}
	}

	private void updateMediaTypes(AbstractJackson2HttpMessageConverter converter) {
		Collection<MediaType> gistMediaTypes = getMediaTypes(converter);
		List<MediaType> mediaTypes = new ArrayList<>(gistMediaTypes);
		converter.setSupportedMediaTypes(mediaTypes);
	}

	private Collection<MediaType> getMediaTypes(AbstractJackson2HttpMessageConverter converter) {
		Set<MediaType> mediaTypes = new LinkedHashSet<>(converter.getSupportedMediaTypes());
		for(String mediaType: serviceProperties.getMediatypes()) {
			mediaTypes.add(MediaType.parseMediaType(mediaType));
		}
		mediaTypes.addAll(DEFAULT_GITHUB_MEDIA_TYPES);
		return mediaTypes;
	}

	private boolean isJsonConverter(HttpMessageConverter<?> converter) {
		if(AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converter.getClass())) {
			for (MediaType mediaType : converter.getSupportedMediaTypes()) {
				if (mediaType.equals(MediaType.APPLICATION_JSON)) {
					return true;
				}
			}
		}
		return false;
	}

}
