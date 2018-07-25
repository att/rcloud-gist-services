/*******************************************************************************
* Copyright (c) 2018 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.repository.git.GistCommentStore;

@RunWith(MockitoJUnitRunner.class)
public class GistCommentStoreTest {

	private GistCommentStore instance = new GistCommentStore();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Mock
	private ObjectMapper mockObjectMapper;

	private static final String MOCK_BODY = "Some description";
	private static final long MOCK_ID = 1234l;

	@Test
	public void shouldCreateNewCommentsFile() throws IOException {
		File testDir = tempFolder.newFolder();
		File outputFile = new File(testDir, "comments.json");

		GistCommentResponse comment = new GistCommentResponse();
		comment.setBody(MOCK_BODY);
		comment.setId(MOCK_ID);
		List<GistCommentResponse> comments = Lists.newArrayList(comment);

		instance.save(outputFile, comments);

		List<GistCommentResponse> result = instance.load(outputFile);
		assertEquals(comments, result);
	}

	@Test
	public void shouldUpdateExistingCommentsFile() throws IOException {
		File testDir = tempFolder.newFolder();
		File outputFile = new File(testDir, "comments.json");

		outputFile.createNewFile();

		GistCommentResponse comment = new GistCommentResponse();
		comment.setBody(MOCK_BODY);
		comment.setId(MOCK_ID);
		List<GistCommentResponse> comments = Lists.newArrayList(comment);

		instance.save(outputFile, comments);
		
		List<GistCommentResponse> result = instance.load(outputFile);
		assertEquals(comments, result);
	}

	@Test
	public void shouldNotUpdateExistingCommentsFileInCaseOfError() throws IOException {
		File testDir = tempFolder.newFolder();
		File outputFile = new File(testDir, "comments.json");

		GistCommentResponse comment = new GistCommentResponse();
		comment.setBody(MOCK_BODY);
		comment.setId(MOCK_ID);
		List<GistCommentResponse> comments = Lists.newArrayList(comment);

		instance.save(outputFile, comments);

		GistCommentResponse comment2 = new GistCommentResponse();
		comment2.setBody("Second comment");
		comment2.setId(2345l);
		List<GistCommentResponse> comments2 = Lists.newArrayList(comments);
		comments2.add(comment2);

		doThrow(IOException.class).when(mockObjectMapper).writeValue(any(File.class), same(comments2));
		ObjectMapper functionalObjectMapper = instance.getObjectMapper();

		instance.setObjectMapper(mockObjectMapper);

		try {
			instance.save(outputFile, comments2);
			fail("Expected gist error provoked by IOException thrown by ObjectMapper");
		} catch (GistRepositoryError error) {
			// expected, no need to process
		}

		instance.setObjectMapper(functionalObjectMapper);
		List<GistCommentResponse> result = instance.load(outputFile);

		assertEquals("Initial comments should be loaded from the comments file", comments, result);
		
	}
	
	@Test
	public void shouldLoadStateFromWorkingCopyIfMainFileDoesNotExist() throws IOException {
		File testDir = tempFolder.newFolder();
		File outputFile = new File(testDir, "comments.json");

		GistCommentResponse comment = new GistCommentResponse();
		comment.setBody(MOCK_BODY);
		comment.setId(MOCK_ID);
		List<GistCommentResponse> comments = Lists.newArrayList(comment);

		instance.save(outputFile, comments);
		
		Files.move(outputFile, new File(outputFile.getParentFile(), outputFile.getName() + ".tmp"));

		List<GistCommentResponse> result = instance.load(outputFile);

		assertEquals("Initial comments should be loaded from the comments file", comments, result);
		
	}
}
