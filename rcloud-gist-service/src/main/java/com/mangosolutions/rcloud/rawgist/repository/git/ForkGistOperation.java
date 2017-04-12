package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.IOException;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.OpenOp;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;

import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistErrorCode;
import com.mangosolutions.rcloud.rawgist.repository.GistRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryException;

public class ForkGistOperation extends ReadGistOperation {

	private static final Logger logger = LoggerFactory.getLogger(ForkGistOperation.class);

	private GistRepository originalRepository;

	private GistRepository newRepository;

	public ForkGistOperation(RepositoryLayout layout, GistRepository originalRepository, GistRepository newRepository, UserDetails user) {
		super(layout, newRepository.getId(), user);
		this.originalRepository = originalRepository;
		this.newRepository = newRepository;
	}
	
	public ForkGistOperation(File repositoryFolder, GistRepository originalRepository, GistRepository newRepository, UserDetails user) {
		this(new RepositoryLayout(repositoryFolder), originalRepository, newRepository, user);
	}
	
	@Override
	public GistResponse call() {
		OpenOp openOp = new OpenOp();
		openOp.setDir(this.getLayout().getBareFolder());
		try (Grgit git = openOp.call()) {
			this.forkGist();
			return this.readGist(git);
		}
	}

	private void forkGist() {
		RepositoryLayout layout = this.getLayout();
		File originalFolder = originalRepository.getGistRepositoryFolder(this.getUser());
		try {
			FileUtils.copyDirectory(originalFolder, layout.getRootFolder());
			this.updateMetadata();
			originalRepository.registerFork(newRepository);
		} catch (IOException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_FORK_FAILURE,
					"Could not fork gist {} to a new gist with id {}", originalRepository.getId(), this.getGistId());
			logger.error(error.getFormattedMessage() + " with folder path {}", layout.getBareFolder());
			throw new GistRepositoryException(error, e);
		}
	}

	private void updateMetadata() {
		GistMetadata metadata = getMetadata();
		metadata.setId(this.getGistId());
		metadata.setOwner(this.getUser().getUsername());
		metadata.setCreatedAt(new DateTime());
		metadata.setUpdatedAt(new DateTime());
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
