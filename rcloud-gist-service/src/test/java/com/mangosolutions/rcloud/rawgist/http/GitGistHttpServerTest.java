/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.http;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.Status;
import org.ajoberstar.grgit.Status.Changes;
import org.ajoberstar.grgit.operation.AddOp;
import org.ajoberstar.grgit.operation.CloneOp;
import org.ajoberstar.grgit.operation.CommitOp;
import org.ajoberstar.grgit.operation.PushOp;
import org.ajoberstar.grgit.operation.StatusOp;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.mangosolutions.rcloud.rawgist.Application;
import com.mangosolutions.rcloud.rawgist.api.GistTestHelper;
import com.mangosolutions.rcloud.sessionkeyauth.SessionKeyServerService;




@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { Application.class }, webEnvironment=WebEnvironment.RANDOM_PORT)
@ContextConfiguration(loader = SpringBootContextLoader.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ActiveProfiles({"test", "default"})
public class GitGistHttpServerTest {

    public static MediaType GITHUB_BETA_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.github.beta+json");
    public static MediaType GITHUB_V3_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.github.v3+json");

    private MockMvc mvc;

    private String defaultGistId;

    @LocalServerPort
    int randomServerPort;
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GistTestHelper gistTestHelper;
    
    @Autowired
    private GitServiceAuthenticationManager gitServiceAuthenticationManager;

    @Before
    public void setup() throws Exception {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity()).build();
        gistTestHelper.clearGistRepository();
        defaultGistId = gistTestHelper.createGist("mock_user", "The default gist", "file1.txt",
                "This is some default content");
        gistTestHelper.emptyHazelcast();
        
        CredentialsProvider.setDefault(new UsernamePasswordCredentialsProvider("mock_user", "abcdefg"));
        SessionKeyServerService service = Mockito.mock(SessionKeyServerService.class);
        Mockito.when(service.authenticate("mock_user", "abcdefg")).thenReturn("1234567890");
        gitServiceAuthenticationManager.setSessionKeyServerService(service);
        
    }

    @Test
    public void testCloneGist() throws Exception {
        CloneOp op = new CloneOp();
        op.setUri("http://localhost:" + randomServerPort + "/repositories/" + defaultGistId);
        File tempRoot = folder.getRoot();
        File cloneFolder = new File(tempRoot, this.defaultGistId);
        op.setDir(cloneFolder);
        op.call();
        Assert.assertTrue(cloneFolder.exists());
    }
    
    @Test
    public void testUpdateGistFileAndPush() throws Exception {
        CloneOp op = new CloneOp();
        op.setUri("http://localhost:" + randomServerPort + "/repositories/" + defaultGistId);
        File tempRoot = folder.getRoot();
        File cloneFolder1 = new File(tempRoot, this.defaultGistId + "1");
        op.setDir(cloneFolder1);
        Grgit git = op.call();
        Assert.assertTrue(cloneFolder1.exists());
        File file1 = new File(cloneFolder1, "file1.txt");
        Assert.assertTrue(file1.exists());
        String file1Content = FileUtils.readFileToString(file1);
        Assert.assertEquals("This is some default content", file1Content);
        FileUtils.write(file1, " this is some updated content", true);
        AddOp addOp = new AddOp(git.getRepository());
        addOp.setUpdate(true);
        Set<String> patterns = new HashSet<>();
        patterns.add(".");
        addOp.setPatterns(patterns);
        addOp.call();
        CommitOp commitOp = new CommitOp(git.getRepository());
        commitOp.setMessage("Updated file for test case");
        commitOp.call();
        PushOp pushOp = new PushOp(git.getRepository());
        pushOp.setRemote("origin");
        pushOp.call();
        StatusOp statusOp = new StatusOp(git.getRepository());
        Status status = statusOp.call();
        Changes stagedChanges = status.getStaged();
        Assert.assertEquals(0, stagedChanges.getAllChanges().size());
        Changes unstagedChanges = status.getUnstaged();
        Assert.assertEquals(0, unstagedChanges.getAllChanges().size());
        
    }

}
