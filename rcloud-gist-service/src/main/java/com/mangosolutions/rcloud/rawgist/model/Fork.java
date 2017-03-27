/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
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

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({ "user", "url", "id", "created_at", "updated_at" })
public class Fork implements Serializable
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("user")
    private GistIdentity user;
	
	@JsonProperty("url")
    private String url;
    
	@JsonProperty("id")
    private String id;
    
	@JsonProperty("created_at")
    private DateTime createdAt;
    
	@JsonProperty("updated_at")
    private DateTime updatedAt;
	
	
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    @JsonProperty("user")
    public GistIdentity getUser() {
		return user;
	}

    @JsonProperty("user")
	public void setUser(GistIdentity user) {
		this.user = user;
	}

    @JsonProperty("url")
	public String getUrl() {
		return url;
	}

    @JsonProperty("url")
	public void setUrl(String url) {
		this.url = url;
	}

    @JsonProperty("id")
	public String getId() {
		return id;
	}

    @JsonProperty("id")
	public void setId(String id) {
		this.id = id;
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

}
