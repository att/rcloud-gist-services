package com.mangosolutions.rcloud.rawgist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class ContentNegotiationConfiguration extends WebMvcConfigurerAdapter {

	private static final List<MediaType> GITHUB_MEDIA_TYPES = Arrays.asList(
			MediaType.parseMediaType("application/vnd.github.raw"),
			MediaType.parseMediaType("application/vnd.github.v3.raw"),
			MediaType.parseMediaType("application/vnd.github.base64"),
			MediaType.parseMediaType("application/vnd.github.v3.base64"));

	// @Override
	// public void configureContentNegotiation(ContentNegotiationConfigurer
	// configurer) {
	// configurer.ignoreAcceptHeader(true);
	// }

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
		List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
		mediaTypes.addAll(GITHUB_MEDIA_TYPES);
		converter.setSupportedMediaTypes(mediaTypes);
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
