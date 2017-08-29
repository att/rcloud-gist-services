/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.api;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;
import com.mangosolutions.rcloud.rawgist.Application;
import com.mangosolutions.rcloud.rawgist.repository.git.CollaborationDataStore;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ActiveProfiles({ "test", "default" })
@TestPropertySource(properties = { "management.port=", "security.user.password=abcd" })
public class GistRestControllerTest {

    public static MediaType GITHUB_BETA_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.github.beta+json");
    public static MediaType GITHUB_V3_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.github.v3+json");

    private MockMvc mvc;

    private String defaultGistId;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CollaborationDataStore collaborationDataStore;

    @Autowired
    private GistTestHelper gistTestHelper;

    @Before
    public void setup() throws Exception {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity()).build();
        gistTestHelper.clearGistRepository();
        defaultGistId = gistTestHelper.createGist("mock_user", "The default gist", "file1.txt",
                "This is some default content");
        gistTestHelper.emptyHazelcast();
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
                .perform(post("/gists").accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE)
                        .content(payload))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.owner.login", is("mock_user")))
                .andExpect(jsonPath("$.description", is(description))).andExpect(jsonPath("$.comments", is(0)))
                .andExpect(jsonPath("$.collaborators.length()", is(1)))
                .andExpect(jsonPath("$.collaborators[0].login", is("mock_collab_user"))).andReturn();
    }

    @Test
    @WithMockUser(username = "anonymous", roles = { "ANONYMOUS" })
    public void testCreateGistWithAnonymousUserShouldBeForbidden() throws Exception {
        String description = "the description for this gist";
        String fileName = "file1.txt";
        String fileContent = "String file contents";
        String payloadTemplate = "{\"description\": \"{}\",\"public\": true,\"files\": {\"{}\": {\"content\": \"{}\"}}}";
        String payload = this.buildMessage(payloadTemplate, description, fileName, fileContent);
        MvcResult result = mvc.perform(
                post("/gists").accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE).content(payload))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @WithMockUser(username = "mock_user2", roles = { "USER" })
    public void testUpdateGistWithNonCollaboratorUserShouldBeForbidden() throws Exception {
        String fileName = "anotherfile.txt";
        String fileContent = "Some content";
        String payloadTemplate = "{\"files\": {\"{}\": {\"content\": \"{}\"}}}";
        String payload = this.buildMessage(payloadTemplate, fileName, fileContent);
        MvcResult result = mvc
                .perform(patch("/gists/" + defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE)
                        .contentType(GITHUB_BETA_MEDIA_TYPE).content(payload))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @WithMockUser(username = "mock_collab_user", roles = { "USER" })
    public void testUpdateGistWithCollaboratorUserShouldBeOk() throws Exception {
        String fileName = "anotherfile.txt";
        String fileContent = "Some content";
        String payloadTemplate = "{\"files\": {\"{}\": {\"content\": \"{}\"}}}";
        String payload = this.buildMessage(payloadTemplate, fileName, fileContent);
        MvcResult result = mvc
                .perform(patch("/gists/" + defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE)
                        .contentType(GITHUB_BETA_MEDIA_TYPE).content(payload))
                .andExpect(status().isOk()).andExpect(jsonPath("$.owner.login", is("mock_user"))).andReturn();
    }

    @Test
    @WithMockUser(username = "another_mock_collab_user", roles = { "USER" })
    public void testUpdateGistWithWrongCollaboratorUserShouldBeForbidden() throws Exception {

        String fileName = "anotherfile.txt";
        String fileContent = "Some content";
        String payloadTemplate = "{\"files\": {\"{}\": {\"content\": \"{}\"}}}";
        String payload = this.buildMessage(payloadTemplate, fileName, fileContent);
        MvcResult result = mvc
                .perform(patch("/gists/" + defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE)
                        .contentType(GITHUB_BETA_MEDIA_TYPE).content(payload))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @WithMockUser(username = "mock_collab_user", roles = { "USER" })
    public void testCreateGistWithCollaboratorAsCollaboratorUser() throws Exception {

        String description = "the description for this gist";
        String fileName = "file1.txt";
        String fileContent = "String file contents";
        String payloadTemplate = "{\"owner\": \"mock_user\", \"description\": \"{}\",\"public\": true,\"files\": {\"{}\": {\"content\": \"{}\"}}}";
        String payload = this.buildMessage(payloadTemplate, description, fileName, fileContent);
        MvcResult result = mvc
                .perform(post("/gists").accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE)
                        .content(payload))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.owner.login", is("mock_user"))).andReturn();
    }

    @Test
    @WithMockUser(username = "mock_collab_user", roles = { "USER" })
    public void testCreateGistWithCollaboratorUserAsOwner() throws Exception {

        String description = "the description for this gist";
        String fileName = "file1.txt";
        String fileContent = "String file contents";
        String payloadTemplate = "{\"owner\": \"mock_collab_user\", \"description\": \"{}\",\"public\": true,\"files\": {\"{}\": {\"content\": \"{}\"}}}";
        String payload = this.buildMessage(payloadTemplate, description, fileName, fileContent);
        MvcResult result = mvc
                .perform(post("/gists").accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE)
                        .content(payload))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.owner.login", is("mock_collab_user")))
                .andReturn();
    }

    @Test
    @WithMockUser(username = "mock_collab_user", roles = { "USER" })
    public void testCreateGistWithCollaboratorAsUser() throws Exception {

        String description = "the description for this gist";
        String fileName = "file1.txt";
        String fileContent = "String file contents";
        String payloadTemplate = "{\"description\": \"{}\",\"public\": true,\"files\": {\"{}\": {\"content\": \"{}\"}}}";
        String payload = this.buildMessage(payloadTemplate, description, fileName, fileContent);
        MvcResult result = mvc
                .perform(post("/gists").accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE)
                        .content(payload))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.owner.login", is("mock_collab_user")))
                .andReturn();
    }

    @Test
    @WithMockUser(username = "mock_collab_user", roles = { "USER" })
    public void testCreateGistWithCollaboratorAndWrongOwnerUserShouldBeForbidden() throws Exception {

        String description = "the description for this gist";
        String fileName = "file1.txt";
        String fileContent = "String file contents";
        String payloadTemplate = "{\"owner\": \"non_collaborator_user\", \"description\": \"{}\",\"public\": true,\"files\": {\"{}\": {\"content\": \"{}\"}}}";
        String payload = this.buildMessage(payloadTemplate, description, fileName, fileContent);
        MvcResult result = mvc.perform(
                post("/gists").accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE).content(payload))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @WithMockUser("mock_user")
    public void testListGistWithMockUser() throws Exception {
        MvcResult result = mvc.perform(get("/gists").accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.length()", is(1))).andReturn();
    }

    @Test
    @WithMockUser("mock_user_2")
    public void testListGistWithMockUser2() throws Exception {
        MvcResult result = mvc
                .perform(get("/gists").accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE)
                        .with(user("mock_user_2")))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.length()", is(0))).andReturn();
    }

    @Test
    @WithMockUser("mock_user")
    public void testGetGistWithMockUser() throws Exception {
        MvcResult result = mvc
                .perform(get("/gists/" + this.defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE)
                        .contentType(GITHUB_BETA_MEDIA_TYPE))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(this.defaultGistId))).andReturn();
    }

    @Test
    @WithMockUser("mock_user")
    public void testForkRepositoryWithMockUser() throws Exception {

        // Get the gist response.
        String originalGist = mvc
                .perform(get("/gists/" + this.defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE)
                        .contentType(GITHUB_BETA_MEDIA_TYPE))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // Check that there are no forks.
        mvc.perform(get("/gists/" + this.defaultGistId + "/forks").accept(GITHUB_BETA_MEDIA_TYPE)
                .contentType(GITHUB_BETA_MEDIA_TYPE)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));

        // Fork the repository
        String forkResponse = mvc
                .perform(post("/gists/" + this.defaultGistId + "/forks").accept(GITHUB_BETA_MEDIA_TYPE)
                        .contentType(GITHUB_BETA_MEDIA_TYPE))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.files.length()", is(1)))
                .andExpect(jsonPath("$.fork_of.id", is(this.defaultGistId))).andReturn().getResponse()
                .getContentAsString();
        String forkedGistId = JsonPath.read(forkResponse, "$.id");

        Assert.assertNotEquals(this.defaultGistId, forkedGistId);
        String forkOfUrl = JsonPath.read(forkResponse, "$.fork_of.url");
        Assert.assertTrue(forkOfUrl.endsWith(this.defaultGistId));

        // update the forked gist
        String fileName = "file_in_new_gist.txt";
        String fileContent = "String file contents";
        String payloadTemplate = "{\"files\": {\"{}\": {\"content\": \"{}\"}}}";
        String payload = this.buildMessage(payloadTemplate, fileName, fileContent);
        mvc.perform(patch("/gists/" + forkedGistId).accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE)
                .content(payload)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(forkedGistId))).andExpect(jsonPath("$.files.length()", is(2)))
                .andReturn();

        // check that original gist hasn't changed
        String originalGist2 = mvc
                .perform(get("/gists/" + this.defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE)
                        .contentType(GITHUB_BETA_MEDIA_TYPE))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Assert.assertEquals(originalGist, originalGist2);

        // get the list of forks
        mvc.perform(get("/gists/" + this.defaultGistId + "/forks").accept(GITHUB_BETA_MEDIA_TYPE)
                .contentType(GITHUB_BETA_MEDIA_TYPE)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1))).andExpect(jsonPath("$.[0].id", is(forkedGistId)));

    }

    @Test
    @WithMockUser("mock_user")
    public void testForkRepositoryWithMockUserAndRenameFork() throws Exception {

        // Get the gist response.
        String originalGist = mvc
                .perform(get("/gists/" + this.defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE)
                        .contentType(GITHUB_BETA_MEDIA_TYPE))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // Fork the repository
        String forkResponse = mvc
                .perform(post("/gists/" + this.defaultGistId + "/forks").accept(GITHUB_BETA_MEDIA_TYPE)
                        .contentType(GITHUB_BETA_MEDIA_TYPE))
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.files.length()", is(1)))
                .andExpect(jsonPath("$.fork_of.id", is(this.defaultGistId))).andReturn().getResponse()
                .getContentAsString();
        String forkedGistId = JsonPath.read(forkResponse, "$.id");
        String description = "The new description for the gist";
        String payloadTemplate = "{\"description\": \"{}\"}";
        String payload = this.buildMessage(payloadTemplate, description);
        mvc.perform(patch("/gists/" + forkedGistId).accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE)
                .content(payload)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(forkedGistId))).andExpect(jsonPath("$.files.length()", is(1)))
                .andExpect(jsonPath("$.description", is(description))).andReturn();

    }

    @Test
    @WithMockUser("mock_user_2")
    public void testGetGistWithMockUser2() throws Exception {
        MvcResult result = mvc
                .perform(get("/gists/" + this.defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE)
                        .contentType(GITHUB_BETA_MEDIA_TYPE))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(this.defaultGistId))).andReturn();
    }

    @Test
    @WithMockUser("mock_user")
    public void testGetGistHistory() throws Exception {
        int historySize = 30;
        int historyIndex = 10;
        String historyVersion = null;
        {
            MvcResult result = mvc
                    .perform(get("/gists/" + this.defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE)
                            .contentType(GITHUB_BETA_MEDIA_TYPE))
                    .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.id", is(this.defaultGistId)))
                    .andExpect(jsonPath("$.history.length()", is(1))).andExpect(jsonPath("$.files.length()", is(1)))
                    .andReturn();
            addFilesToGist(this.defaultGistId, historySize);
        }
        {
            MvcResult result = mvc
                    .perform(get("/gists/" + this.defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE)
                            .contentType(GITHUB_BETA_MEDIA_TYPE))
                    .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.id", is(this.defaultGistId)))
                    .andExpect(jsonPath("$.history.length()", is(historySize + 1)))
                    .andExpect(jsonPath("$.files.length()", is(historySize + 1))).andReturn();
            String response = result.getResponse().getContentAsString();
            historyVersion = JsonPath.read(response, "$.history[" + historyIndex + "].version");
        }
        {
            MvcResult result = mvc
                    .perform(get("/gists/" + this.defaultGistId + "/" + historyVersion).accept(GITHUB_BETA_MEDIA_TYPE)
                            .contentType(GITHUB_BETA_MEDIA_TYPE))
                    .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.id", is(this.defaultGistId)))
                    .andExpect(jsonPath("$.files.length()", is(historySize + 1 - historyIndex))).andReturn();
            String response = result.getResponse().getContentAsString();
            System.out.println(response);
            Map<?, ?> files = JsonPath.read(response, "$.files");
            Assert.assertEquals(historySize + 1 - historyIndex, files.size());
        }
    }

    @Test
    @WithMockUser("mock_user")
    public void testGistCacheRefresh() throws Exception {

        // get the gist and check the collaborators
        mvc.perform(
                get("/gists/" + this.defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE))
                .andExpect(jsonPath("$.collaborators.length()", is(1)))
                .andExpect(jsonPath("$.collaborators[0].login", is("mock_collab_user"))).andReturn();

        // refresh the application context (should clear the cache)
        mvc.perform(post("/manage/refresh").with(httpBasic("admin", "abcd"))).andExpect(status().isOk());

        // update the collaborators

        Map<String, List<String>> collabs = new HashMap<>();
        collabs.put("mock_user", Arrays.asList("dino", "gerry"));
        this.collaborationDataStore.updateCollaborators(collabs);

        // get the gist again this should have the new collaborators
        mvc.perform(
                get("/gists/" + this.defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE).contentType(GITHUB_BETA_MEDIA_TYPE))
                .andExpect(jsonPath("$.collaborators.length()", is(2)))
                .andExpect(jsonPath("$.collaborators[0].login", is("dino")))
                .andExpect(jsonPath("$.collaborators[1].login", is("gerry"))).andReturn();
        
        // refresh the application (should clear the cache)
        mvc.perform(post("/manage/refresh").with(httpBasic("admin", "abcd"))).andExpect(status().isOk());

        // get the gist again should have the original collaborators
        MvcResult result = mvc
                .perform(get("/gists/" + this.defaultGistId).accept(GITHUB_BETA_MEDIA_TYPE)
                        .contentType(GITHUB_BETA_MEDIA_TYPE))
                .andExpect(jsonPath("$.collaborators.length()", is(1)))
                .andExpect(jsonPath("$.collaborators[0].login", is("mock_collab_user"))).andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println(response);

    }

    private void addFilesToGist(String gistId, int historySize) throws Exception {
        for (int i = 0; i < historySize; i++) {
            String fileName = i + "otherfile.txt";
            String fileContent = "Some content for " + i;
            String payloadTemplate = "{\"files\": {\"{}\": {\"content\": \"{}\"}}}";
            String payload = this.buildMessage(payloadTemplate, fileName, fileContent);
            MvcResult result = mvc
                    .perform(patch("/gists/" + gistId).accept(GITHUB_BETA_MEDIA_TYPE)
                            .contentType(GITHUB_BETA_MEDIA_TYPE).content(payload))
                    .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn();

        }

    }

    private String buildMessage(String format, Object... params) {
        return MessageFormatter.arrayFormat(format, params).getMessage();
    }

    private UserDetails createUserDetails(String name, Collection<GrantedAuthority> authorities) {
        return new User(name, "", authorities);
    }
}
