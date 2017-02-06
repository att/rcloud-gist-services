package com.mangosolutions.rcloud.rawgist.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.AddOp;
import org.ajoberstar.grgit.operation.CommitOp;
import org.ajoberstar.grgit.operation.InitOp;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;

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
	public void getGist(String gistId) {
		
	}

	@Override
	public void createGist(GistRequest request) {
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
		
	}

	private void saveContent(File repositoryFolder, Grgit git, GistRequest request) {
		Map<String, Object> files = request.getFiles();
		for(Map.Entry<String, Object> file: files.entrySet()) {
			String fileName = file.getKey();
			if(file.getValue() == null) {
				//this should delete the file
			} else {
				String content = file.getValue().toString();
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
