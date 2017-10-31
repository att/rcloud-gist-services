/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsymetricFourFolderRepositoryStorageLocator implements RepositoryStorageLocator {

    private static final Pattern REPOSITORYID_FOLDER_PATTERN = Pattern.compile("(.)(.)(.)(.*)");

    private File root;

    public AsymetricFourFolderRepositoryStorageLocator(File root) {
        this.root = root;
    }

    @Override
    public File getStorageFolder(String gistId) {
        File path = null;
        Matcher matcher = REPOSITORYID_FOLDER_PATTERN.matcher(gistId);
        if (matcher.matches()) {
            int groups = matcher.groupCount();
            path = root;
            for (int i = 1; i <= groups; i++) {
                path = new File(path, matcher.group(i));
            }
        }
        return path;
    }

}
