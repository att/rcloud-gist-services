/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.util.UUID;

import com.mangosolutions.rcloud.rawgist.repository.GistIdGenerator;

public class UUIDGistIdGenerator implements GistIdGenerator {

	/* (non-Javadoc)
	 * @see com.mangosolutions.rcloud.rawgist.api.GistIdGenerator#generateId()
	 */
	@Override
	public String generateId() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
