/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository;

import java.util.UUID;

public class UUIDGistIdGenerator implements GistIdGenerator {

	/* (non-Javadoc)
	 * @see com.mangosolutions.rcloud.rawgist.api.GistIdGenerator#generateId()
	 */
	@Override
	public String generateId() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
