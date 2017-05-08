/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "url",
    "forks_url",
    "commits_url",
    "id",
    "description",
    "public",
    "owner",
    "user",
    "files",
    "truncated",
    "fork_of",
    "comments",
    "comments_url",
    "created_at",
    "updated_at",
    "history"
})
public class GistResponse implements Serializable
{

    @JsonProperty("url")
    private String url;
    @JsonProperty("commits_url")
    private String commitsUrl;
    @JsonProperty("forks_url")
    private String forksUrl;
    @JsonProperty("id")
    private String id;
    @JsonProperty("description")
    private String description;
    @JsonProperty("public")
    private Boolean _public;
    @JsonProperty("owner")
    private GistIdentity owner;
    @JsonProperty("user")
    private GistIdentity user;
    @JsonProperty("files")
    private Map<String, FileContent> files = new HashMap<String, FileContent>();
    @JsonProperty("fork_of")
    private Fork forkOf;
    @JsonProperty("truncated")
    private Boolean truncated = false;
    @JsonProperty("comments")
    private Integer comments = 0;
    @JsonProperty("comments_url")
    private String commentsUrl;
    @JsonProperty("created_at")
    private DateTime createdAt;
    @JsonProperty("updated_at")
    private DateTime updatedAt;
    @JsonProperty("history")
    private List<GistHistory> history = new ArrayList<>();

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 5239803736959473806L;

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("forks_url")
    public String getForksUrl() {
		return forksUrl;
	}

    @JsonProperty("forks_url")
	public void setForksUrl(String forksUrl) {
		this.forksUrl = forksUrl;
	}

	@JsonProperty("commits_url")
    public String getCommitsUrl() {
        return commitsUrl;
    }

    @JsonProperty("commits_url")
    public void setCommitsUrl(String commitsUrl) {
        this.commitsUrl = commitsUrl;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("public")
    public Boolean getPublic() {
        return _public;
    }

    @JsonProperty("public")
    public void setPublic(Boolean _public) {
        this._public = _public;
    }

    @JsonProperty("owner")
    public GistIdentity getOwner() {
        return owner;
    }

    @JsonProperty("owner")
    public void setOwner(GistIdentity owner) {
        this.owner = owner;
    }

    @JsonProperty("user")
    public GistIdentity getUser() {
        return user;
    }

    @JsonProperty("user")
    public void setUser(GistIdentity user) {
        this.user = user;
    }

    @JsonProperty("files")
    public Map<String, FileContent> getFiles() {
        return files;
    }

    @JsonProperty("files")
    public void setFiles(Map<String, FileContent> files) {
        this.files = files;
    }

    @JsonProperty("truncated")
    public Boolean getTruncated() {
        return truncated;
    }

    @JsonProperty("truncated")
    public void setTruncated(Boolean truncated) {
        this.truncated = truncated;
    }

    @JsonProperty("comments")
    public Integer getComments() {
        return comments;
    }

    @JsonProperty("comments")
    public void setComments(Integer comments) {
        this.comments = comments;
    }

    @JsonProperty("comments_url")
    public String getCommentsUrl() {
        return commentsUrl;
    }

    @JsonProperty("comments_url")
    public void setCommentsUrl(String commentsUrl) {
        this.commentsUrl = commentsUrl;
    }

    @JsonProperty("created_at")
    public DateTime getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(DateTime createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("updated_at")
    public DateTime getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(DateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    @JsonProperty("history")
    public List<GistHistory> getHistory() {
        return history;
    }

    @JsonProperty("history")
    public void setHistory(List<GistHistory> history) {
        this.history = history;
    }
    
    @JsonProperty("fork_of")
    public Fork getForkOf() {
		return forkOf;
	}

    @JsonProperty("fork_of")
	public void setForkOf(Fork forkOf) {
		this.forkOf = forkOf;
	}

	@JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public void addAdditionalProperties(Map<String, Object> properties) {
        this.additionalProperties.putAll(properties);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_public == null) ? 0 : _public.hashCode());
		result = prime * result + ((additionalProperties == null) ? 0 : additionalProperties.hashCode());
		result = prime * result + ((comments == null) ? 0 : comments.hashCode());
		result = prime * result + ((commentsUrl == null) ? 0 : commentsUrl.hashCode());
		result = prime * result + ((commitsUrl == null) ? 0 : commitsUrl.hashCode());
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((files == null) ? 0 : files.hashCode());
		result = prime * result + ((forkOf == null) ? 0 : forkOf.hashCode());
		result = prime * result + ((forksUrl == null) ? 0 : forksUrl.hashCode());
		result = prime * result + ((history == null) ? 0 : history.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((truncated == null) ? 0 : truncated.hashCode());
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		GistResponse other = (GistResponse) obj;
		if (_public == null) {
			if (other._public != null)
				return false;
		} else if (!_public.equals(other._public))
			return false;
		if (additionalProperties == null) {
			if (other.additionalProperties != null)
				return false;
		} else if (!additionalProperties.equals(other.additionalProperties))
			return false;
		if (comments == null) {
			if (other.comments != null)
				return false;
		} else if (!comments.equals(other.comments))
			return false;
		if (commentsUrl == null) {
			if (other.commentsUrl != null)
				return false;
		} else if (!commentsUrl.equals(other.commentsUrl))
			return false;
		if (commitsUrl == null) {
			if (other.commitsUrl != null)
				return false;
		} else if (!commitsUrl.equals(other.commitsUrl))
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files))
			return false;
		if (forkOf == null) {
			if (other.forkOf != null)
				return false;
		} else if (!forkOf.equals(other.forkOf))
			return false;
		if (forksUrl == null) {
			if (other.forksUrl != null)
				return false;
		} else if (!forksUrl.equals(other.forksUrl))
			return false;
		if (history == null) {
			if (other.history != null)
				return false;
		} else if (!history.equals(other.history))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (truncated == null) {
			if (other.truncated != null)
				return false;
		} else if (!truncated.equals(other.truncated))
			return false;
		if (updatedAt == null) {
			if (other.updatedAt != null)
				return false;
		} else if (!updatedAt.equals(other.updatedAt))
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
		return true;
	}

	@Override
	public String toString() {
		return "GistResponse [url=" + url + ", commitsUrl=" + commitsUrl + ", forksUrl=" + forksUrl + ", id=" + id
				+ ", description=" + description + ", _public=" + _public + ", owner=" + owner + ", user=" + user
				+ ", files=" + files + ", forkOf=" + forkOf + ", truncated=" + truncated + ", comments=" + comments
				+ ", commentsUrl=" + commentsUrl + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ ", history=" + history + ", additionalProperties=" + additionalProperties + "]";
	}

	
}
