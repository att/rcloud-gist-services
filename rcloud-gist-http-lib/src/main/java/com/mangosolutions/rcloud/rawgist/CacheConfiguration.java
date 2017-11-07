/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.mangosolutions.rcloud.rawgist.CacheConfigurationProperties.GistCacheConfiguration;

@Configuration
@EnableConfigurationProperties(CacheConfigurationProperties.class)
public class CacheConfiguration {

    private static final String RANDOM_GROUP_NAME = "random";

    private final Logger logger = LoggerFactory.getLogger(CacheConfiguration.class);

    @Autowired
    private CacheConfigurationProperties cacheConfigurationProperties;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Bean
    @RefreshScope
    public HazelcastCacheManager cacheManager() {
        HazelcastCacheManager cacheManager = new HazelcastCacheManager(hazelcastInstance);
        configureGroupName();
        configureCaches(hazelcastInstance);
        return cacheManager;
    }


    private void configureGroupName() {
        GroupConfig groupConfig = hazelcastInstance.getConfig().getGroupConfig();
        String name = groupConfig.getName();
        if (RANDOM_GROUP_NAME.equalsIgnoreCase(name)) {
            String groupName = UUID.randomUUID().toString();
            String groupPass = UUID.randomUUID().toString();
            groupConfig.setName(groupName);
            groupConfig.setPassword(groupPass);
        }

    }


    public void configureCaches(HazelcastInstance hazelcastInstance) {
        for (GistCacheConfiguration cacheConfig : cacheConfigurationProperties.getCaches()) {
            String name = cacheConfig.getName();
            if (!StringUtils.isEmpty(name)) {
                createAndApplyCacheConfiguration(hazelcastInstance, cacheConfig);
            }
        }
    }

    private void createAndApplyCacheConfiguration(HazelcastInstance hazelcastInstance,
            GistCacheConfiguration cacheConfig) {
        Config config = hazelcastInstance.getConfig();
        Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        MapConfig defaultMapConfig = getDefaultMapConfig(mapConfigs);
        String cacheName = cacheConfig.getName();
        emptyCache(cacheName, hazelcastInstance);
        MapConfig mapConfig = null;
        if (mapConfigs.containsKey(cacheName)) {
            mapConfig = mapConfigs.get(cacheName);
            logger.warn("Altering existing configuration of cache config {} with values {}", mapConfig, cacheConfig);
        } else {
            mapConfig = new MapConfig(defaultMapConfig);
            mapConfig.setName(cacheName);
            mapConfig.setEvictionPolicy(cacheConfig.getEvictionPolicy());
            mapConfig.setTimeToLiveSeconds(cacheConfig.getTtl());
            mapConfig.setMaxIdleSeconds(cacheConfig.getMaxIdleSeconds());
            MaxSizeConfig maxSizeConfig = mapConfig.getMaxSizeConfig();
            maxSizeConfig.setSize(cacheConfig.getMaxSize());
            mapConfig.setMaxSizeConfig(maxSizeConfig);
        }
        config.addMapConfig(mapConfig);
        
        
        logger.info("Configured cache {} with with settings: {}", cacheName, mapConfig);
    }

    private MapConfig getDefaultMapConfig(Map<String, MapConfig> mapConfigs) {
        MapConfig mapConfig = null;
        if (mapConfigs.containsKey("default")) {
            mapConfig = mapConfigs.get("default");
        } else {
            mapConfig = new MapConfig("default");
        }
        return mapConfig;
    }


    private void emptyCache(String cacheName, HazelcastInstance hazelcastInstance2) {
        IMap<Object, Object> mapCache = hazelcastInstance.getMap(cacheName);
        if(mapCache != null) {
            logger.info("Clearing {} entries from cache '{}'", mapCache.keySet().size(), cacheName);
            mapCache.clear();
        } else {
            logger.info("No cache defined called: '{}'", cacheName);
        }
    }
    
    @EventListener
    public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
        logger.info("Reconfiguring caches after refresh event");
        this.configureCaches(hazelcastInstance);
        
    }

}
