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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({ "description", "public", "files" })
public class GistRequest implements Serializable {

	@JsonProperty("description")
	private String description;

	@JsonProperty("public")
	private Boolean _public;

	@JsonProperty("files")
	private Map<String, FileDefinition> files;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();
	private final static long serialVersionUID = -7352290872081419828L;

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

	@JsonProperty("files")
	public Map<String, FileDefinition> getFiles() {
		return files;
	}

	@JsonProperty("files")
	public void setFiles(Map<String, FileDefinition> files) {
		this.files = files;
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
