/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.git.http;

import java.io.File;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.OpenOp;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;

import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryException;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;
import com.mangosolutions.rcloud.rawgist.repository.git.RepositoryLayout;

public class GistRepositoryResolver<HttpServletRequest> implements RepositoryResolver<HttpServletRequest> {

    private GistRepositoryService gistRepositoryService = null;

    public GistRepositoryResolver(GistRepositoryService gistRepositoryService) {
        this.gistRepositoryService = gistRepositoryService;
    }

    @Override
    public Repository open(final HttpServletRequest req, final String name)
            throws RepositoryNotFoundException, ServiceNotEnabledException {
        // resolve the storage location
        File gistRepositoryFolder = this.getGistRepositoryFolder(name);
        OpenOp openOp = new OpenOp();
        openOp.setDir(gistRepositoryFolder);
        Grgit git = openOp.call();
        return git.getRepository().getJgit().getRepository();
    }

    private File getGistRepositoryFolder(String name) throws RepositoryNotFoundException {
        File gistRepositoryFolder = getRepositoryRootLocation(name);
        return getGitRepositoryFolder(gistRepositoryFolder, name);
    }

    private File getGitRepositoryFolder(File gistRepositoryFolder, String name) throws RepositoryNotFoundException {
        File gitRepositoryFolder;
        RepositoryLayout layout = new RepositoryLayout(gistRepositoryFolder);
        gitRepositoryFolder = layout.getBareFolder();
        if (!gitRepositoryFolder.exists()) {
            throw new RepositoryNotFoundException(name);
        }
        return gitRepositoryFolder;
    }

    private File getRepositoryRootLocation(String name) throws RepositoryNotFoundException {
        try {
            return this.gistRepositoryService.getRepositoryFolder(name);
        } catch (GistRepositoryException e)  {
            throw new RepositoryNotFoundException(name, e);
        }
    }

}
