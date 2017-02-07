package com.mangosolutions.rcloud.rawgist.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
import org.ajoberstar.grgit.operation.RmOp;
import org.ajoberstar.grgit.operation.StatusOp;
import org.ajoberstar.grgit.operation.ResetOp.Mode;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.util.StringUtils;

public class GitGistRepository implements GistRepository {

	private static final String RECYCLE_FOLDER_NAME = ".recycle";
	
	private File repositoryRoot;
	private File recycleRoot;
	private GistIdGenerator idGenerator;

	public GitGistRepository(String repositoryRoot, GistIdGenerator idGenerator) throws IOException {
		this.repositoryRoot = new File(repositoryRoot);
		if(!this.repositoryRoot.exists()) {
			FileUtils.forceMkdir(this.repositoryRoot);
		}
		recycleRoot = new File(repositoryRoot, RECYCLE_FOLDER_NAME);
		if(!this.recycleRoot.exists()) {
			FileUtils.forceMkdir(this.recycleRoot);
		}
		this.idGenerator = idGenerator;
	}
	
	@Override
	public void listGists() {
		
	}

	@Override
	public GistResponse getGist(String gistId) {
		File repositoryFolder = getRepositoryFolder(gistId);
		GistResponse response = null;
		if(repositoryFolder.exists()) {
			// create git repository
			OpenOp openOp = new OpenOp();
			openOp.setDir(repositoryFolder);
			Grgit git = openOp.call();
			response = buildResponse(gistId, repositoryFolder, git);
		}
		return response;
	}

	@Override
	public GistResponse createGist(GistRequest request) {
		String id = idGenerator.generateId();
		//create folder
		File repositoryFolder = getRepositoryFolder(id);
		if(!repositoryFolder.exists()) {
			try {
				FileUtils.forceMkdir(repositoryFolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// create git repository
		InitOp initOp = new InitOp();
		initOp.setDir(repositoryFolder);
		Grgit git = initOp.call();
		saveContent(repositoryFolder, git, request);
		return buildResponse(id, repositoryFolder, git);
	}
	
	@Override
	public GistResponse editGist(String gistId, GistRequest request) {
		File repositoryFolder = getRepositoryFolder(gistId);
		GistResponse response = null;
		if(repositoryFolder.exists()) {
			// create git repository
			OpenOp openOp = new OpenOp();
			openOp.setDir(repositoryFolder);
			Grgit git = openOp.call();
			saveContent(repositoryFolder, git, request);
			response = buildResponse(gistId, repositoryFolder, git);
		}
		return response;
	}

	@Override
	public void deleteGist(String gistId) {
		File repositoryFolder = getRepositoryFolder(gistId);
		try {
			FileUtils.moveDirectoryToDirectory(repositoryFolder, new File(recycleRoot, gistId), true);
			FileUtils.forceDelete(repositoryFolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private GistResponse buildResponse(String gistId, File repositoryFolder, Grgit git) {
		GistResponse response = new GistResponse();
		response.setId(gistId);
		Map<String, FileContent> files = new LinkedHashMap<String, FileContent>();
		Collection<File> fileList = FileUtils.listFiles(repositoryFolder, FileFileFilter.FILE, FileFilterUtils.and(TrueFileFilter.INSTANCE, FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(".git"))));
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
				e.printStackTrace();
			}
		}
		response.setFiles(files);
		return response;
	}

	private void saveContent(File repositoryFolder, Grgit git, GistRequest request) {
		Map<String, FileDefinition> files = request.getFiles();
		try {
			for(Map.Entry<String, FileDefinition> file: files.entrySet()) {
				String fileName = file.getKey();
				FileDefinition definition = file.getValue();
				if(isDelete(definition)) {
					FileUtils.forceDelete(new File(repositoryFolder, fileName));
				} 
				if(isUpdate(definition)) {
					FileUtils.write(new File(repositoryFolder, fileName), definition.getContent(), CharEncoding.UTF_8);
				} 
				
				if(isMove(definition)) {
					FileUtils.moveFile(new File(repositoryFolder, fileName), new File(repositoryFolder, definition.getFilename()));
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

	private void deleteFile(Grgit git, File file) {
		RmOp rm = new RmOp(git.getRepository());
		rm.setPatterns(new HashSet<String>(Arrays.asList(file.getName())));
		rm.call();
	}

	private boolean isDelete(FileDefinition definition) {
		return definition == null;
	}

	@SuppressWarnings("unchecked")
	private String getFileContent(Object value) {
		String content = null;
		if(value != null && value instanceof Map && ((Map<String, String>)value).containsKey("content")) {
			content = ((Map<String, String>)value).get("content");
		}
		return content;
	}

	private File getRepositoryFolder(String id) {
		String[] paths = id.split("-");
		File folder = repositoryRoot;
		for(String path: paths) {
			folder = new File(folder, path);
		}
		return folder;
	}



}
