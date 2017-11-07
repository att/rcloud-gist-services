/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mangosolutions.rcloud.rawgist.repository.GistRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryFactory;

@Component
public class GitGistRepositoryFactory implements GistRepositoryFactory {

    @Autowired
    private GistOperationFactory gistOperationFactory;

    public GistRepository getRepository(File folder) {
        return new GitGistRepository(folder, gistOperationFactory);
    }

}
