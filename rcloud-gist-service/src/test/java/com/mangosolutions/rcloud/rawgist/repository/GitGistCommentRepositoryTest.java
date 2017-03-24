/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangosolutions.rcloud.rawgist.model.FileContent;
import com.mangosolutions.rcloud.rawgist.model.FileDefinition;
import com.mangosolutions.rcloud.rawgist.model.GistHistory;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;


@RunWith(SpringRunner.class)
@AutoConfigureJsonTesters
@JsonTest
public class GitGistCommentRepositoryTest {

	private GitGistRepository repository;

	private String gistId;

	private UserDetails userDetails;

	@Autowired
	private ObjectMapper objectMapper;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setup() {
		File repositoryFolder = folder.getRoot();
		gistId = UUID.randomUUID().toString();
		Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
		userDetails = new User("gist_user", "gist_user_pwd", authorities);
		repository = new GitGistRepository(repositoryFolder, gistId, objectMapper, userDetails);
	}

	@Test
	public void createNewGistTest() {
		String expectedDescription = "This is a cool gist";
		String expectedFilename = "i_am_file.R";
		String expectedContent = "I am the content of the file";
		GistResponse response = createGist(expectedDescription, new String[]{expectedFilename, expectedContent});
		validateResponse(1, expectedDescription, expectedFilename, expectedContent, response);
		validateHistory(response, 1);
	}


	@Test
	public void getGistTest() {
		String expectedDescription = "This is a cool gist";
		String expectedFilename = "i_am_file.R";
		String expectedContent = "I am the content of the file";
		this.createGist(expectedDescription, new String[]{expectedFilename, expectedContent});
		GistResponse response = repository.getGist(userDetails);
		validateResponse(1, expectedDescription, expectedFilename, expectedContent, response);
		validateHistory(response, 1);
	}

	@Test
	public void editGistContentTest() {
		String expectedDescription = "This is a cool gist";
		String expectedFilename = "i_am_file.R";
		String initialContent = "I am the content of the file";
		String updatedContent = "I am new content, this should be different";
		this.createGist(expectedDescription, new String[]{expectedFilename, initialContent});
		GistResponse response = this.updateGist(new String[]{expectedFilename, updatedContent});
		validateResponse(1, expectedDescription, expectedFilename, updatedContent, response);
		validateHistory(response, 2);
	}

	@Test
	public void addNewGistFileTest() {
		String expectedDescription = "This is a cool gist";
		String expectedFilename = "i_am_file_1.R";

		String initialContent = "I am the content of the file";
		String newFilename = "i_am_file_2.R";
		String newContent = "I am the content of a different file";
		this.createGist(expectedDescription, new String[]{expectedFilename, initialContent});
		GistResponse response = this.updateGist(new String[]{newFilename, newContent});
		validateResponse(2, expectedDescription, newFilename, newContent, response);
		validateHistory(response, 2);
	}

	@Test
	public void deleteGistFileTest() {
		String expectedDescription = "This is a cool gist";
		String expectedFilename = "i_am_file_1.R";

		String initialContent = "I am the content of the file";
		String newFilename = "i_am_file_2.R";
		String newContent = "I am the content of a different file";
		this.createGist(expectedDescription, new String[]{expectedFilename, initialContent});
		this.updateGist(new String[]{newFilename, newContent});
		GistResponse response = this.updateGist(new String[]{expectedFilename});
		validateResponse(1, expectedDescription, newFilename, newContent, response);
		validateHistory(response, 3);
	}


	@Test
	public void getGistRevision() {
		String expectedDescription = "This is a cool gist";
		String initialFilename = "i_am_file_1.R";

		String initialContent = "I am the content of the file";
		String newFilename = "i_am_file_2.R";
		String newContent = "I am the content of a different file";
		this.createGist(expectedDescription, new String[]{initialFilename, initialContent});
		this.updateGist(new String[]{newFilename, newContent});

		GistResponse response = this.updateGist(new String[]{initialFilename});
		validateResponse(1, expectedDescription, newFilename, newContent, response);
		validateHistory(response, 3);

		GistHistory history = response.getHistory().get(1);
		String commitId = history.getVersion();
		response = repository.getGist(commitId, userDetails);
		validateResponse(2, expectedDescription, initialFilename, initialContent, response);
		validateResponse(2, expectedDescription, newFilename, newContent, response);
		validateHistory(response, 2);
	}

	@Test
	public void updateGistWithDifferentUser() {
		String expectedDescription = "This is a cool gist";
		String expectedFilename = "i_am_file.R";
		String initialContent = "I am the content of the file";
		String updatedContent = "I am new content, this should be different";
		this.createGist(expectedDescription, new String[]{expectedFilename, initialContent});
		GistResponse response = this.updateGist(new String[]{expectedFilename, updatedContent});
		validateResponse(1, expectedDescription, expectedFilename, updatedContent, response);
		validateHistory(response, 2);

		String newFilename = "i_am_file_2.R";
		String newContent = "I am the content of a different file";

		GistRequest request = createGistRequest(null, new String[]{newFilename, newContent});
		Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
		UserDetails userDetails = new User("another_gist_user", "gist_user_pwd", authorities);
		response = repository.editGist(request, userDetails);
		validateResponse(2, expectedDescription, newFilename, newContent, response);
		GistHistory history = response.getHistory().get(0);
		Assert.assertEquals(userDetails.getUsername(), history.getUser().getLogin());
	}

	private GistResponse updateGist(String[] contents) {
		GistRequest request = createGistRequest(null, contents);
		return repository.editGist(request, userDetails);
	}

	private GistResponse createGist(String description, String[]... contents) {
		GistRequest request = createGistRequest(description, contents);
		return repository.createGist(request, userDetails);
	}

	private GistRequest createGistRequest(String description, String[]... contents) {
		GistRequest request = new GistRequest();
		request.setDescription(description);
		Map<String, FileDefinition> files = new HashMap<>();
		for(String[] content: contents) {
			String fileName = content[0];
			FileDefinition fileDefinition = null;
			if(content.length > 1 && content[1] != null) {
				String fileContents = content[1];
				fileDefinition = new FileDefinition();
				fileDefinition.setContent(fileContents);
			}
			if(content.length > 2 && content[2] != null) {
				fileDefinition = fileDefinition == null? new FileDefinition(): fileDefinition;
				String newName = content[2];
				fileDefinition.setFilename(newName);
			}
			files.put(fileName, fileDefinition);
		}
		request.setFiles(files);
		return request;
	}

	private void validateHistory(GistResponse response, int expectedHistoryLength) {
		List<GistHistory> histories = response.getHistory();
		Assert.assertEquals(expectedHistoryLength, histories.size());
		for(GistHistory history: histories) {
			Assert.assertEquals(this.userDetails.getUsername(), history.getUser().getLogin());
		}
	}

	private void validateResponse(int files, String expectedDescription, String expectedFilename, String expectedContent,
			GistResponse response) {
		String id = response.getId();
		Assert.assertEquals(this.gistId, id);
		Assert.assertEquals(Integer.valueOf(0), response.getComments());
		Assert.assertEquals(expectedDescription, response.getDescription());
		Map<String, FileContent> gistFiles = response.getFiles();
		Assert.assertEquals(files, gistFiles.size());
		FileContent content = gistFiles.get(expectedFilename);
		Assert.assertNotNull(content);
		Assert.assertEquals(expectedFilename, content.getFilename());
		Assert.assertEquals(expectedContent, content.getContent());
		Assert.assertEquals("R", content.getLanguage());
		Assert.assertEquals(Long.valueOf(expectedContent.getBytes().length), content.getSize());
	}


}
