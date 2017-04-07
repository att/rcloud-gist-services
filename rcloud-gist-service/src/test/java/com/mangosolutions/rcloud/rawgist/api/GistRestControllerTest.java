package com.mangosolutions.rcloud.rawgist.api;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;
import com.mangosolutions.rcloud.rawgist.Application;
import com.mangosolutions.rcloud.rawgist.model.FileDefinition;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GitGistRepositoryService;

import net.minidev.json.JSONArray;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles({"test", "default"})
public class GistRestControllerTest {

	public static MediaType GITHUB_BETA_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.github.beta+json");
	public static MediaType GITHUB_V3_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.github.v3+json");

	private MockMvc mvc;
	
	private String defaultGistId;
	
	@Autowired
	private GitGistRepositoryService service;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void setup() throws Exception {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
		//delete all the gists
		String tmpdir = System.getProperty("java.io.tmpdir");
		File gistFolder = new File(tmpdir + "/gists");
		FileUtils.forceDelete(gistFolder);
		FileUtils.forceMkdir(gistFolder);
		FileUtils.forceMkdir(new File(gistFolder, ".recycle"));
		defaultGistId = createGist("mock_user", "The default gist", "file1.txt", "This is some default content");
	}

	@Test
	@WithMockUser("mock_user")
	public void testCreateGist() throws Exception {
		String description = "the description for this gist";
		String fileName = "file1.txt";
		String fileContent = "String file contents";
		String payloadTemplate = "{\"description\": \"{}\",\"public\": true,\"files\": {\"{}\": {\"content\": \"{}\"}}}";
		String payload = this.buildMessage(payloadTemplate, description, fileName, fileContent);
		MvcResult result = mvc
			.perform(
				post("/gists")
				.accept(GITHUB_BETA_MEDIA_TYPE)
				.contentType(GITHUB_BETA_MEDIA_TYPE)
				.content(payload)
			)
			.andExpect(status().isCreated())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$.owner.login", is("mock_user")))
			.andExpect(jsonPath("$.description", is(description)))
			.andExpect(jsonPath("$.comments", is(0)))
			.andReturn();
	}
	
	@Test
	@WithMockUser("mock_user")
	public void testListGistWithMockUser() throws Exception {
		MvcResult result = mvc
			.perform(
				get("/gists")
				.accept(GITHUB_BETA_MEDIA_TYPE)
				.contentType(GITHUB_BETA_MEDIA_TYPE)
			)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$.length()", is(1)))
			.andReturn();
	}
	
	@Test
	@WithMockUser("mock_user_2")
	public void testListGistWithMockUser2() throws Exception {
		MvcResult result = mvc
			.perform(
				get("/gists")
				.accept(GITHUB_BETA_MEDIA_TYPE)
				.contentType(GITHUB_BETA_MEDIA_TYPE)
				.with(user("mock_user_2"))
			)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$.length()", is(0)))
			.andReturn();
	}
	
	@Test
	@WithMockUser("mock_user")
	public void testGetGistWithMockUser() throws Exception {
		MvcResult result = mvc
			.perform(
				get("/gists/" + this.defaultGistId)
				.accept(GITHUB_BETA_MEDIA_TYPE)
				.contentType(GITHUB_BETA_MEDIA_TYPE)
			)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$.id", is(this.defaultGistId)))
			.andReturn();
	}
	
	@Test
	@WithMockUser("mock_user_2")
	public void testGetGistWithMockUser2() throws Exception {
		MvcResult result = mvc
			.perform(
				get("/gists/" + this.defaultGistId)
				.accept(GITHUB_BETA_MEDIA_TYPE)
				.contentType(GITHUB_BETA_MEDIA_TYPE)
			)
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$.id", is(this.defaultGistId)))
			.andReturn();
	}
	
	@Test
	@WithMockUser("mock_user")
	public void testGetGistHistory() throws Exception {
		int historySize = 30;
		int historyIndex = 10;
		String historyVersion = null;
		{
			MvcResult result = mvc
				.perform(
					get("/gists/" + this.defaultGistId)
					.accept(GITHUB_BETA_MEDIA_TYPE)
					.contentType(GITHUB_BETA_MEDIA_TYPE)
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.id", is(this.defaultGistId)))
				.andExpect(jsonPath("$.history.length()", is(1)))
				.andExpect(jsonPath("$.files.length()", is(1)))
				.andReturn();
			addFilesToGist(this.defaultGistId, historySize);
		}
		{
			MvcResult result = mvc
				.perform(
					get("/gists/" + this.defaultGistId)
					.accept(GITHUB_BETA_MEDIA_TYPE)
					.contentType(GITHUB_BETA_MEDIA_TYPE)
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.id", is(this.defaultGistId)))
				.andExpect(jsonPath("$.history.length()", is(historySize + 1)))
				.andExpect(jsonPath("$.files.length()", is(historySize + 1)))
				.andReturn();
			String response = result.getResponse().getContentAsString();
			historyVersion = JsonPath.read(response, "$.history[" + historyIndex + "].version");
		}
		{
			MvcResult result = mvc
				.perform(
					get("/gists/" + this.defaultGistId + "/" + historyVersion)
					.accept(GITHUB_BETA_MEDIA_TYPE)
					.contentType(GITHUB_BETA_MEDIA_TYPE)
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.id", is(this.defaultGistId)))
				.andExpect(jsonPath("$.files.length()", is(historySize + 1 - historyIndex)))
				.andReturn();
			String response = result.getResponse().getContentAsString();
			System.out.println(response);
			Map<?,?> files = JsonPath.read(response, "$.files");
			Assert.assertEquals(historySize + 1 - historyIndex, files.size());
		}
	}
	
	private void addFilesToGist(String gistId, int historySize) throws Exception {
		for(int i = 0; i < historySize; i++) {
			String fileName = i + "otherfile.txt";
			String fileContent = "Some content for " + i;
			String payloadTemplate = "{\"files\": {\"{}\": {\"content\": \"{}\"}}}";
			String payload = this.buildMessage(payloadTemplate, fileName, fileContent);
			MvcResult result = mvc
				.perform(
					patch("/gists/" + gistId)
					.accept(GITHUB_BETA_MEDIA_TYPE)
					.contentType(GITHUB_BETA_MEDIA_TYPE)
					.content(payload)
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andReturn();
		}
		
	}

	private String createGist(String user, String description, String fileName, String fileContent) throws Exception {
		GistRequest request = new GistRequest();
		request.setDescription(description);
		request.setPublic(false);

		Map<String, FileDefinition> files = new HashMap<>();
		FileDefinition def = new FileDefinition();
		def.setContent(fileContent);
		files.put(fileName, def);
		request.setFiles(files);
		Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
		UserDetails userDetails = new User(user, "gist_user_pwd", authorities);
		GistResponse response = this.service.createGist(request, userDetails);
		return response.getId();
	}
	
	
	private String buildMessage(String format, Object... params) {
		 return MessageFormatter.arrayFormat(format, params).getMessage();
	}

}
