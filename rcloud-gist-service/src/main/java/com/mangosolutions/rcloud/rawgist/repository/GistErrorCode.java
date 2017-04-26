/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository;

public enum GistErrorCode {

	ERR_METADATA_NOT_READABLE,
	ERR_METADATA_NOT_WRITEABLE,
	ERR_COMMENTS_NOT_READABLE,
	ERR_COMMENTS_NOT_WRITEABLE,
	ERR_GIST_UPDATE_FAILURE,
	ERR_GIST_FORK_FAILURE,
	ERR_GIST_CONTENT_NOT_READABLE,
	ERR_GIST_CONTENT_NOT_AVAILABLE,
	ERR_GIST_NOT_EXIST,
	ERR_COMMENT_NOT_EXIST,
	FATAL_GIST_INITIALISATION,
	ERR_ACL_WRITE_DENIED,
	ERR_ACL_READ_DENIED
}
