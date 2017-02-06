package com.mangosolutions.rcloud.rawgist.api;

import java.util.UUID;

public class UUIDGistIdGenerator implements GistIdGenerator {

	/* (non-Javadoc)
	 * @see com.mangosolutions.rcloud.rawgist.api.GistIdGenerator#generateId()
	 */
	@Override
	public String generateId() {
		return UUID.randomUUID().toString();
	}
}
