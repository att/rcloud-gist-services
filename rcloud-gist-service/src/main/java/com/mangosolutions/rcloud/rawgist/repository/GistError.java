package com.mangosolutions.rcloud.rawgist.repository;

import java.io.Serializable;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.StringUtils;

public class GistError {

	public enum GistErrorCode {
	
		ERR_METADATA_NOT_READABLE, 
		ERR_METADATA_NOT_WRITEABLE, 
		ERR_COMMENTS_NOT_READABLE, 
		ERR_COMMENTS_NOT_WRITEABLE,
		ERR_GIST_UPDATE_FAILURE,
		ERR_GIST_CONTENT_NOT_READABLE,
		ERR_GIST_CONTENT_NOT_AVAILABLE,
		ERR_GIST_NOT_EXIST,
		ERR_COMMENT_NOT_EXIST,
		FATAL_GIST_INITIALISATION
	}
	
	private static final String PREFIX_FORMAT = "{}: ";
	
	private GistErrorCode code;
	
	private String message;
	
	private Serializable[] params;
	
	public GistError(GistErrorCode code, String message, Serializable... params) {
		this.code = code;
		this.message = message;
		this.params = params;
	}
	
	public GistErrorCode getCode() {
		return code;
	}
	
	
	public String getMessage() {
		return this.message;
	}
	
	public Serializable[] getParams() {
		return this.params;
	}

	public String getFormattedMessage() {
		String prefix = "";
		if(!StringUtils.isEmpty(code)) {
			prefix = this.format(PREFIX_FORMAT, code);
		}
		return prefix + this.format(message, params);
	}
	
	
	private String format(String format, Object... params) {
		return MessageFormatter.arrayFormat(format, params).getMessage();
	}

	public String toString() {
		return this.getFormattedMessage();
	}
	
}
