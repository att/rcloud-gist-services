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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.mangosolutions.rcloud.rawgist.repository.git.GistMetadata;
import com.mangosolutions.rcloud.rawgist.repository.git.GistMetadataStore;

@RunWith(MockitoJUnitRunner.class)
public class GistMetadataStoreTest {

	private GistMetadataStore instance = new GistMetadataStore();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Mock
	private ObjectMapper mockObjectMapper;

	private static final String MOCK_DESCRIPTION = "Some description";
	private static final String MOCK_ID = "Some id";

	@Test
	public void shouldCreateNewMetadataFile() throws IOException {
		File testDir = tempFolder.newFolder();
		File outputFile = new File(testDir, "gist.json");

		GistMetadata gistMetadata = new GistMetadata();
		gistMetadata.setDescription(MOCK_DESCRIPTION);
		gistMetadata.setId(MOCK_ID);
		instance.save(outputFile, gistMetadata);

		GistMetadata result = instance.load(outputFile);
		assertEquals(gistMetadata, result);
	}

	@Test
	public void shouldUpdateExistingMetadataFile() throws IOException {
		File testDir = tempFolder.newFolder();
		File outputFile = new File(testDir, "gist.json");

		outputFile.createNewFile();

		GistMetadata gistMetadata = new GistMetadata();
		gistMetadata.setDescription(MOCK_DESCRIPTION);
		gistMetadata.setId(MOCK_ID);
		instance.save(outputFile, gistMetadata);

		GistMetadata result = instance.load(outputFile);
		assertEquals(gistMetadata, result);
	}

	@Test
	public void shouldNotUpdateExistingMetadataFileInCaseOfError() throws IOException {
		File testDir = tempFolder.newFolder();
		File outputFile = new File(testDir, "gist.json");

		GistMetadata gistMetadata = new GistMetadata();
		gistMetadata.setDescription(MOCK_DESCRIPTION);
		gistMetadata.setId(MOCK_ID);
		instance.save(outputFile, gistMetadata);

		GistMetadata gistMetadataNew = new GistMetadata();
		gistMetadataNew.setDescription(MOCK_DESCRIPTION);
		gistMetadataNew.setId(MOCK_ID);
		gistMetadataNew.setOwner("Someone");

		doThrow(IOException.class).when(mockObjectMapper).writeValue(any(File.class), same(gistMetadataNew));
		ObjectMapper functionalObjectMapper = instance.getObjectMapper();

		instance.setObjectMapper(mockObjectMapper);

		try {
			instance.save(outputFile, gistMetadataNew);
			fail("Expected gist error provoked by IOException thrown by ObjectMapper");
		} catch (GistRepositoryError error) {
			// expected, no need to process
		}

		instance.setObjectMapper(functionalObjectMapper);
		GistMetadata result = instance.load(outputFile);

		assertEquals("Initial Gist metadata should be loaded from metadata file", gistMetadata, result);
	}
	
	@Test
	public void shouldLoadStateFromTmpStateFileIfMainFileDoesNotExist() throws IOException {
		File testDir = tempFolder.newFolder();
		File outputFile = new File(testDir, "gist.json");

		GistMetadata gistMetadata = new GistMetadata();
		gistMetadata.setDescription(MOCK_DESCRIPTION);
		gistMetadata.setId(MOCK_ID);
		instance.save(outputFile, gistMetadata);
		
		Files.move(outputFile, new File(outputFile.getParentFile(), outputFile.getName() + ".tmp"));

		GistMetadata result = instance.load(outputFile);

		assertEquals("Initial Gist metadata should be loaded from metadata file", gistMetadata, result);
	}
}
