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
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.hazelcast.core.HazelcastInstance;
import com.mangosolutions.rcloud.rawgist.model.GistComment;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;

public class GitGistRepositoryService implements GistRepositoryService {

	private static final Splitter REPOSITORYID_FOLDER_SPLITTER = Splitter.fixedLength(4);

	private static final String RECYCLE_FOLDER_NAME = ".recycle";

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

	@Override
	public List<GistResponse> listGists(UserDetails userDetails) {
		List<GistResponse> gists = new ArrayList<GistResponse>();
		for(File file: FileUtils.listFiles(repositoryRoot, FileFilterUtils.and(FileFileFilter.FILE, new NameFileFilter(GitGistRepository.GIST_META_JSON_FILE)), TrueFileFilter.INSTANCE)) {
			GistRepository repository = new GitGistRepository(file.getParentFile(), objectMapper);
			gists.add(repository.getGist(userDetails));
		}
		return gists;
	}

	@Override
	public GistResponse getGist(String gistId, UserDetails userDetails) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getRepositoryFolder(gistId);
					GistRepository repository = new GitGistRepository(repositoryFolder, objectMapper);
					return repository.getGist(userDetails);
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
	public GistResponse getGist(String gistId, String commitId, UserDetails userDetails) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getRepositoryFolder(gistId);
					GistRepository repository = new GitGistRepository(repositoryFolder, objectMapper);
					return repository.getGist(commitId, userDetails);
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
	public GistResponse createGist(GistRequest request, UserDetails activeUser) {
		String gistId = idGenerator.generateId();
		File repositoryFolder = getRepositoryFolder(gistId);
		GistRepository repository = new GitGistRepository(repositoryFolder, gistId, objectMapper, activeUser);
		return repository.createGist(request, activeUser);
	}

	@Override
	public GistResponse editGist(String gistId, GistRequest request, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getRepositoryFolder(gistId);
					GistRepository repository = new GitGistRepository(repositoryFolder, objectMapper);
					return repository.editGist(request, activeUser);
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
	public void deleteGist(String gistId, UserDetails activeUser) {
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
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getRepositoryFolder(gistId);
					GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
					return repository.getComments(activeUser);
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
	public GistCommentResponse getComment(String gistId, long commentId, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getRepositoryFolder(gistId);
					GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
					return repository.getComment(commentId, activeUser);
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
	public GistCommentResponse createComment(String gistId, GistComment comment, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getRepositoryFolder(gistId);
					GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
					return repository.createComment(comment, activeUser);
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
	public GistCommentResponse editComment(String gistId, long commentId, GistComment comment, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getRepositoryFolder(gistId);
					GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
					return repository.editComment(commentId, comment, activeUser);
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
	public void deleteComment(String gistId, long commentId, UserDetails activeUser) {
		Lock lock = hazelcastInstance.getLock(gistId);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					File repositoryFolder = getRepositoryFolder(gistId);
					GistCommentRepository repository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
					repository.deleteComment(commentId, activeUser);
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


}
