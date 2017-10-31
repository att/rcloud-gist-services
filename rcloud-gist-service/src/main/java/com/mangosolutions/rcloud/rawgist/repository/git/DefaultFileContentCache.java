/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.mangosolutions.rcloud.rawgist.model.FileContent;

@Component
public class DefaultFileContentCache implements FileContentCache {

    @Override
    @Cacheable(value = "filecontentcache", key = "{#contentId, #path}")
    public FileContent load(String contentId, String path) {
        return null;
    }

    @Override
    @CachePut(cacheNames = "filecontentcache", key = "{#contentId, #path}")
    public FileContent save(String contentId, String path, FileContent content) {
        return content;
    }

}
