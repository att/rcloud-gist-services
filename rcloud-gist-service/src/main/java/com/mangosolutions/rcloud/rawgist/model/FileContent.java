
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"filename",
    "size",
    "raw_url",
    "type",
    "language",
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
    private String type;
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
        this.type = type;
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

}
