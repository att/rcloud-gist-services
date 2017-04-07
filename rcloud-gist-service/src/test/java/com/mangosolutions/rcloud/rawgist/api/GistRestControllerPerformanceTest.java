package com.mangosolutions.rcloud.rawgist.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
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

import com.mangosolutions.rcloud.rawgist.Application;
import com.mangosolutions.rcloud.rawgist.model.FileDefinition;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.git.GitGistRepositoryService;



@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles({"test", "default"})
public class GistRestControllerPerformanceTest {

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
	public void testConsistentGistWriteSpeed() throws Exception {
		int historySize = 300;
		double[] durations = addFilesToGist(this.defaultGistId, historySize);
		StandardDeviation stdDev = new StandardDeviation();
		
		double min = StatUtils.min(durations);
		double max = StatUtils.max(durations);
		double dev = stdDev.evaluate(durations);
		System.out.println(min);
		System.out.println(max);
		System.out.println(dev);
		System.out.println(Arrays.toString(durations));
		
	}
	
	private double[] addFilesToGist(String gistId, int historySize) throws Exception {
		double[] durations = new double[historySize];
		
		for(int i = -1; i < historySize; i++) {
			String fileName = i + "otherfile.txt";
			String fileContent = "Some content for " + i;
			String payloadTemplate = "{\"files\": {\"{}\": {\"content\": \"{}\"}}}";
			String payload = this.buildMessage(payloadTemplate, fileName, fileContent);
			long start = System.currentTimeMillis();
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
			long end = System.currentTimeMillis();
			double diff = end - start;
			if(i >= 0) {
				durations[i] = diff;
			}
		}
		return durations;
		
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
