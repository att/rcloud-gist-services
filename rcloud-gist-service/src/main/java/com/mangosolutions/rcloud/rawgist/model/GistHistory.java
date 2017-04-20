/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.model;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({ "url", "version", "user", "change_status", "committed_at" })
public class GistHistory implements Serializable {

	private static final long serialVersionUID = 7346102204021167017L;
	
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((changeStatus == null) ? 0 : changeStatus.hashCode());
		result = prime * result + ((committedAt == null) ? 0 : committedAt.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		GistHistory other = (GistHistory) obj;
		if (changeStatus == null) {
			if (other.changeStatus != null)
				return false;
		} else if (!changeStatus.equals(other.changeStatus))
			return false;
		if (committedAt == null) {
			if (other.committedAt != null)
				return false;
		} else if (!committedAt.equals(other.committedAt))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GistHistory [url=" + url + ", version=" + version + ", user=" + user + ", changeStatus=" + changeStatus
				+ ", committedAt=" + committedAt + "]";
	}

}
