/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;

public interface RepositoryStorageLocator {

    /**
     * Gets the absolute location of the gist folder.
     * 
     * @param gistId
     *            the gistId to resolve to a location
     * @return A File object representing the root folder of the gist storage
     *         location
     */
    File getStorageFolder(String gistId);

}
