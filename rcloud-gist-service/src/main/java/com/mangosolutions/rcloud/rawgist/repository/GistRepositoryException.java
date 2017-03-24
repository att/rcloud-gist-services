/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository;

public class GistRepositoryException extends RuntimeException {

	private static final long serialVersionUID = -3925461305121147713L;

	private GistError error;

	public GistRepositoryException(GistError error, Throwable cause) {
		super(error.getFormattedMessage(), cause);
		this.error = error;
	}

	public GistRepositoryException(GistError error) {
		this(error, null);
	}

	public GistError getGistError() {
		return error;
	}

}
