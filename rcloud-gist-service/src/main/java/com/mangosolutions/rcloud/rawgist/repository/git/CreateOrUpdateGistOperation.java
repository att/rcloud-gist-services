package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.OpenOp;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator.DefaultFileModeStrategy;
import org.eclipse.jgit.treewalk.FileTreeIterator.FileModeStrategy;
import org.eclipse.jgit.treewalk.FileTreeIterator.NoGitlinksStrategy;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.eclipse.jgit.util.FS;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.mangosolutions.rcloud.rawgist.model.FileContent;
import com.mangosolutions.rcloud.rawgist.model.FileDefinition;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistErrorCode;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryException;

public class CreateOrUpdateGistOperation extends ReadGistOperation {

	private static final Logger logger = LoggerFactory.getLogger(CreateOrUpdateGistOperation.class);

	private GistRequest gistRequest;
	
	private GistResponse preChangeResponse;

	public CreateOrUpdateGistOperation(RepositoryLayout layout, String gistId, GistRequest gistRequest, UserDetails user) {
		super(layout, gistId, user);
		this.gistRequest = gistRequest;
	}
	
	public CreateOrUpdateGistOperation(File repositoryFolder, String gistId, GistRequest gistRequest, UserDetails user) {
		this(new RepositoryLayout(repositoryFolder), gistId, gistRequest, user);
	}
	
	@Override
	public GistResponse call() {
		
		try (Grgit git = openRepository()) {
			preChangeResponse = this.readGist(git);
		}
		
		try (Grgit git = createOrOpenWorkingCopy()) {
			createMetadata();
			saveContent(git);
		}
		
		try (Grgit git = openRepository()) {
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
			applyFileChanges(git, layout, files, gistId, userDetails);
			this.updateMetadata(this.gistRequest);
		} finally {
			cleanRepository(git);
		}

	}

	private void cleanRepository(Grgit git) {
		try {
			FileUtils.cleanDirectory(this.getLayout().getWorkingFolder());
		} catch (IOException e) {
			logger.warn("Couldn't clean working directory for gist {}", this.getGistId(), e);
		}
	}
	
	private DirCache getIndex(Grgit git) {
		File indexFile = new File(this.getLayout().getWorkingFolder(), ".index");
		DirCache index = new DirCache(indexFile, FS.detect());
		return index;
	}

	private void applyFileChanges(Grgit git, RepositoryLayout layout, Map<String, FileDefinition> files, String gistId, UserDetails userDetails) {
		DirCache index = getIndex(git);
		BareAddCommand addCommand = null;
		BareRmCommand rmCommand = null;
		BareCommitCommand commitCommand = new BareCommitCommand(git.getRepository().getJgit().getRepository(), index);
		for (Map.Entry<String, FileDefinition> file : files.entrySet()) {
			String filename = file.getKey();
			File workingFolder = getWorkTree(git);
			FileDefinition definition = file.getValue();
			if (isDelete(definition)) {
				commitCommand.setOnly(filename);
				rmCommand = applyRmPath(rmCommand, filename, git, index);
			}
			if (isUpdate(definition)) {
				updateFile(workingFolder, gistId, filename, definition);
				commitCommand.setOnly(filename);
				addCommand = applyAddPath(addCommand, filename, workingFolder, git, index);
			}

			if (isMove(filename, definition)) {
				moveFile(workingFolder, gistId, filename, definition);
				rmCommand = applyRmPath(rmCommand, filename, git, index);
				commitCommand.setOnly(filename);
				addCommand = applyAddPath(addCommand, definition.getFilename(), workingFolder, git, index);
				commitCommand.setOnly(definition.getFilename());
			}
		}
		try {
			if(addCommand != null) {
				FileTreeIterator it = this.getFileTreeIterator(git.getRepository().getJgit(), this.getWorkTree(git));
				addCommand.setWorkingTreeIterator(it);
				addCommand.call();
			}
			if(rmCommand != null) {
				rmCommand.call();
			}
			try {
				if(addCommand != null || rmCommand != null) {
					commitCommand.workingFolder = this.getWorkTree(git);
					commitCommand.setAuthor(userDetails.getUsername(), "");
					commitCommand.setCommitter(userDetails.getUsername(), "");
					commitCommand.setMessage("");
					commitCommand.setNoVerify(true);
					commitCommand.call();
				}
			} catch (GitAPIException e) {
				throw new RuntimeException(e);
			}
		} catch (GitAPIException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_UPDATE_FAILURE,
					"Could not update gist with id {}", this.getGistId());
			logger.error(error.getFormattedMessage() + " with folder path {}", this.getWorkTree(git), e);
			throw new GistRepositoryException(error, e);
		}
	}
	
	private BareRmCommand applyRmPath(BareRmCommand rmCommand, String filename, Grgit git, DirCache index) {
		if(rmCommand == null) {
			Git jgit = git.getRepository().getJgit();
			rmCommand = new BareRmCommand(jgit.getRepository(), index);
		}
		return rmCommand.addFilepattern(filename);
	}

	

	private BareAddCommand applyAddPath(BareAddCommand addCommand, String filename, File workingFolder, Grgit git, DirCache index) {
		if(addCommand == null) {
			Git jgit = git.getRepository().getJgit();
			
			FileTreeIterator iterator = getFileTreeIterator(jgit, workingFolder);
			addCommand = new BareAddCommand(jgit.getRepository());
			addCommand.setWorkingTreeIterator(iterator);
			addCommand.addDirCache(index);
		}
		return addCommand.addFilepattern(filename);
	}
	
	private FileTreeIterator getFileTreeIterator(Git jgit, File workingFolder) {
		
		FileModeStrategy fileModeStrategy = jgit.getRepository().getConfig().get(WorkingTreeOptions.KEY).isDirNoGitLinks() ?
				NoGitlinksStrategy.INSTANCE :
				DefaultFileModeStrategy.INSTANCE;
		
		
		FileTreeIterator iterator = new FileTreeIterator(
				workingFolder, jgit.getRepository().getFS(),
				jgit.getRepository().getConfig().get(WorkingTreeOptions.KEY), fileModeStrategy);
		
		return iterator;
	}

	private void moveFile(File workingFolder, String gistId, String filename, FileDefinition definition) {
		File oldFile = new File(workingFolder, filename);
		File newFile = new File(workingFolder, definition.getFilename());
		if (!oldFile.equals(newFile)) {
			try {
				FileContent oldFileContent = this.preChangeResponse.getFiles().get(filename);
				if(oldFileContent != null) {
					String content = definition.getContent();
					if(StringUtils.isEmpty(content)) {
						content = oldFileContent.getContent();
					}
					FileUtils.write(newFile, content,
							CharEncoding.UTF_8);
				}
//				FileUtils.moveFile(oldFile, newFile);
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

	private boolean isMove(String filename, FileDefinition definition) {
		return definition != null && !StringUtils.isEmpty(definition.getFilename()) && !filename.equals(definition.getFilename());
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
