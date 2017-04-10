package com.mangosolutions.rcloud.rawgist.repository;

public class GistAccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = -217812774457301300L;
	
	private GistError error;

	public GistAccessDeniedException(GistError error, Throwable cause) {
		super(error.getFormattedMessage(), cause);
		this.error = error;
	}

	public GistAccessDeniedException(GistError error) {
		this(error, null);
	}

	public GistError getGistError() {
		return error;
	}
}
