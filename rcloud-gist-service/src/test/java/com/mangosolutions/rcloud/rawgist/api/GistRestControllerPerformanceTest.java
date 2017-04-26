package com.mangosolutions.rcloud.rawgist.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangosolutions.rcloud.rawgist.Application;
import com.mangosolutions.rcloud.rawgist.model.FileContent;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;




@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles({"test", "default"})
public class GistRestControllerPerformanceTest {

	public static MediaType GITHUB_BETA_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.github.beta+json");
	public static MediaType GITHUB_V3_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.github.v3+json");

	private MockMvc mvc;
	
	private String defaultGistId;
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
	private GistTestHelper gistTestHelper;
	
	@Autowired
	private ObjectMapper objectMapper;

	@Before
	public void setup() throws Exception {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
		
		gistTestHelper.clearGistRepository();
		gistTestHelper.emptyHazelcast();
		defaultGistId = gistTestHelper.createGist("mock_user", "The default gist", "file1.txt", "This is some default content");
		gistTestHelper.warmupWebService(this.mvc, defaultGistId);
	}
	
	@Test
	@WithMockUser("mock_user")
	public void testConsistentGistWriteSpeed() throws Exception {
		int historySize = 300;
//		int historySize = 10;
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
		
		for(int i = 0; i < historySize; i++) {
			String fileName = i + "otherfile.txt";
			String fileName2 = i + "anotherfile.txt";
			String fileContent = "Some content for " + i;
			String payloadTemplate = "{\"files\": {\"{}\": {\"content\": \"{}\"}, \"{}\": {\"content\": \"{}\"}}}";
			String payload = this.buildMessage(payloadTemplate, fileName, fileContent, fileName2, fileContent);
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
			String content = result.getResponse().getContentAsString();
			GistResponse response = objectMapper.readValue(content, GistResponse.class);
			Map<String, FileContent> files = response.getFiles();
			Assert.assertEquals(((i + 1)*2) + 1, files.keySet().size());
		}
		return durations;
		
	}

	
	private String buildMessage(String format, Object... params) {
		 return MessageFormatter.arrayFormat(format, params).getMessage();
	}

}
