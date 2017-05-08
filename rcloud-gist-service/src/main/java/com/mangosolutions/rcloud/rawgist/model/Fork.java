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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "user", "url", "id", "created_at", "updated_at" })
public class Fork implements Serializable
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("url")
    private String url;
    
	@JsonProperty("id")
    private String id;
    
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

	@JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((additionalProperties == null) ? 0 : additionalProperties.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		Fork other = (Fork) obj;
		if (additionalProperties == null) {
			if (other.additionalProperties != null)
				return false;
		} else if (!additionalProperties.equals(other.additionalProperties))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Fork [url=" + url + ", id=" + id + ", additionalProperties=" + additionalProperties + "]";
	}

}
