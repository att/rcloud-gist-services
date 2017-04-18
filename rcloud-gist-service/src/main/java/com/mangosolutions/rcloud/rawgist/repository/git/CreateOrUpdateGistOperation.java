package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.Person;
import org.ajoberstar.grgit.Repository;
import org.ajoberstar.grgit.Status;
import org.ajoberstar.grgit.Status.Changes;
import org.ajoberstar.grgit.operation.AddOp;
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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.mangosolutions.rcloud.rawgist.model.FileDefinition;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistErrorCode;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryException;

public class CreateOrUpdateGistOperation extends ReadGistOperation {

	private static final Logger logger = LoggerFactory.getLogger(CreateOrUpdateGistOperation.class);

	private GistRequest gistRequest;

	public CreateOrUpdateGistOperation(RepositoryLayout layout, String gistId, GistRequest gistRequest, UserDetails user) {
		super(layout, gistId, user);
		this.gistRequest = gistRequest;
	}
	
	public CreateOrUpdateGistOperation(File repositoryFolder, String gistId, GistRequest gistRequest, UserDetails user) {
		this(new RepositoryLayout(repositoryFolder), gistId, gistRequest, user);
	}
	
	@Override
	public GistResponse call() {
		
		try (Grgit git = createOrOpenWorkingCopy()) {
			createMetadata();
			saveContent(git);
			return this.readGist(git);
		}
	}

	private Grgit createOrOpenWorkingCopy() {
		Grgit git = openRepository();
		git = initialiseRepo(git);
		cleanRepository(git);
		return git;
		
	}

	private Grgit initialiseRepo(Grgit git) {
		Git jgit = git.getRepository().getJgit();
		if(jgit.getRepository().isBare()) {
			setupWorkingConfig(jgit);
			OpenOp openOp = new OpenOp();
			openOp.setDir(this.getLayout().getBareFolder());
			git = openOp.call();
		}
		File workingFolder = git.getRepository().getJgit().getRepository().getWorkTree();
		if(!workingFolder.exists()) {
			try {
				FileUtils.forceMkdir(workingFolder);
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.FATAL_GIST_INITIALISATION,
						"Could not initialise gist repository with id {}", this.getGistId());
				logger.error(error.getFormattedMessage() + " with folder path {}", workingFolder, e);
				throw new GistRepositoryException(error, e);
			}
		}
		return git;
	}

	private void setupWorkingConfig(Git jgit) {
		StoredConfig config = jgit.getRepository().getConfig();
		String workingDirectory = FilenameUtils.normalize(this.getLayout().getWorkingFolder().getAbsolutePath());
		config.setString("core", null, "worktree", workingDirectory);
		config.setBoolean("core", null, "bare", false);
		try {
			config.save();
		} catch (IOException e) {
			GistError error = new GistError(GistErrorCode.FATAL_GIST_INITIALISATION,
					"Could not initialise gist repository with id {}, from gist {}", this.getGistId());
			logger.error(error.getFormattedMessage() + " with folder path {}", workingDirectory, e);
			throw new GistRepositoryException(error, e);
		}
	}

	private Grgit openRepository() {
		OpenOp openOp = new OpenOp();
		openOp.setDir(this.getLayout().getBareFolder());
		Grgit git = openOp.call();
		return git;
	}

	private void saveContent(Grgit git) {
		RepositoryLayout layout = this.getLayout();
		UserDetails userDetails = this.getUser();
		Map<String, FileDefinition> files = this.gistRequest.getFiles();
		String gistId = this.getGistId();
		try {
			applyFileChanges(git, layout, files, gistId);
			commitChanges(git, userDetails);
			this.updateMetadata(this.gistRequest);
		} finally {
			cleanRepository(git);
		}

	}

	private void cleanRepository(Grgit git) {
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

	private void applyFileChanges(Grgit git, RepositoryLayout layout, Map<String, FileDefinition> files, String gistId) {
		for (Map.Entry<String, FileDefinition> file : files.entrySet()) {
			String filename = file.getKey();
			File workingFolder = git.getRepository().getJgit().getRepository().getWorkTree();
			FileDefinition definition = file.getValue();
			if (isDelete(definition)) {
				deleteFile(workingFolder, gistId, filename);
			}
			if (isUpdate(definition)) {
				updateFile(workingFolder, gistId, filename, definition);
			}

			if (isMove(definition)) {
				moveFile(workingFolder, gistId, filename, definition);
			}
		}
	}

	private void commitChanges(Grgit git, UserDetails userDetails) {
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
	}

	private void moveFile(File workingFolder, String gistId, String filename, FileDefinition definition) {
		File oldFile = new File(workingFolder, filename);
		File newFile = new File(workingFolder, definition.getFilename());
		if (!oldFile.equals(newFile)) {
			try {
				FileUtils.moveFile(oldFile, newFile);
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.ERR_GIST_UPDATE_FAILURE,
						"Could not move {} to {} for gist {}", filename, definition.getFilename(),
						gistId);
				logger.error(error.getFormattedMessage() + " with folder path {}", workingFolder);
				throw new GistRepositoryException(error, e);
			}
		}
	}

	private void updateFile(File workingFolder, String gistId, String filename, FileDefinition definition) {
		try {
			FileUtils.write(new File(workingFolder, filename), definition.getContent(),
					CharEncoding.UTF_8);
		} catch (IOException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_UPDATE_FAILURE,
					"Could not update {} for gist {}", filename, gistId);
			logger.error(error.getFormattedMessage() + " with folder path {}", workingFolder);
			throw new GistRepositoryException(error, e);
		}
	}

	private void deleteFile(File workingFolder, String gistId, String filename) {
		try {
			FileUtils.forceDelete(new File(workingFolder, filename));
		} catch (IOException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_UPDATE_FAILURE,
					"Could not remove {} from gist {}", filename, gistId);
			logger.error(error.getFormattedMessage() + " with folder path {}", workingFolder);
			throw new GistRepositoryException(error, e);
		}
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

	private void createMetadata() {
		GistMetadata metadata = getMetadata();
		metadata.setId(this.getGistId());
		if (this.getUser() != null && StringUtils.isEmpty(metadata.getOwner())) {
			metadata.setOwner(this.getUser().getUsername());
		}
		this.saveMetadata(metadata);
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

			if (request.getPublic() != null) {
				metadata.setPublic(request.getPublic());
			}

			metadata.setUpdatedAt(new DateTime());
		}
		this.saveMetadata(metadata);
	}

	private void saveMetadata(GistMetadata metadata) {
		this.getMetadataStore().save(this.getLayout().getMetadataFile(), metadata);
	}

	public GistRequest getGistRequest() {
		return gistRequest;
	}

	public void setGistRequest(GistRequest gistRequest) {
		this.gistRequest = gistRequest;
	}
}
