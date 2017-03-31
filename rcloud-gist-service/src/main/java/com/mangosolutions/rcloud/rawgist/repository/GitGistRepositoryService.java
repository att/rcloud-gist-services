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
import com.mangosolutions.rcloud.rawgist.repository.GistError.GistErrorCode;

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

	@Override
	public List<GistResponse> listGists(UserDetails userDetails) {
		List<GistResponse> gists = new ArrayList<GistResponse>();
		for (File file : FileUtils.listFiles(repositoryRoot,
				FileFilterUtils.and(FileFileFilter.FILE, new NameFileFilter(GitGistRepository.GIST_META_JSON_FILE)),
				TrueFileFilter.INSTANCE)) {
			GistRepository repository = new GitGistRepository(file.getParentFile(), objectMapper);
			gists.add(repository.getGist(userDetails));
		}
		return gists;
	}

	@Override
	public GistResponse getGist(String gistId, UserDetails userDetails) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getAndValidateRepositoryFolder(gistId);
					GistRepository repository = new GitGistRepository(repositoryFolder, objectMapper);
					return repository.getGist(userDetails);
				} finally {
					lock.unlock();
				}
			} else {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
						"Could not read gist {}, it is currently being updated", gistId);
				logger.error(error.getFormattedMessage());
				throw new GistRepositoryException(error);
			}
		} catch (InterruptedException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
					"Could not read gist {}, it is currently being updated", gistId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
	}

	@Override
	public GistResponse getGist(String gistId, String commitId, UserDetails userDetails) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getAndValidateRepositoryFolder(gistId);
					GistRepository repository = new GitGistRepository(repositoryFolder, objectMapper);
					return repository.getGist(commitId, userDetails);
				} finally {
					lock.unlock();
				}
			} else {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
						"Could not read gist {}, it is currently being updated", gistId);
				logger.error(error.getFormattedMessage());
				throw new GistRepositoryException(error);
			}
		} catch (InterruptedException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
					"Could not read gist {}, it is currently being updated", gistId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
	}

	@Override
	public GistResponse createGist(GistRequest request, UserDetails activeUser) {
		String gistId = idGenerator.generateId();
		File repositoryFolder = getRepositoryFolder(gistId);
		GistRepository repository = new GitGistRepository(repositoryFolder, gistId, objectMapper, activeUser);
		return repository.createGist(request, activeUser);
	}

	@Override
	public GistResponse forkGist(String gistToForkId, User activeUser) {
		Lock lock = hazelcastInstance.getLock(gistToForkId);
		try {
			if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				try {
					File forkedGistRepositoryFolder = getAndValidateRepositoryFolder(gistToForkId);
					GistRepository forkedRepository = new GitGistRepository(forkedGistRepositoryFolder, objectMapper);
					String gistId = idGenerator.generateId();
					File repositoryFolder = getRepositoryFolder(gistId);
					GistRepository repository = new GitGistRepository(repositoryFolder, gistId, objectMapper, activeUser);
					return repository.fork(forkedRepository, activeUser);
				} finally {
					lock.unlock();
				}
			} else {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
						"Could not read gist {}, it is currently being updated", gistToForkId);
				logger.error(error.getFormattedMessage());
				throw new GistRepositoryException(error);
			}
		} catch (InterruptedException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
					"Could not read gist {}, it is currently being updated", gistToForkId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
	}
	
	@Override
	public GistResponse editGist(String gistId, GistRequest request, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getAndValidateRepositoryFolder(gistId);
					GistRepository repository = new GitGistRepository(repositoryFolder, objectMapper);
					return repository.editGist(request, activeUser);
				} finally {
					lock.unlock();
				}
			} else {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
						"Could not update gist {}, it is currently being updated", gistId);
				logger.error(error.getFormattedMessage());
				throw new GistRepositoryException(error);
			}
		} catch (InterruptedException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
					"Could not update gist {}, it is currently being updated", gistId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
	}

	@Override
	public void deleteGist(String gistId, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getAndValidateRepositoryFolder(gistId);
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
			} else {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
						"Could not delete gist {}, it is currently being updated", gistId);
				logger.error(error.getFormattedMessage());
				throw new GistRepositoryException(error);
			}

		} catch (InterruptedException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
					"Could not delete gist {}, it is currently being updated", gistId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
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

	@Override
	public List<GistCommentResponse> getComments(String gistId, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getAndValidateRepositoryFolder(gistId);
					GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId,
							objectMapper);
					return repository.getComments(activeUser);
				} finally {
					lock.unlock();
				}
			} else {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
						"Could not read comments for gist {}, it is currently being updated", gistId);
				logger.error(error.getFormattedMessage());
				throw new GistRepositoryException(error);
			}
		} catch (InterruptedException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
					"Could not read comments for gist {}, it is currently being updated", gistId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
	}

	@Override
	public GistCommentResponse getComment(String gistId, long commentId, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getAndValidateRepositoryFolder(gistId);
					GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId,
							objectMapper);
					return repository.getComment(commentId, activeUser);
				} finally {
					lock.unlock();
				}
			} else {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
						"Could not read comment {} on gist {}, it is currently being updated", commentId, gistId);
				logger.error(error.getFormattedMessage());
				throw new GistRepositoryException(error);
			}
		} catch (InterruptedException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
					"Could not read comment {} on gist {}, it is currently being updated", commentId, gistId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
	}

	@Override
	public GistCommentResponse createComment(String gistId, GistComment comment, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getAndValidateRepositoryFolder(gistId);
					GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId,
							objectMapper);
					return repository.createComment(comment, activeUser);
				} finally {
					lock.unlock();
				}
			} else {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
						"Could not create gist {} with new comment, it is currently being updated", gistId);
				logger.error(error.getFormattedMessage());
				throw new GistRepositoryException(error);
			}
		} catch (InterruptedException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
					"Could not create gist {} with new comment, it is currently being updated", gistId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
	}

	@Override
	public GistCommentResponse editComment(String gistId, long commentId, GistComment comment, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getAndValidateRepositoryFolder(gistId);
					GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId,
							objectMapper);
					return repository.editComment(commentId, comment, activeUser);
				} finally {
					lock.unlock();
				}
			} else {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
						"Could not edit comment {} for gist {}, it is currently being updated", commentId, gistId);
				logger.error(error.getFormattedMessage());
				throw new GistRepositoryException(error);
			}
		} catch (InterruptedException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
					"Could not edit comment {} for gist {}, it is currently being updated", commentId, gistId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
	}

	@Override
	public void deleteComment(String gistId, long commentId, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getAndValidateRepositoryFolder(gistId);
					GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId,
							objectMapper);
					repository.deleteComment(commentId, activeUser);
				} finally {
					lock.unlock();
				}
			} else {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
						"Could not delete comment {} on gist {}, it is currently being updated", commentId, gistId);
				logger.error(error.getFormattedMessage());
				throw new GistRepositoryException(error);
			}
		} catch (InterruptedException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_AVAILABLE,
					"Could not delete comment {} on gist {}, it is currently being updated", commentId, gistId);
			logger.error(error.getFormattedMessage());
			throw new GistRepositoryException(error);
		}
	}

	

}
