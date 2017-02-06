package com.mangosolutions.rcloud.rawgist.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.AddOp;
import org.ajoberstar.grgit.operation.CommitOp;
import org.ajoberstar.grgit.operation.InitOp;
import org.ajoberstar.grgit.operation.OpenOp;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class GitGistRepository implements GistRepository {

	private File repositoryRoot;
	private GistIdGenerator idGenerator;

	public GitGistRepository(String repositoryRoot, GistIdGenerator idGenerator) throws IOException {
		this.repositoryRoot = new File(repositoryRoot);
		if(!this.repositoryRoot.exists()) {
			FileUtils.forceMkdir(this.repositoryRoot);
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

	private GistResponse buildResponse(String id, File repositoryFolder, Grgit git) {
		GistResponse response = new GistResponse();
		response.setId(id);
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
		Map<String, Object> files = request.getFiles();
		for(Map.Entry<String, Object> file: files.entrySet()) {
			String fileName = file.getKey();
			String content = getFileContent(file.getValue());
			if(content == null) {
				//this should delete the file
			} else {
				try {
					FileUtils.write(new File(repositoryFolder, fileName), content, CharEncoding.UTF_8);
					AddOp add = new AddOp(git.getRepository());
					add.setPatterns(new HashSet<String>(Arrays.asList(fileName)));
					add.call();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		CommitOp commitOp = new CommitOp(git.getRepository());
		commitOp.setMessage("");
		commitOp.call();
	}

	@SuppressWarnings("unchecked")
	private String getFileContent(Object value) {
		String content = null;
		if(value != null && value instanceof Map && ((Map<String, String>)value).containsKey("content")) {
			content = ((Map<String, String>)value).get("content");
		}
		return content.toString();
	}

	private File getRepositoryFolder(String id) {
		String[] paths = id.split("-");
		File folder = repositoryRoot;
		for(String path: paths) {
			folder = new File(folder, path);
		}
		return folder;
	}

	@Override
	public void editGist() {
		
	}

	@Override
	public void deleteGist(String gistId) {
		
	}

}
