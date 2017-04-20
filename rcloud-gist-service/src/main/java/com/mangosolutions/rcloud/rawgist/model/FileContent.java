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
@JsonPropertyOrder({
	"filename",
	"type",
	"language",
	"raw_url",
    "size",
    "truncated",
    "content"
})
public class FileContent implements Serializable
{

    @JsonProperty("size")
    private Long size;
    @JsonProperty("raw_url")
    private String rawUrl;
    @JsonProperty("type")
    private String type = "text/plain";
    @JsonProperty("language")
    private String language;
    @JsonProperty("truncated")
    private Boolean truncated;
    @JsonProperty("content")
    private String content;
    @JsonProperty("filename")
    private String filename;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 2081800888788458092L;

    @JsonProperty("filename")
    public String getFilename() {
		return filename;
	}

    @JsonProperty("filename")
	public void setFilename(String filename) {
		this.filename = filename;
	}

	@JsonProperty("size")
    public Long getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(Long size) {
        this.size = size;
    }

    @JsonProperty("raw_url")
    public String getRawUrl() {
        return rawUrl;
    }

    @JsonProperty("raw_url")
    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        //this.type = type;
    }

    @JsonProperty("language")
    public String getLanguage() {
        return language;
    }

    @JsonProperty("language")
    public void setLanguage(String language) {
        this.language = language;
    }

    @JsonProperty("truncated")
    public Boolean getTruncated() {
        return truncated;
    }

    @JsonProperty("truncated")
    public void setTruncated(Boolean truncated) {
        this.truncated = truncated;
    }

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
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
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((rawUrl == null) ? 0 : rawUrl.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		result = prime * result + ((truncated == null) ? 0 : truncated.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		FileContent other = (FileContent) obj;
		if (additionalProperties == null) {
			if (other.additionalProperties != null)
				return false;
		} else if (!additionalProperties.equals(other.additionalProperties))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (rawUrl == null) {
			if (other.rawUrl != null)
				return false;
		} else if (!rawUrl.equals(other.rawUrl))
			return false;
		if (size == null) {
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		if (truncated == null) {
			if (other.truncated != null)
				return false;
		} else if (!truncated.equals(other.truncated))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FileContent [size=" + size + ", rawUrl=" + rawUrl + ", type=" + type + ", language=" + language
				+ ", truncated=" + truncated + ", content=" + content + ", filename=" + filename
				+ ", additionalProperties=" + additionalProperties + "]";
	}

}
