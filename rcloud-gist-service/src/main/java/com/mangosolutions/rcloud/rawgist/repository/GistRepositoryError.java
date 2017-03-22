package com.mangosolutions.rcloud.rawgist.repository;

public class GistRepositoryError extends RuntimeException {

	private static final long serialVersionUID = 7221509801637802584L;
	
	private GistError error;
	
	public GistRepositoryError(GistError error, Throwable cause) {
		super(error.getFormattedMessage(), cause);
		this.error = error;
	}

	public GistRepositoryError(GistError error) {
		this(error, null);
	}
	
	public GistError getGistError() {
		return error;
	}
	
}
