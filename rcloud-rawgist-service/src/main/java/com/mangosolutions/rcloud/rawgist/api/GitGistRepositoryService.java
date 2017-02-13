package com.mangosolutions.rcloud.rawgist.api;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.FileUtils;

import com.hazelcast.core.HazelcastInstance;

public class GitGistRepositoryService implements GistRepositoryService {

	private static final String RECYCLE_FOLDER_NAME = ".recycle";

	private File repositoryRoot;
	private File recycleRoot;
	private GistIdGenerator idGenerator;
	private HazelcastInstance hazelcastInstance;

	public GitGistRepositoryService(String repositoryRoot, GistIdGenerator idGenerator,
			HazelcastInstance hazelcastInstance) throws IOException {
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
	}

	@Override
	public void listGists() {
		throw new UnsupportedOperationException("Listing available gists is not yet supported.");
	}

	@Override
	public GistResponse getGist(String gistId) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getRepositoryFolder(gistId);
					GistRepository repository = new GitGistRepository(repositoryFolder, gistId);
					return repository.getGist();
				} finally {
					lock.unlock();
				}
			} else {
				throw new RuntimeException("Could not acquire write lock for gist " + gistId);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Could not acquire write lock for gist " + gistId);
		}
	}

	@Override
	public GistResponse createGist(GistRequest request) {
		String gistId = idGenerator.generateId();
		File repositoryFolder = getRepositoryFolder(gistId);
		GistRepository repository = new GitGistRepository(repositoryFolder, gistId);
		return repository.createGist(request);
	}

	@Override
	public GistResponse editGist(String gistId, GistRequest request) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getRepositoryFolder(gistId);
					GistRepository repository = new GitGistRepository(repositoryFolder, gistId);
					return repository.editGist(request);
				} finally {
					lock.unlock();
				}
			} else {
				throw new RuntimeException("Could not acquire write lock for gist " + gistId);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Could not acquire write lock for gist " + gistId);
		}
	}

	@Override
	public void deleteGist(String gistId) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getRepositoryFolder(gistId);
					FileUtils.moveDirectoryToDirectory(repositoryFolder, new File(recycleRoot, gistId), true);
					FileUtils.forceDelete(repositoryFolder);
				} catch (IOException e) {
					throw new RuntimeException("Could not delete gist.", e);
				} finally {
					lock.unlock();
				}
			} else {
				throw new RuntimeException("Could not acquire write lock for gist " + gistId);
			}

		} catch (InterruptedException e) {
			throw new RuntimeException("Could not acquire write lock for gist " + gistId);
		}
	}

	private File getRepositoryFolder(String id) {
		String[] paths = id.split("-");
		File folder = repositoryRoot;
		for (String path : paths) {
			folder = new File(folder, path);
		}
		return folder;
	}

}
