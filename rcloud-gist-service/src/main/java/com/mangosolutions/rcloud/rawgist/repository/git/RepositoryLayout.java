package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.Serializable;

public class RepositoryLayout implements Serializable {
	
	private static final long serialVersionUID = 6380460397113981325L;

	public static final String GIST_META_JSON_FILE = "gist.json";
	
	public static final String GIST_REPO_FOLDER_NAME = "repo";
	
	public static final String COMMENT_REPOSITORY_FOLDER = "comments";
	
	public static final String COMMENTS_FILE = "comments.json";
	
	private File rootFolder;

	private File commentsFolder;
	
	private File commentsFile;
	
	private File gistFolder;
	
	private File metadataFile;
	
	public RepositoryLayout(File root) {
		rootFolder = root;
		commentsFolder = new File(root, COMMENT_REPOSITORY_FOLDER);
		commentsFile = new File(commentsFolder, COMMENTS_FILE);
		gistFolder = new File(root, GIST_REPO_FOLDER_NAME);
		metadataFile = new File(rootFolder, GIST_META_JSON_FILE);
	}

	public File getCommentsFolder() {
		return commentsFolder;
	}

	public void setCommentsFolder(File commentsFolder) {
		this.commentsFolder = commentsFolder;
	}

	public File getCommentsFile() {
		return commentsFile;
	}

	public void setCommentsFile(File commentsFile) {
		this.commentsFile = commentsFile;
	}

	public File getGistFolder() {
		return gistFolder;
	}

	public void setGistFolder(File gistFolder) {
		this.gistFolder = gistFolder;
	}

	public File getMetadataFile() {
		return metadataFile;
	}

	public void setMetadataFile(File metadataFile) {
		this.metadataFile = metadataFile;
	}

	public File getRootFolder() {
		return rootFolder;
	}

	public void setRootFolder(File rootFolder) {
		this.rootFolder = rootFolder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((commentsFile == null) ? 0 : commentsFile.hashCode());
		result = prime * result + ((commentsFolder == null) ? 0 : commentsFolder.hashCode());
		result = prime * result + ((gistFolder == null) ? 0 : gistFolder.hashCode());
		result = prime * result + ((metadataFile == null) ? 0 : metadataFile.hashCode());
		result = prime * result + ((rootFolder == null) ? 0 : rootFolder.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RepositoryLayout other = (RepositoryLayout) obj;
		if (commentsFile == null) {
			if (other.commentsFile != null)
				return false;
		} else if (!commentsFile.equals(other.commentsFile))
			return false;
		if (commentsFolder == null) {
			if (other.commentsFolder != null)
				return false;
		} else if (!commentsFolder.equals(other.commentsFolder))
			return false;
		if (gistFolder == null) {
			if (other.gistFolder != null)
				return false;
		} else if (!gistFolder.equals(other.gistFolder))
			return false;
		if (metadataFile == null) {
			if (other.metadataFile != null)
				return false;
		} else if (!metadataFile.equals(other.metadataFile))
			return false;
		if (rootFolder == null) {
			if (other.rootFolder != null)
				return false;
		} else if (!rootFolder.equals(other.rootFolder))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RepositoryLayout [rootFolder=" + rootFolder + ", commentsFolder=" + commentsFolder + ", commentsFile="
				+ commentsFile + ", gistFolder=" + gistFolder + ", metadataFile=" + metadataFile + "]";
	}
	
	
	
}