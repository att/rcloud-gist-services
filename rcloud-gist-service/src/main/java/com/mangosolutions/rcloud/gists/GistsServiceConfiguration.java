package com.mangosolutions.rcloud.gists;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mangosolutions.rcloud.gists.filters.HeaderUrlRewritingFilter;
import com.mangosolutions.rcloud.gists.filters.JsonContentUrlRewritingFilter;
import com.netflix.zuul.ZuulFilter;

/**
 * Main Spring configuration
 *
 */
@Configuration
public class GistsServiceConfiguration {

	@Bean
	public ZuulFilter getUrlRewritingFilter() {
		return new HeaderUrlRewritingFilter();
	}
	
	@Bean
	public ZuulFilter getJsonContentUrlRewritingFilter() {
		return new JsonContentUrlRewritingFilter();
	}

}
