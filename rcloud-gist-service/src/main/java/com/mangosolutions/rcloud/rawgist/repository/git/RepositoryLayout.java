package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.Serializable;

public class RepositoryLayout implements Serializable {
	
	private static final long serialVersionUID = 6380460397113981325L;

	public static final String GIST_META_FILE = "gist.json";
	
	public static final String GIST_BARE_REPOSITORY_FOLDER = "repo";
	
	public static final String GIST_WORKING_REPOSITORY_FOLDER = ".work";
	
	public static final String COMMENT_REPOSITORY_FOLDER = "comments";
	
	public static final String COMMENTS_FILE = "comments.json";
	
	private File rootFolder;

	private File commentsFolder;
	
	private File commentsFile;
	
	private File bareFolder;
	
	private File metadataFile;
	
	private File workingFolder;
	
	public RepositoryLayout(File root) {
		rootFolder = root;
		commentsFolder = new File(root, COMMENT_REPOSITORY_FOLDER);
		commentsFile = new File(commentsFolder, COMMENTS_FILE);
		bareFolder = new File(root, GIST_BARE_REPOSITORY_FOLDER);
		metadataFile = new File(rootFolder, GIST_META_FILE);
		workingFolder = new File(rootFolder, GIST_WORKING_REPOSITORY_FOLDER);
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

	public File getBareFolder() {
		return bareFolder;
	}

	public void setBareFolder(File bareFolder) {
		this.bareFolder = bareFolder;
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

	public File getWorkingFolder() {
		return workingFolder;
	}

	public void setWorkingFolder(File workingFolder) {
		this.workingFolder = workingFolder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((commentsFile == null) ? 0 : commentsFile.hashCode());
		result = prime * result + ((commentsFolder == null) ? 0 : commentsFolder.hashCode());
		result = prime * result + ((bareFolder == null) ? 0 : bareFolder.hashCode());
		result = prime * result + ((metadataFile == null) ? 0 : metadataFile.hashCode());
		result = prime * result + ((rootFolder == null) ? 0 : rootFolder.hashCode());
		result = prime * result + ((workingFolder == null) ? 0 : workingFolder.hashCode());
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
		if (bareFolder == null) {
			if (other.bareFolder != null)
				return false;
		} else if (!bareFolder.equals(other.bareFolder))
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
		if (workingFolder == null) {
			if (other.workingFolder != null)
				return false;
		} else if (!workingFolder.equals(other.workingFolder))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RepositoryLayout [rootFolder=" + rootFolder + ", commentsFolder=" + commentsFolder + ", commentsFile="
				+ commentsFile + ", gistFolder=" + bareFolder + ", metadataFile=" + metadataFile + ", workingFolder="
				+ workingFolder + "]";
	}
	
	
	
}