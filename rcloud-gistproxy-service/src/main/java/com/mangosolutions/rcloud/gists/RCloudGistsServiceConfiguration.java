/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.gists;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mangosolutions.rcloud.gists.filters.AcceptContentTypeReWritingFilter;
import com.mangosolutions.rcloud.gists.filters.SessionTokenParameterToHeaderFilter;
import com.netflix.zuul.ZuulFilter;

/**
 * Main Spring configuration
 *
 */
@Configuration
@Profile("rcloudgist")
public class RCloudGistsServiceConfiguration {

	private static final String GISTS_PATH = "/gists";
	
	@Bean
	public ZuulFilter getAcceptContentTypeReWritingFilter() {
		return new AcceptContentTypeReWritingFilter(GISTS_PATH, 30);
	}

	@Bean
	public ZuulFilter getSessionTokenParameterToHeaderFilter() {
		return new SessionTokenParameterToHeaderFilter(GISTS_PATH, 40);
	}
	
	
}
