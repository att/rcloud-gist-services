package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.Person;
import org.ajoberstar.grgit.Status;
import org.ajoberstar.grgit.operation.CleanOp;
import org.ajoberstar.grgit.operation.CommitOp;
import org.ajoberstar.grgit.operation.OpenOp;
import org.ajoberstar.grgit.operation.ResetOp;
import org.ajoberstar.grgit.operation.ResetOp.Mode;
import org.ajoberstar.grgit.operation.StatusOp;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator.NoGitlinksStrategy;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
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
		} finally {
			resetRepo();
		}
	}

	private void resetRepo() {
		Grgit git = openRepository();
		StoredConfig config = git.getRepository().getJgit().getRepository().getConfig();
		String workingDirectory = FilenameUtils.normalize(this.getLayout().getWorkingFolder().getAbsolutePath());
		config.unset("core", null, "worktree");
		config.setBoolean("core", null, "bare", true);
		try {
			config.save();
		} catch (IOException e) {
			GistError error = new GistError(GistErrorCode.FATAL_GIST_INITIALISATION,
					"Could not initialise gist repository with id {}, from gist {}", this.getGistId());
			logger.error(error.getFormattedMessage() + " with folder path {}", workingDirectory, e);
			throw new GistRepositoryException(error, e);
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
		File workingFolder = getWorkTree(git);
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
	
	private File getWorkTree(Grgit git) {
		if(git.getRepository().getJgit().getRepository().isBare()) {
			return this.getLayout().getWorkingFolder();			
		}
		return git.getRepository().getJgit().getRepository().getWorkTree();
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
		AddCommand addCommand = null;
		RmCommand rmCommand = null;
		for (Map.Entry<String, FileDefinition> file : files.entrySet()) {
			String filename = file.getKey();
			File workingFolder = getWorkTree(git);
			FileDefinition definition = file.getValue();
			if (isDelete(definition)) {
				deleteFile(workingFolder, gistId, filename);
				rmCommand = applyRmPath(rmCommand, filename, git);
			}
			if (isUpdate(definition)) {
				updateFile(workingFolder, gistId, filename, definition);
				addCommand = applyAddPath(addCommand, filename, workingFolder, git);
			}

			if (isMove(definition)) {
				moveFile(workingFolder, gistId, filename, definition);
				rmCommand = applyRmPath(rmCommand, filename, git);
				addCommand = applyAddPath(addCommand, definition.getFilename(), workingFolder, git);
			}
		}
		try {
			if(addCommand != null) {
				addCommand.call();
			}
			if(rmCommand != null) {
				rmCommand.call();
			}
		} catch (GitAPIException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_UPDATE_FAILURE,
					"Could not update gist with id {}", this.getGistId());
			logger.error(error.getFormattedMessage() + " with folder path {}", this.getWorkTree(git), e);
			throw new GistRepositoryException(error, e);
		}
		
	}
	
	private RmCommand applyRmPath(RmCommand rmCommand, String filename, Grgit git) {
		if(rmCommand == null) {
			Git jgit = git.getRepository().getJgit();
			rmCommand = new RmCommand(jgit.getRepository());
		}
		return rmCommand.addFilepattern(filename);
	}

	

	private AddCommand applyAddPath(AddCommand addCommand, String filename, File workingFolder, Grgit git) {
		if(addCommand == null) {
			Git jgit = git.getRepository().getJgit();
			addCommand = new AddCommand(jgit.getRepository());
			FileTreeIterator iterator = new FileTreeIterator(
					workingFolder, jgit.getRepository().getFS(),
					jgit.getRepository().getConfig().get(WorkingTreeOptions.KEY), NoGitlinksStrategy.INSTANCE);
			addCommand.setWorkingTreeIterator(iterator);
		}
		return addCommand.addFilepattern(filename);
	}

	private void commitChanges(Grgit git, UserDetails userDetails) {
		StatusOp statusOp = new StatusOp(git.getRepository());
		Status status = statusOp.call();
		if (!status.isClean()) {
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
