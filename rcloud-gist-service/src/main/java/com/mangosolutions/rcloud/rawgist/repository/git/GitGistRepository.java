/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.Person;
import org.ajoberstar.grgit.Repository;
import org.ajoberstar.grgit.Status;
import org.ajoberstar.grgit.Status.Changes;
import org.ajoberstar.grgit.operation.AddOp;
import org.ajoberstar.grgit.operation.CheckoutOp;
import org.ajoberstar.grgit.operation.CleanOp;
import org.ajoberstar.grgit.operation.CommitOp;
import org.ajoberstar.grgit.operation.OpenOp;
import org.ajoberstar.grgit.operation.ResetOp;
import org.ajoberstar.grgit.operation.ResetOp.Mode;
import org.ajoberstar.grgit.operation.RmOp;
import org.ajoberstar.grgit.operation.StatusOp;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.mangosolutions.rcloud.rawgist.model.FileContent;
import com.mangosolutions.rcloud.rawgist.model.FileDefinition;
import com.mangosolutions.rcloud.rawgist.model.Fork;
import com.mangosolutions.rcloud.rawgist.model.GistHistory;
import com.mangosolutions.rcloud.rawgist.model.GistIdentity;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistCommentRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistErrorCode;
import com.mangosolutions.rcloud.rawgist.repository.GistRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryError;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryException;

public class GitGistRepository implements GistRepository, Serializable {

	private static final long serialVersionUID = -8235501365399798269L;

	private static final Logger logger = LoggerFactory.getLogger(GitGistRepository.class);

	private static final String B64_BINARY_EXTENSION = "b64";

	private RepositoryLayout layout;
	
	private MetadataStore metadataStore;
	
	private CommentStore commentStore;

	private HistoryStore historyStore;

	public GitGistRepository(File repositoryFolder, MetadataStore metadataStore, CommentStore commentStore, HistoryStore historyStore) {
		this.metadataStore = metadataStore;
		this.commentStore = commentStore;
		this.historyStore = historyStore;
		InitRepositoryLayoutOperation op = new InitRepositoryLayoutOperation(repositoryFolder);
		this.layout = op.call();
	}
	
	
	@Override
	public String getId() {
		return this.getMetadata().getId();
	}

	@Override
	public File getGistRepositoryFolder(UserDetails owner) {
		return layout.getRootFolder();
	}

	@Override
	public GistResponse getGist(UserDetails userDetails) {
		GistResponse response = null;
		// create git repository
		OpenOp openOp = new OpenOp();
		openOp.setDir(layout.getGistFolder());
		Grgit git = openOp.call();
		response = buildResponse(git, userDetails);
		return response;
	}

	@Override
	public GistResponse getGist(String commitId, UserDetails userDetails) {
		GistResponse response = null;
		// create git repository
		OpenOp openOp = new OpenOp();
		openOp.setDir(layout.getGistFolder());
		Grgit git = openOp.call();
		response = buildResponse(git, commitId, userDetails);
		return response;
	}

	@Override
	public GistResponse createGist(GistRequest request, String gistId, UserDetails userDetails) {
		this.updateMetadata(userDetails, gistId);
		OpenOp openOp = new OpenOp();
		openOp.setDir(layout.getGistFolder());
		Grgit git = openOp.call();
		saveContent(git, request, userDetails);
		return buildResponse(git, userDetails);
	}
	
	@Override
	public GistResponse fork(GistRepository originalRepository, String gistId, UserDetails userDetails) {
		File originalFolder = originalRepository.getGistRepositoryFolder(userDetails);
		try {
			FileUtils.copyDirectory(originalFolder, layout.getRootFolder());
			OpenOp openOp = new OpenOp();
			openOp.setDir(layout.getGistFolder());
			Grgit git = openOp.call();
			this.updateMetadata(userDetails, gistId);
			originalRepository.registerFork(this);
			return this.buildResponse(git, userDetails);
		} catch (IOException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_FORK_FAILURE, "Could not fork gist {} to a new gist with id {}", originalRepository.getId(), this.getId());
			logger.error(error.getFormattedMessage() + " with folder path {}", layout.getGistFolder());
			throw new GistRepositoryException(error, e);
		}
		
		
	}

	@Override
	public GistResponse editGist(GistRequest request, UserDetails userDetails) {
		OpenOp openOp = new OpenOp();
		openOp.setDir(layout.getGistFolder());
		Grgit git = openOp.call();
		saveContent(git, request, userDetails);
		return buildResponse(git, userDetails);
	}

	private void saveContent(Grgit git, GistRequest request, UserDetails userDetails) {
		Map<String, FileDefinition> files = request.getFiles();
		try {
			for (Map.Entry<String, FileDefinition> file : files.entrySet()) {
				String filename = file.getKey();
				FileDefinition definition = file.getValue();
				if (isDelete(definition)) {
					try {
						FileUtils.forceDelete(new File(layout.getGistFolder(), filename));
					} catch (IOException e) {
						GistError error = new GistError(GistErrorCode.ERR_GIST_UPDATE_FAILURE, "Could not remove {} from gist {}", filename, this.getId());
						logger.error(error.getFormattedMessage() + " with folder path {}", layout.getGistFolder());
						throw new GistRepositoryException(error, e);
					}
				}
				if (isUpdate(definition)) {
					try {
						FileUtils.write(new File(layout.getGistFolder(), filename), definition.getContent(), CharEncoding.UTF_8);
					} catch (IOException e) {
						GistError error = new GistError(GistErrorCode.ERR_GIST_UPDATE_FAILURE, "Could not update {} for gist {}", filename, this.getId());
						logger.error(error.getFormattedMessage() + " with folder path {}", layout.getGistFolder());
						throw new GistRepositoryException(error, e);
					}
				}

				if (isMove(definition)) {
					File oldFile = new File(layout.getGistFolder(), filename);
					File newFile = new File(layout.getGistFolder(), definition.getFilename());
					if(!oldFile.equals(newFile)) {
						try {
							FileUtils.moveFile(oldFile, newFile);
						} catch (IOException e) {
							GistError error = new GistError(GistErrorCode.ERR_GIST_UPDATE_FAILURE, "Could not move {} to {} for gist {}", filename, definition.getFilename(), this.getId());
							logger.error(error.getFormattedMessage() + " with folder path {}", layout.getGistFolder());
							throw new GistRepositoryException(error, e);
						}
					}
				}
			}
			StatusOp statusOp = new StatusOp(git.getRepository());
			Status status = statusOp.call();
			if (!status.isClean()) {
				stageAllChanges(status, git.getRepository());
				CommitOp commitOp = new CommitOp(git.getRepository());
				Person person = new Person(userDetails.getUsername(), "");
				commitOp.setCommitter(person);
				commitOp.setAuthor(person);
				commitOp.setMessage("");
				commitOp.call();
			}
			this.updateMetadata(request);
		} finally {
			StatusOp statusOp = new StatusOp(git.getRepository());
			Status status = statusOp.call();
			if (!status.isClean()) {
				// clean and then reset
				CleanOp cleanOp = new CleanOp(git.getRepository());
				cleanOp.call();
				ResetOp resetOp = new ResetOp(git.getRepository());
				resetOp.setMode(Mode.HARD);
				resetOp.call();
			}

		}
	}

	private void updateMetadata(UserDetails owner, String gistId) {
		GistMetadata metadata = getMetadata();
		metadata.setId(gistId);
		if (owner != null && StringUtils.isEmpty(metadata.getOwner())) {
			metadata.setOwner(owner.getUsername());
		}
		this.saveMetadata(metadata);
	}

	private void saveMetadata(GistMetadata metadata) {
		metadataStore.save(this.layout.getMetadataFile(), metadata);
	}

	private void updateMetadata(GistRequest request) {
		GistMetadata metadata = getMetadata();
		if (request != null) {
			String description = request.getDescription();

			if (description != null) {
				metadata.setDescription(description);
			}

			if (metadata.getCreatedAt() == null) {
				metadata.setCreatedAt(new DateTime());
			}
			
			if(request.getPublic() != null) {
				metadata.setPublic(request.getPublic());
			}

			metadata.setUpdatedAt(new DateTime());
		}
		this.saveMetadata(metadata);
	}

	public GistMetadata getMetadata() {
		return metadataStore.load(layout.getMetadataFile());
	}

	private void stageAllChanges(Status status, Repository repository) {
		Changes changes = status.getUnstaged();
		Set<String> added = changes.getAdded();
		Set<String> modified = changes.getModified();
		Set<String> removed = changes.getRemoved();
		if (added != null && !added.isEmpty()) {
			AddOp addOp = new AddOp(repository);
			addOp.setPatterns(added);
			addOp.call();
		}
		if (modified != null && !modified.isEmpty()) {
			AddOp addOp = new AddOp(repository);
			addOp.setPatterns(modified);
			addOp.call();
		}
		if (removed != null && !removed.isEmpty()) {
			RmOp rm = new RmOp(repository);
			rm.setPatterns(removed);
			rm.call();
		}
	}

	private boolean isMove(FileDefinition definition) {
		return definition != null && !StringUtils.isEmpty(definition.getFilename());
	}

	private boolean isUpdate(FileDefinition definition) {
		return definition != null && definition.getContent() != null;
	}

	private boolean isDelete(FileDefinition definition) {
		return definition == null;
	}

	private GistResponse buildResponse(Grgit git, String commitId, UserDetails activeUser) {
		// switch the working copy to that commit
		GistResponse response = null;
		try {
			CheckoutOp checkoutOp = new CheckoutOp(git.getRepository());
			checkoutOp.setBranch(commitId);
			checkoutOp.call();
			response = buildResponse(git, activeUser);
		} finally {
			CheckoutOp checkoutOp = new CheckoutOp(git.getRepository());
			checkoutOp.setBranch("master");
			checkoutOp.call();
		}
		return response;
	}

	private GistResponse buildResponse(Grgit git, UserDetails activeUser) {
		GistResponse response = new GistResponse();

		Map<String, FileContent> files = new LinkedHashMap<String, FileContent>();
		Collection<File> fileList = FileUtils.listFiles(layout.getGistFolder(), FileFileFilter.FILE, FileFilterUtils
				.and(TrueFileFilter.INSTANCE, FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(".git"))));
		for (File file : fileList) {
			FileContent content = new FileContent();
			try {
				content.setFilename(file.getName());
				content.setContent(FileUtils.readFileToString(file, CharEncoding.UTF_8));
				content.setSize(file.length());
				content.setTruncated(false);
				// TODO the language
				String language = FilenameUtils.getExtension(file.getName());
				if(!B64_BINARY_EXTENSION.equals(language) && !StringUtils.isEmpty(language) ) {
					content.setLanguage(language);
				}
				// TODO mimetype
				content.setType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file));
				files.put(file.getName(), content);
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_READABLE, "Could not read content of {} for gist {}", file.getName(), this.getId());
				logger.error(error.getFormattedMessage() + " with path {}", file);
				throw new GistRepositoryError(error, e);
			}
		}
		response.setFiles(files);
		response.setComments(this.getCommentRepository().getComments(activeUser).size());
		List<GistHistory> history = getHistory(git);
		response.setHistory(history);
		applyMetadata(response);
		return response;
	}

	private List<GistHistory> getHistory(Grgit git) {
		String gistId = this.getId();
		List<GistHistory> history = historyStore.load(gistId);
		GitHistoryOperation historyOperation = new GitHistoryOperation();
		historyOperation.setRepository(git.getRepository());
		historyOperation.setknownHistory(history);
		history = historyOperation.call();
		historyStore.save(gistId, history);
		return history;
	}

	private void applyMetadata(GistResponse response) {
		
		GistMetadata metadata = this.getMetadata();
		response.setId(metadata.getId());
		response.setDescription(metadata.getDescription());
		response.setDescription(metadata.getDescription());
		response.setCreatedAt(metadata.getCreatedAt());
		response.setUpdatedAt(metadata.getUpdatedAt());
		response.setPublic(metadata.isPublic());
		if (!StringUtils.isEmpty(metadata.getOwner())) {
			GistIdentity owner = new GistIdentity();
			owner.setLogin(metadata.getOwner());
			response.setOwner(owner);
			response.setUser(owner);
		}
		response.addAdditionalProperties(metadata.getAdditionalProperties());
	}

	@Override
	public void registerFork(GistRepository forkedRepository) {
		this.updateForkInformation(forkedRepository);
	}

	private void updateForkInformation(GistRepository forkedRepository) {
		GistMetadata metadata = this.getMetadata();
		GistMetadata forksMetadata = forkedRepository.getMetadata();
		Fork fork = new Fork();
		fork.setCreatedAt(forksMetadata.getCreatedAt());
		fork.setUpdatedAt(forksMetadata.getUpdatedAt());
		fork.setId(forksMetadata.getId());
		String forkOwner = forksMetadata.getOwner(); 
		GistIdentity forkOwnerIdentity = new GistIdentity();
		forkOwnerIdentity.setLogin(forkOwner);
		fork.setUser(forkOwnerIdentity);
		metadata.addFork(fork);
		this.saveMetadata(metadata);
	}



	@Override
	public GistCommentRepository getCommentRepository() {
		return new GitGistCommentRepository(this.layout.getCommentsFile(), this.commentStore);
	}



}
