package com.mangosolutions.rcloud.rawgist.model;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({ "url", "version", "user", "change_status", "committed_at" })
public class GistHistory {

	private String url;
	private String version;
	private GistIdentity user;
	private GitChangeStatus changeStatus;

	@JsonProperty("committed_at")
	private DateTime committedAt;

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

	public GistIdentity getUser() {
		return user;
	}

	public void setUser(GistIdentity user) {
		this.user = user;
	}

	public GitChangeStatus getChangeStatus() {
		return changeStatus;
	}

	public void setChangeStatus(GitChangeStatus changeStatus) {
		this.changeStatus = changeStatus;
	}

	@JsonProperty("committed_at")
	public DateTime getCommittedAt() {
		return committedAt;
	}

	@JsonProperty("committed_at")
	public void setCommittedAt(DateTime committedAt) {
		this.committedAt = committedAt;
	}

}
