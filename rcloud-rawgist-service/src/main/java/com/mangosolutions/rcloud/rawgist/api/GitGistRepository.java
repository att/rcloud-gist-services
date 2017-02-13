package com.mangosolutions.rcloud.rawgist.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.Repository;
import org.ajoberstar.grgit.Status;
import org.ajoberstar.grgit.Status.Changes;
import org.ajoberstar.grgit.operation.AddOp;
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
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GitGistRepository implements GistRepository {

	public static final String GIST_META_JSON_FILE = "gist.json";

	private static final String GIT_REPO_FOLDER_NAME = "repo";

	private File repositoryFolder;
	private File gitFolder;
	private String gistId;

	private ObjectMapper objectMapper;

	public GitGistRepository(File repositoryFolder, String id, ObjectMapper objectMapper) {
		this.repositoryFolder = repositoryFolder;
		this.gitFolder = new File(repositoryFolder, GIT_REPO_FOLDER_NAME);
		this.gistId = id;
		this.initializeRepository();
		this.objectMapper = objectMapper;
	}
	
	public GitGistRepository(File repositoryFolder, ObjectMapper objectMapper) {
		this.repositoryFolder = repositoryFolder;
		this.gitFolder = new File(repositoryFolder, GIT_REPO_FOLDER_NAME);
		this.objectMapper = objectMapper;
		this.gistId = this.getMetaData().getId();
	}

	private void initializeRepository() {
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
	}

	@Override
	public File getGistRepositoryFolder() {
		return repositoryFolder;
	}

	@Override
	public GistResponse getGist() {
		GistResponse response = null;
		// create git repository
		OpenOp openOp = new OpenOp();
		openOp.setDir(gitFolder);
		Grgit git = openOp.call();
		response = buildResponse(git);
		return response;
	}

	@Override
	public GistResponse createGist(GistRequest request) {
		OpenOp openOp = new OpenOp();
		openOp.setDir(gitFolder);
		Grgit git = openOp.call();
		saveContent(git, request);
		return buildResponse(git);
	}

	@Override
	public GistResponse editGist(GistRequest request) {
		OpenOp openOp = new OpenOp();
		openOp.setDir(gitFolder);
		Grgit git = openOp.call();
		saveContent(git, request);
		return buildResponse(git);
	}
	
	private void saveContent(Grgit git, GistRequest request) {
		Map<String, FileDefinition> files = request.getFiles();
		try {
			for(Map.Entry<String, FileDefinition> file: files.entrySet()) {
				String fileName = file.getKey();
				FileDefinition definition = file.getValue();
				if(isDelete(definition)) {
					FileUtils.forceDelete(new File(gitFolder, fileName));
				} 
				if(isUpdate(definition)) {
					FileUtils.write(new File(gitFolder, fileName), definition.getContent(), CharEncoding.UTF_8);
				} 
				
				if(isMove(definition)) {
					FileUtils.moveFile(new File(gitFolder, fileName), new File(gitFolder, definition.getFilename()));
				}
			}
			StatusOp statusOp = new StatusOp(git.getRepository());
			Status status = statusOp.call();
			if(!status.isClean()) {
				stageAllChanges(status, git.getRepository());
				CommitOp commitOp = new CommitOp(git.getRepository());
				commitOp.setMessage("");
				commitOp.call();
			}
			this.saveMetaData(request);
		} catch (IOException e) {
			//TODO throw new exception
			throw new RuntimeException("Could not change repository.", e);
		} finally {
			StatusOp statusOp = new StatusOp(git.getRepository());
			Status status = statusOp.call();
			if(!status.isClean()) {
				//clean and then reset
				CleanOp cleanOp = new CleanOp(git.getRepository());
				cleanOp.call();
				ResetOp resetOp = new ResetOp(git.getRepository());
				resetOp.setMode(Mode.HARD);
				resetOp.call();
			}
			
		}
	}
	
	private void saveMetaData(GistRequest request) {
		GistMetadata metaData = getMetaData();
		metaData.setId(this.gistId);
		String description = request.getDescription();
		if(description != null) {
			metaData.setDescription(description);
		}
		if(metaData.getCreatedAt() == null) {
			metaData.setCreatedAt(new DateTime());
		}
		metaData.setUpdatedAt(new DateTime());
		
		File metaDataFile = new File(this.repositoryFolder, GIST_META_JSON_FILE);
	    try {
			objectMapper.writeValue(metaDataFile, metaData);
		} catch (IOException e) {
			throw new RuntimeException("Could not read metadata file");
		}
	}
	
	private GistMetadata getMetaData() {
		File metaDataFile = new File(this.repositoryFolder, GIST_META_JSON_FILE);
		GistMetadata metaData = new GistMetadata();
		if(metaDataFile.exists()) {
		    try {
				metaData = objectMapper.readValue(metaDataFile, GistMetadata.class);
			} catch (IOException e) {
				throw new RuntimeException("Could not read metadata file");
			}
		}
		return metaData;
	}
	

	private void stageAllChanges(Status status, Repository repository) {
		Changes changes = status.getUnstaged();
		Set<String> added = changes.getAdded();
		Set<String> modified = changes.getModified();
		Set<String> removed = changes.getRemoved();
		if(added != null && !added.isEmpty()) {
			AddOp addOp = new AddOp(repository);
			addOp.setPatterns(added);
			addOp.call();
		}
		if(modified != null && !modified.isEmpty()) {
			AddOp addOp = new AddOp(repository);
			addOp.setPatterns(modified);
			addOp.call();
		}
		if(removed != null && !removed.isEmpty()) {
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
	
	private GistResponse buildResponse(Grgit git) {
		GistResponse response = new GistResponse();
		
		response.setId(gistId);
		Map<String, FileContent> files = new LinkedHashMap<String, FileContent>();
		Collection<File> fileList = FileUtils.listFiles(gitFolder, FileFileFilter.FILE, FileFilterUtils.and(TrueFileFilter.INSTANCE, FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(".git"))));
		for(File file: fileList) {
			FileContent content = new FileContent();
			try {
				content.setContent(FileUtils.readFileToString(file, CharEncoding.UTF_8));
				content.setSize(file.length());
				content.setTruncated(false);
				//TODO mimetype
				content.setType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file));
				files.put(file.getName(), content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException("Could not build response", e);
			}
		}
		response.setFiles(files);
		applyMetadata(response);
		return response;
	}

	private void applyMetadata(GistResponse response) {
		GistMetadata metaData = this.getMetaData();
		response.setDescription(metaData.getDescription());
		response.setDescription(metaData.getDescription());
		response.setCreatedAt(metaData.getCreatedAt());
		response.setUpdatedAt(metaData.getUpdatedAt());
		response.addAdditionalProperties(metaData.getAdditionalProperties());
	}

}
