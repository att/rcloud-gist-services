/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mangosolutions.rcloud.rawgist.api.GistIdGenerator;
import com.mangosolutions.rcloud.rawgist.api.GistRepository;
import com.mangosolutions.rcloud.rawgist.api.GitGistRepository;
import com.mangosolutions.rcloud.rawgist.api.UUIDGistIdGenerator;

/**
 * Main Spring configuration
 *
 */
@Configuration()
@EnableConfigurationProperties(GistServiceProperties.class)
public class GistServiceConfiguration {

	@Autowired
	private GistServiceProperties serviceProperties;
	
	
	
	@Bean
	public GistRepository getGistRepository() throws IOException {
		return new GitGistRepository(serviceProperties.getRoot(), this.getGistIdGenerator());
	}
	
	public GistIdGenerator getGistIdGenerator() {
		return new UUIDGistIdGenerator();
	}
	
}
