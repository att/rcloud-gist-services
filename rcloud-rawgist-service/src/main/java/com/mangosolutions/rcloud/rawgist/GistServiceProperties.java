package com.mangosolutions.rcloud.rawgist;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gists")
public class GistServiceProperties {

	private String root;
	
	private String cache = "gists";

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getCache() {
		return cache;
	}

	public void setCache(String cacheName) {
		this.cache = cacheName;
	}
	
	
	
}
