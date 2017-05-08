/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.CloneOp;
import org.ajoberstar.grgit.operation.OpenOp;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.model.Fork;
import com.mangosolutions.rcloud.rawgist.model.GistIdentity;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistErrorCode;
import com.mangosolutions.rcloud.rawgist.repository.GistRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryException;

public class ForkGistOperation extends ReadGistOperation {

	private static final Logger logger = LoggerFactory.getLogger(ForkGistOperation.class);

	private GistRepository originalRepository;

	private GistRepository newRepository;

	public ForkGistOperation(RepositoryLayout layout, GistRepository originalRepository, GistRepository newRepository, String gistId, UserDetails user) {
		super(layout, gistId, user);
		this.originalRepository = originalRepository;
		this.newRepository = newRepository;
	}

	public ForkGistOperation(File repositoryFolder, GistRepository originalRepository, GistRepository newRepository, String gistId, UserDetails user) {
		this(new RepositoryLayout(repositoryFolder), originalRepository, newRepository, gistId, user);
	}

	@Override
	public GistResponse call() {
		this.forkGist();
		OpenOp openOp = new OpenOp();
		openOp.setDir(this.getLayout().getBareFolder());
		try (Grgit git = openOp.call()) {
			return this.readGist(git);
		}
	}

	private void forkGist() {
		RepositoryLayout layout = this.getLayout();
		try {

			Grgit git = cloneRepository();
			removeRemotes(git);
			this.updateMetadata();
			originalRepository.registerFork(newRepository);
		} catch (IOException | GitAPIException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_FORK_FAILURE,
					"Could not fork gist {} to a new gist with id {}", originalRepository.getId(), this.getGistId());
			logger.error(error.getFormattedMessage() + " with folder path {}", layout.getBareFolder());
			throw new GistRepositoryException(error, e);
		}
	}

	private void removeRemotes(Grgit git) throws GitAPIException {
		Git jgit = git.getRepository().getJgit();
		Collection<String> remotes = jgit.getRepository().getRemoteNames();
		for(String remote: remotes) {
			RemoteRemoveCommand remoteRemoveCommand = jgit.remoteRemove();
			remoteRemoveCommand.setName(remote);
			remoteRemoveCommand.call();
		}
	}

	private Grgit cloneRepository() throws IOException {
		File bareFolder = this.getLayout().getBareFolder();
		FileUtils.cleanDirectory(bareFolder);
		CloneOp cloneOp = new CloneOp();
		cloneOp.setCheckout(false);
		cloneOp.setBare(true);
		cloneOp.setDir(bareFolder);
		cloneOp.setUri(originalRepository.getGistGitRepositoryFolder(this.getUser()).getAbsolutePath());

		Grgit git = cloneOp.call();
		return git;
	}

	private void updateMetadata() {
		GistMetadata originalMetadata = originalRepository.getMetadata();
		GistMetadata metadata = getMetadata();
		metadata.setId(this.getGistId());
		metadata.setOwner(this.getUser().getUsername());
		metadata.setCreatedAt(new DateTime());
		metadata.setUpdatedAt(new DateTime());
		metadata.setPublic(false);
		metadata.setDescription(originalMetadata.getDescription());
		Fork fork = new Fork();
		fork.setId(originalMetadata.getId());
		metadata.setForkOf(fork);
		this.saveMetadata(metadata);
	}

	private void saveMetadata(GistMetadata metadata) {
		this.getMetadataStore().save(this.getLayout().getMetadataFile(), metadata);
	}

	public GistRepository getOriginalRepository() {
		return originalRepository;
	}

	public void setOriginalRepository(GistRepository originalRepository) {
		this.originalRepository = originalRepository;
	}

	public GistRepository getNewRepository() {
		return newRepository;
	}

	public void setNewRepository(GistRepository newRepository) {
		this.newRepository = newRepository;
	}

}
