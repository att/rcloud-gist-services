package com.mangosolutions.rcloud.rawgist.api;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

import com.mangosolutions.rcloud.rawgist.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles({"test", "default"})
public class GistRestControllerTest {

	public static MediaType GITHUB_BETA_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.github.beta+json");
	public static MediaType GITHUB_V3_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.github.v3+json");

	private MockMvc mvc;

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
		createGist("The default gist", "file1.txt", "This is some default content");
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
	
	
	private void createGist(String description, String fileName, String fileContent) throws Exception {
		String payloadTemplate = "{\"description\": \"{}\",\"public\": true,\"files\": {\"{}\": {\"content\": \"{}\"}}}";
		String payload = this.buildMessage(payloadTemplate, description, fileName, fileContent);
		mvc
			.perform(
				post("/gists")
				.accept(GITHUB_BETA_MEDIA_TYPE)
				.contentType(GITHUB_BETA_MEDIA_TYPE)
				.content(payload)
				.with(user("mock_user"))
			);
	}
	
	
	private String buildMessage(String format, Object... params) {
		 return MessageFormatter.arrayFormat(format, params).getMessage();
	}

}
