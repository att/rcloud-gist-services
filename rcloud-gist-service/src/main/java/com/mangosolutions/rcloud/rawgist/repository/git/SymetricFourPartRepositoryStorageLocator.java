/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;

import com.google.common.base.Splitter;

public class SymetricFourPartRepositoryStorageLocator implements RepositoryStorageLocator {

	private static final Splitter REPOSITORYID_FOLDER_SPLITTER = Splitter.fixedLength(4);

	private File root;

	public SymetricFourPartRepositoryStorageLocator(File root) {
		this.root = root;
	}

	@Override
	public File getStorageFolder(String gistId) {
		File folder = root;
		for (String path : REPOSITORYID_FOLDER_SPLITTER.split(gistId)) {
			folder = new File(folder, path);
		}
		return folder;
	}

}
