package com.mangosolutions.rcloud.rawgist.model;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "url", "version", "user", "change_status", "commited_at" })
public class GistHistory {

	private String url;
	private String version;
	private GistOwner user;
	private GitChangeStatus changeStatus;

	@JsonProperty("created_at")
	private DateTime commitedAt;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public GistOwner getUser() {
		return user;
	}

	public void setUser(GistOwner user) {
		this.user = user;
	}

	public GitChangeStatus getChangeStatus() {
		return changeStatus;
	}

	public void setChangeStatus(GitChangeStatus changeStatus) {
		this.changeStatus = changeStatus;
	}

	@JsonProperty("created_at")
	public DateTime getCommitedAt() {
		return commitedAt;
	}

	@JsonProperty("created_at")
	public void setCommitedAt(DateTime commitedAt) {
		this.commitedAt = commitedAt;
	}

}
