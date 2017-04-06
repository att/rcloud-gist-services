/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.hazelcast.core.HazelcastInstance;
import com.mangosolutions.rcloud.rawgist.model.GistComment;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;

public class GitGistRepositoryService implements GistRepositoryService {

	private static final int DEFAULT_LOCK_TIMEOUT = 30;

	private int lockTimeout = DEFAULT_LOCK_TIMEOUT;

	private static final Splitter REPOSITORYID_FOLDER_SPLITTER = Splitter.fixedLength(4);

	private static final String RECYCLE_FOLDER_NAME = ".recycle";

	private Logger logger = LoggerFactory.getLogger(GitGistRepositoryService.class);

	private File repositoryRoot;
	private File recycleRoot;
	private GistIdGenerator idGenerator;
	private HazelcastInstance hazelcastInstance;
	private ObjectMapper objectMapper;
	private GistSecurityManager securityManager;

	public GitGistRepositoryService(String repositoryRoot, GistIdGenerator idGenerator,
			HazelcastInstance hazelcastInstance, ObjectMapper objectMapper) throws IOException {
		this.repositoryRoot = new File(repositoryRoot);
		if (!this.repositoryRoot.exists()) {
			FileUtils.forceMkdir(this.repositoryRoot);
		}
		recycleRoot = new File(repositoryRoot, RECYCLE_FOLDER_NAME);
		if (!this.recycleRoot.exists()) {
			FileUtils.forceMkdir(this.recycleRoot);
		}
		this.idGenerator = idGenerator;
		this.hazelcastInstance = hazelcastInstance;
		this.objectMapper = objectMapper;
	}

	public void setLockTimeout(int timeout) {
		this.lockTimeout = timeout;
	}

	public GistSecurityManager getSecurityManager() {
		return securityManager;
	}

	public void setSecurityManager(GistSecurityManager securityManager) {
		this.securityManager = securityManager;
	}

	@Override
	public List<GistResponse> listGists(UserDetails user) {
		List<GistResponse> gists = new ArrayList<GistResponse>();
		for (File file : FileUtils.listFiles(repositoryRoot,
				FileFilterUtils.and(FileFileFilter.FILE, new NameFileFilter(GitGistRepository.GIST_META_JSON_FILE)),
				TrueFileFilter.INSTANCE)) {
			GistRepository repository = new GitGistRepository(file.getParentFile(), objectMapper);
			if(this.securityManager.canWrite(repository, user)) {
				gists.add(repository.getGist(user));
			}
		}
		return gists;
	}

	@Override
	public GistResponse getGist(String gistId, UserDetails user) {
		Lock lock = acquireGistLock(gistId);
		try {
			File repositoryFolder = getAndValidateRepositoryFolder(gistId);
			GistRepository repository = new GitGistRepository(repositoryFolder, objectMapper);
			this.ensureReadable(repository, user);
			return repository.getGist(user);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public GistResponse getGist(String gistId, String commitId, UserDetails user) {
		Lock lock = acquireGistLock(gistId);
		try {
			File repositoryFolder = getAndValidateRepositoryFolder(gistId);
			GistRepository repository = new GitGistRepository(repositoryFolder, objectMapper);
			this.ensureReadable(repository, user);
			return repository.getGist(commitId, user);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public GistResponse createGist(GistRequest request, UserDetails user) {
		String gistId = idGenerator.generateId();
		File repositoryFolder = getRepositoryFolder(gistId);
		GistRepository repository = new GitGistRepository(repositoryFolder, gistId, objectMapper, user);
		return repository.createGist(request, user);
	}

	@Override
	public GistResponse forkGist(String gistToForkId, User user) {
		Lock lock = acquireGistLock(gistToForkId);
		try {
			File forkedGistRepositoryFolder = getAndValidateRepositoryFolder(gistToForkId);
			GistRepository forkedRepository = new GitGistRepository(forkedGistRepositoryFolder, objectMapper);
			this.ensureReadable(forkedRepository, user);
			String gistId = idGenerator.generateId();
			File repositoryFolder = getRepositoryFolder(gistId);
			GistRepository repository = new GitGistRepository(repositoryFolder, gistId, objectMapper, user);
			return repository.fork(forkedRepository, user);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public GistResponse editGist(String gistId, GistRequest request, UserDetails user) {
		Lock lock = acquireGistLock(gistId);
		try {
			File repositoryFolder = getAndValidateRepositoryFolder(gistId);
			GistRepository repository = new GitGistRepository(repositoryFolder, objectMapper);
			this.ensureWritable(repository, user);
			return repository.editGist(request, user);
		} finally {
			lock.unlock();
		}
	}



	@Override
	public void deleteGist(String gistId, UserDetails user) {
		Lock lock = acquireGistLock(gistId);
		try {
			File repositoryFolder = getAndValidateRepositoryFolder(gistId);
			GistRepository repository = new GitGistRepository(repositoryFolder, objectMapper);
			this.ensureWritable(repository, user);
			FileUtils.moveDirectoryToDirectory(repositoryFolder, new File(recycleRoot, gistId), true);
			FileUtils.forceDelete(repositoryFolder);
		} catch (IOException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_UPDATE_FAILURE,
					"Could not delete gist {}, an internal error has occurred", gistId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryError(error, e);
		} finally {
			lock.unlock();
		}
	}



	@Override
	public List<GistCommentResponse> getComments(String gistId, UserDetails user) {
		Lock lock = acquireGistLock(gistId);
		try {
			File repositoryFolder = getAndValidateRepositoryFolder(gistId);
			GistRepository gistRepository = new GitGistRepository(repositoryFolder, objectMapper);
			this.ensureReadable(gistRepository, user);
			GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
			return repository.getComments(user);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public GistCommentResponse getComment(String gistId, long commentId, UserDetails user) {
		Lock lock = acquireGistLock(gistId);
		try {
			File repositoryFolder = getAndValidateRepositoryFolder(gistId);
			GistRepository gistRepository = new GitGistRepository(repositoryFolder, objectMapper);
			this.ensureReadable(gistRepository, user);
			GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
			return repository.getComment(commentId, user);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public GistCommentResponse createComment(String gistId, GistComment comment, UserDetails user) {
		Lock lock = acquireGistLock(gistId);
		try {
			File repositoryFolder = getAndValidateRepositoryFolder(gistId);
			GistRepository gistRepository = new GitGistRepository(repositoryFolder, objectMapper);
			this.ensureWritable(gistRepository, user);
			GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
			return repository.createComment(comment, user);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public GistCommentResponse editComment(String gistId, long commentId, GistComment comment, UserDetails user) {
		Lock lock = acquireGistLock(gistId);
		try {
			File repositoryFolder = getAndValidateRepositoryFolder(gistId);
			GistRepository gistRepository = new GitGistRepository(repositoryFolder, objectMapper);
			this.ensureWritable(gistRepository, user);
			GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
			return repository.editComment(commentId, comment, user);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void deleteComment(String gistId, long commentId, UserDetails user) {
		Lock lock = acquireGistLock(gistId);
		try {
			File repositoryFolder = getAndValidateRepositoryFolder(gistId);
			GistRepository gistRepository = new GitGistRepository(repositoryFolder, objectMapper);
			this.ensureWritable(gistRepository, user);
			GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
			repository.deleteComment(commentId, user);
		} finally {
			lock.unlock();
		}
	}

	private Lock acquireGistLock(String gistId) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (!lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
						"Could not access gist {}, it is currently being updated", gistId);
				logger.error(error.getFormattedMessage());
				throw new GistRepositoryException(error);
			}
		} catch (InterruptedException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
					"Could not acess gist {}, it is currently being updated", gistId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
		return lock;
	}

	private File getAndValidateRepositoryFolder(String id) {
		File folder = getRepositoryFolder(id);
		if (!folder.exists()) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_NOT_EXIST, "Gist with id {} does not exist", id);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
		return folder;
	}

	private File getRepositoryFolder(String id) {
		File folder = repositoryRoot;
		for (String path : REPOSITORYID_FOLDER_SPLITTER.split(id)) {
			folder = new File(folder, path);
		}
		return folder;
	}
	
	private void ensureReadable(GistRepository repository, UserDetails user) {
		if (!this.securityManager.canRead(repository, user)) {
			GistError error = new GistError(GistErrorCode.ERR_ACL_READ_DENIED,
					"You do not have permission to read the gist with id {}.", repository.getId());
			logger.error(error.getFormattedMessage());
			throw new GistAccessDeniedException(error);
		}
	}
	
	private void ensureWritable(GistRepository repository, UserDetails user) {
		if (!this.securityManager.canWrite(repository, user)) {
			GistError error = new GistError(GistErrorCode.ERR_ACL_WRITE_DENIED,
					"You do not have permission to alter the gist with id {}.", repository.getId());
			logger.error(error.getFormattedMessage());
			throw new GistAccessDeniedException(error);
		}
	}
}
