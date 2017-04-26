/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import com.mangosolutions.rcloud.rawgist.model.FileContent;

public interface FileContentCache {

	FileContent load(String contentId, String path);

	FileContent save(String contentId, String path, FileContent content);

}
