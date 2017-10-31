/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mangosolutions.rcloud.rawgist.model.Fork;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "owner", "description", "public", "created_at", "updated_at", "fork_of", "forks" })
public class GistMetadata implements Serializable {

    private final static long serialVersionUID = -7352290872081419828L;

    @JsonProperty("id")
    private String id;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("description")
    private String description;

    @JsonProperty("public")
    private boolean _public = true;

    @JsonProperty("created_at")
    private DateTime createdAt;

    @JsonProperty("updated_at")
    private DateTime updatedAt;

    @JsonProperty("fork_of")
    private Fork forkOf = null;

    @JsonProperty("forks")
    private List<Fork> forks = new ArrayList<>();

    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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
    public boolean isPublic() {
        return _public;
    }

    @JsonProperty("public")
    public void setPublic(boolean _public) {
        this._public = _public;
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

    @JsonProperty("forks")
    public List<Fork> getForks() {
        return forks;
    }

    @JsonProperty("forks")
    public void setForks(List<Fork> forks) {
        this.forks = forks;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public void addOrUpdateFork(Fork fork) {

        for (int i = 0; i < this.forks.size(); i++) {
            Fork existingFork = this.forks.get(i);
            if (existingFork.getId().equals(fork.getId())) {
                this.forks.set(i, fork);
                return;
            }
        }
        this.forks.add(fork);
    }

    @JsonProperty("fork_of")
    public void setForkOf(Fork fork) {
        this.forkOf = fork;
    }

    @JsonProperty("fork_of")
    public Fork getForkOf() {
        return this.forkOf;
    }

}
