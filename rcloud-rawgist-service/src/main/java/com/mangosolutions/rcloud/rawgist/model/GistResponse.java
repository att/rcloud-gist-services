
package com.mangosolutions.rcloud.rawgist.model;

import java.io.Serializable;
import java.util.HashMap;
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
    "commits_url",
    "id",
    "description",
    "public",
    "owner",
    "user",
    "files",
    "truncated",
    "comments",
    "comments_url",
    "created_at",
    "updated_at"
})
public class GistResponse implements Serializable
{

    @JsonProperty("url")
    private String url;
    @JsonProperty("commits_url")
    private String commitsUrl;
    @JsonProperty("id")
    private String id;
    @JsonProperty("description")
    private String description;
    @JsonProperty("public")
    private Boolean _public;
    @JsonProperty("owner")
    private Owner owner;
    @JsonProperty("user")
    private Object user;
    @JsonProperty("files")
    private Map<String, FileContent> files = new HashMap<String, FileContent>();
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
    public Owner getOwner() {
        return owner;
    }

    @JsonProperty("owner")
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @JsonProperty("user")
    public Object getUser() {
        return user;
    }

    @JsonProperty("user")
    public void setUser(Object user) {
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

}
