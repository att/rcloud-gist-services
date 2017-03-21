package com.mangosolutions.rcloud.rawgist.repository;

import java.io.File;
import java.io.IOException;
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
import org.ajoberstar.grgit.operation.InitOp;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangosolutions.rcloud.rawgist.model.FileContent;
import com.mangosolutions.rcloud.rawgist.model.FileDefinition;
import com.mangosolutions.rcloud.rawgist.model.GistHistory;
import com.mangosolutions.rcloud.rawgist.model.GistIdentity;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;

public class GitGistRepository implements GistRepository {

	private static final String B64_BINARY_EXTENSION = "b64";

	public static final String GIST_META_JSON_FILE = "gist.json";

	private static final String GIT_REPO_FOLDER_NAME = "repo";

	private File repositoryFolder;
	private File gitFolder;
	private String gistId;
	private GistCommentRepository commentRepository;

	private ObjectMapper objectMapper;

	public GitGistRepository(File repositoryFolder, String gistId, ObjectMapper objectMapper, UserDetails owner) {
		this(repositoryFolder, objectMapper);
		this.commentRepository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
		this.gistId = gistId;
		this.initializeRepository(owner);
	}

	public GitGistRepository(File repositoryFolder, ObjectMapper objectMapper) {
		this.repositoryFolder = repositoryFolder;
		this.gitFolder = new File(repositoryFolder, GIT_REPO_FOLDER_NAME);
		this.objectMapper = objectMapper;
		this.gistId = this.getMetadata().getId();
		this.commentRepository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
	}

	private void initializeRepository(UserDetails userDetails) {
		if (!repositoryFolder.exists()) {
			try {
				FileUtils.forceMkdir(repositoryFolder);
			} catch (IOException e) {
				throw new RuntimeException("Could not create gist store.", e);
			}
		}

		if (!gitFolder.exists()) {
			try {
				FileUtils.forceMkdir(repositoryFolder);
				InitOp initOp = new InitOp();
				initOp.setDir(gitFolder);
				initOp.call();
			} catch (IOException e) {
				throw new RuntimeException("Could not create gist store.", e);
			}
		}

		this.updateMetadata(userDetails);
	}

	@Override
	public File getGistRepositoryFolder(UserDetails owner) {
		return repositoryFolder;
	}

	@Override
	public GistResponse getGist(UserDetails userDetails) {
		GistResponse response = null;
		// create git repository
		OpenOp openOp = new OpenOp();
		openOp.setDir(gitFolder);
		Grgit git = openOp.call();
		response = buildResponse(git, userDetails);
		return response;
	}

	@Override
	public GistResponse getGist(String commitId, UserDetails userDetails) {
		GistResponse response = null;
		// create git repository
		OpenOp openOp = new OpenOp();
		openOp.setDir(gitFolder);
		Grgit git = openOp.call();
		response = buildResponse(git, commitId, userDetails);
		return response;
	}

	@Override
	public GistResponse createGist(GistRequest request, UserDetails userDetails) {
		OpenOp openOp = new OpenOp();
		openOp.setDir(gitFolder);
		Grgit git = openOp.call();
		saveContent(git, request, userDetails);
		return buildResponse(git, userDetails);
	}

	@Override
	public GistResponse editGist(GistRequest request, UserDetails userDetails) {
		OpenOp openOp = new OpenOp();
		openOp.setDir(gitFolder);
		Grgit git = openOp.call();
		saveContent(git, request, userDetails);
		return buildResponse(git, userDetails);
	}

	private void saveContent(Grgit git, GistRequest request, UserDetails userDetails) {
		Map<String, FileDefinition> files = request.getFiles();
		try {
			for (Map.Entry<String, FileDefinition> file : files.entrySet()) {
				String fileName = file.getKey();
				FileDefinition definition = file.getValue();
				if (isDelete(definition)) {
					FileUtils.forceDelete(new File(gitFolder, fileName));
				}
				if (isUpdate(definition)) {
					FileUtils.write(new File(gitFolder, fileName), definition.getContent(), CharEncoding.UTF_8);
				}

				if (isMove(definition)) {
					File oldFile = new File(gitFolder, fileName);
					File newFile = new File(gitFolder, definition.getFilename());
					if(!oldFile.equals(newFile)) {
						FileUtils.moveFile(oldFile, newFile);
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
		} catch (IOException e) {
			// TODO throw new exception
			throw new RuntimeException("Could not change repository.", e);
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

	private void updateMetadata(UserDetails owner) {
		GistMetadata metadata = getMetadata();
		metadata.setId(this.gistId);
		if (owner != null && StringUtils.isEmpty(metadata.getOwner())) {
			metadata.setOwner(owner.getUsername());
		}
		this.saveMetadata(metadata);
	}

	private void saveMetadata(GistMetadata metadata) {
		File metadataFile = new File(this.repositoryFolder, GIST_META_JSON_FILE);
		try {
			objectMapper.writeValue(metadataFile, metadata);
		} catch (IOException e) {
			throw new RuntimeException("Could not read metadata file");
		}
	}

	private void updateMetadata(GistRequest request) {
		GistMetadata metadata = getMetadata();
		metadata.setId(this.gistId);
		if (request != null) {
			String description = request.getDescription();

			if (description != null) {
				metadata.setDescription(description);
			}

			if (metadata.getCreatedAt() == null) {
				metadata.setCreatedAt(new DateTime());
			}

			metadata.setUpdatedAt(new DateTime());

		}
		this.saveMetadata(metadata);
	}

	private GistMetadata getMetadata() {
		File metadataFile = new File(this.repositoryFolder, GIST_META_JSON_FILE);
		GistMetadata metadata = new GistMetadata();
		if (metadataFile.exists()) {
			try {
				metadata = objectMapper.readValue(metadataFile, GistMetadata.class);
			} catch (IOException e) {
				throw new RuntimeException("Could not read metadata file", e);
			}
		}
		return metadata;
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

		response.setId(gistId);
		Map<String, FileContent> files = new LinkedHashMap<String, FileContent>();
		Collection<File> fileList = FileUtils.listFiles(gitFolder, FileFileFilter.FILE, FileFilterUtils
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
				// TODO Auto-generated catch block
				throw new RuntimeException("Could not build response", e);
			}
		}
		response.setFiles(files);
		response.setComments(commentRepository.getComments(activeUser).size());
		List<GistHistory> history = getHistory(git);
		response.setHistory(history);
		applyMetadata(response);
		return response;
	}

	private List<GistHistory> getHistory(Grgit git) {
		GitHistoryCreator historyCreator = new GitHistoryCreator();
		return historyCreator.call(git.getRepository());
	}

	private void applyMetadata(GistResponse response) {
		GistMetadata metadata = this.getMetadata();
		response.setDescription(metadata.getDescription());
		response.setDescription(metadata.getDescription());
		response.setCreatedAt(metadata.getCreatedAt());
		response.setUpdatedAt(metadata.getUpdatedAt());
		if (!StringUtils.isEmpty(metadata.getOwner())) {
			GistIdentity owner = new GistIdentity();
			owner.setLogin(metadata.getOwner());
			response.setOwner(owner);
			response.setUser(owner);
		}
		response.addAdditionalProperties(metadata.getAdditionalProperties());
	}

}
