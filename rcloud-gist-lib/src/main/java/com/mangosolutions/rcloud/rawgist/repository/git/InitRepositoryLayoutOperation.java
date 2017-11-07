/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.ajoberstar.grgit.exception.GrgitException;
import org.ajoberstar.grgit.operation.InitOp;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistErrorCode;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryError;

public class InitRepositoryLayoutOperation implements Callable<RepositoryLayout> {

    private static final Logger logger = LoggerFactory.getLogger(InitRepositoryLayoutOperation.class);

    private File repositoryRoot;

    public InitRepositoryLayoutOperation(File repositoryRoot) {
        this.repositoryRoot = repositoryRoot;
    }

    public File getRepositoryRoot() {
        return repositoryRoot;
    }

    public void setRepositoryRoot(File repositoryRoot) {
        this.repositoryRoot = repositoryRoot;
    }

    @Override
    public RepositoryLayout call() {
        RepositoryLayout layout = new RepositoryLayout(repositoryRoot);
        // ensure the comments folder exists
        createRootFolder(layout);
        createCommentsFolder(layout);
        createGistFolder(layout);
        createWorkingFolder(layout);
        initGistRepo(layout);
        return layout;
    }

    private void initGistRepo(RepositoryLayout layout) {
        File bareFolder = layout.getBareFolder();
        if (bareFolder.list().length == 0) {
            try {
                InitOp initOp = new InitOp();
                initOp.setDir(bareFolder);
                initOp.setBare(true);
                initOp.call();
            } catch (GrgitException e) {
                GistError error = new GistError(GistErrorCode.FATAL_GIST_INITIALISATION,
                        "Could not create gist storage location for gist");
                logger.error(error.getFormattedMessage() + " with folder path {}", bareFolder);
                throw new GistRepositoryError(error, e);
            }
        }
    }

    private void createCommentsFolder(RepositoryLayout layout) {
        mkdir(layout.getCommentsFolder());
    }

    private void createGistFolder(RepositoryLayout layout) {
        mkdir(layout.getBareFolder());
    }

    private void createWorkingFolder(RepositoryLayout layout) {
        mkdir(layout.getWorkingFolder());
    }

    private void createRootFolder(RepositoryLayout layout) {
        mkdir(layout.getRootFolder());
    }

    private void mkdir(File dir) {
        if (!dir.exists()) {
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                GistError error = new GistError(GistErrorCode.FATAL_GIST_INITIALISATION,
                        "Could not create gist storage location for gist");
                logger.error(error.getFormattedMessage() + " with folder path {}", dir);
                throw new GistRepositoryError(error, e);
            }
        }
    }

}
