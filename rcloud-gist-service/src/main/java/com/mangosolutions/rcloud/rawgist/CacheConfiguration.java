/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.mangosolutions.rcloud.rawgist.CacheConfigurationProperties.GistCacheConfiguration;

@Configuration
@EnableConfigurationProperties(CacheConfigurationProperties.class)
public class CacheConfiguration {

	private final Logger logger = LoggerFactory.getLogger(CacheConfiguration.class);
	
	@Autowired
	private CacheConfigurationProperties cacheConfigurationProperties;

	@Autowired
	private HazelcastInstance hazelcastInstance;
	
	@Bean
    public HazelcastCacheManager cacheManager() {
		HazelcastCacheManager cacheManager = new HazelcastCacheManager(hazelcastInstance);
		configureCaches(cacheManager);
		return cacheManager;
    }
	
	public void configureCaches(HazelcastCacheManager hazelcastCacheManager) {
		HazelcastInstance hazelcastInstance = hazelcastCacheManager.getHazelcastInstance();
		for(GistCacheConfiguration cacheConfig: cacheConfigurationProperties.getCaches()) {
			String name = cacheConfig.getName();
			if(!StringUtils.isEmpty(name)) {
				createAndApplyCacheConfiguration(hazelcastInstance, cacheConfig);
			}
		}
	}

	private void createAndApplyCacheConfiguration(HazelcastInstance hazelcastInstance, GistCacheConfiguration cacheConfig) {
		Config config = hazelcastInstance.getConfig();
		Map<String, MapConfig> mapConfigs = config.getMapConfigs();
		String cacheName = cacheConfig.getName();
		if(mapConfigs.containsKey(cacheName)) {
			logger.warn("Altering existing configuration of cache config {}", mapConfigs.get(cacheName));
		}
		MapConfig mapConfig = new MapConfig(cacheName);//config.getMapConfig(cacheConfig.getName());
		mapConfig.setEvictionPolicy(cacheConfig.getEvictionPolicy());
		mapConfig.setTimeToLiveSeconds(cacheConfig.getTtl());
		config.addMapConfig(mapConfig);
		logger.info("Configured cache {} with with settings: {}", cacheName, mapConfig);
	}

}
