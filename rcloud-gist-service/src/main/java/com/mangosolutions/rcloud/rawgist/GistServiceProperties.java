package com.mangosolutions.rcloud.rawgist;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gists")
public class GistServiceProperties {

	private String root;
	
	private String cache = "gists";
	
	private int lockTimeout = 30;

	
	
	public int getLockTimeout() {
		return lockTimeout;
	}

	public void setLockTimeout(int lockTimeout) {
		this.lockTimeout = lockTimeout;
	}

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
