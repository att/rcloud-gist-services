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
		//ensure the comments folder exists
		createRootFolder(layout);
		createCommentsFolder(layout);
		createGistFolder(layout);
		initGistRepo(layout);
		return layout;
	}

	private void initGistRepo(RepositoryLayout layout) {
		File gistFolder = layout.getGistFolder();
		File gitFolder = new File(gistFolder, ".git");
		if(!gitFolder.exists()) {
			try {
				InitOp initOp = new InitOp();
				initOp.setDir(gistFolder);
				initOp.call();
			} catch (GrgitException e) {
				GistError error = new GistError(GistErrorCode.FATAL_GIST_INITIALISATION, "Could not create gist storage location for gist");
				logger.error(error.getFormattedMessage() + " with folder path {}", gistFolder);
				throw new GistRepositoryError(error, e);
			}
		}
	}

	private void createCommentsFolder(RepositoryLayout layout) {
		mkdir(layout.getCommentsFolder());
	}

	private void createGistFolder(RepositoryLayout layout) {
		mkdir(layout.getGistFolder());
	}

	private void createRootFolder(RepositoryLayout layout) {
		mkdir(layout.getRootFolder());
	}

	private void mkdir(File dir) {
		if (!dir.exists()) {
			try {
				FileUtils.forceMkdir(dir);
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.FATAL_GIST_INITIALISATION, "Could not create gist storage location for gist");
				logger.error(error.getFormattedMessage() + " with folder path {}", dir);
				throw new GistRepositoryError(error, e);
			}
		}
	}

}
