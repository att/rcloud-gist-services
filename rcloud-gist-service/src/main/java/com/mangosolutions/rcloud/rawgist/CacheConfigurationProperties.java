/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.hazelcast.config.EvictionPolicy;

@ConfigurationProperties()
public class CacheConfigurationProperties {
	
	private List<GistCacheConfiguration> caches;

    public List<GistCacheConfiguration> getCaches() {
		return caches;
	}

	public void setCaches(List<GistCacheConfiguration> caches) {
		this.caches = caches;
	}

	public static class GistCacheConfiguration { 
		
		private String name;
        private EvictionPolicy evictionPolicy = EvictionPolicy.LFU;
        private int ttl = 300;
		
        public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public EvictionPolicy getEvictionPolicy() {
			return evictionPolicy;
		}
		public void setEvictionPolicy(EvictionPolicy evictionPolicy) {
			this.evictionPolicy = evictionPolicy;
		}
		public int getTtl() {
			return ttl;
		}
		public void setTtl(int ttl) {
			this.ttl = ttl;
		}
    }
	

}
