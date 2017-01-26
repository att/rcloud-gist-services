package com.mangosolutions.rcloud.gists.filters;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;
import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class JsonContentUrlRewritingFilter extends ZuulFilter {

	private static final Logger logger = LoggerFactory.getLogger(JsonContentUrlRewritingFilter.class);

	private static final MimeType[] JSON_MIME_TYPES = {MimeTypeUtils.APPLICATION_JSON, MimeTypeUtils.parseMimeType("application/*+json")}; 
	
	private int order = 100;
	
	public JsonContentUrlRewritingFilter(int order) {
		this.order = order;
	}
	
	@Override
	public String filterType() {
		return "post";
	}

	@Override
	public int filterOrder() {
		return order;
	}

	@Override
	public boolean shouldFilter() {
		return isJsonContent(RequestContext.getCurrentContext());
	}

	private boolean isJsonContent(RequestContext currentContext) {
		if (isJsonResponseContentType(currentContext.getZuulResponseHeaders())) {
			return true;
		}
		return false;
	}

	private boolean isJsonResponseContentType(List<Pair<String, String>> zuulResponseHeaders) {
		for (Pair<String, String> header : zuulResponseHeaders) {
			String name = header.first();
			if (StringUtils.equalsIgnoreCase("Content-Type", name)) {
				String value = header.second();
				try {
					MimeType mimeType = MimeTypeUtils.parseMimeType(value);
					for(MimeType jsonMimeType: JSON_MIME_TYPES) {
						if(jsonMimeType.isCompatibleWith(mimeType)) {
							return true;
						}
						
					}
				} catch (InvalidMimeTypeException e) {
					logger.warn("Could not parse {} as a valid mimetype", value);
				}
			}
		}
		return false;
	}

	@Override
	public Object run() {
		logger.debug("Running");
		RequestContext context = RequestContext.getCurrentContext();
		ZuulResponseContent zuulContent = new ZuulResponseContent(context);
		
		String content = zuulContent.getContent();
		if (StringUtils.isNotEmpty(content)) {
			content = replaceUrls(context, content);
			zuulContent.setContent(content);
		}
		return null;
	}

	private String replaceUrls(RequestContext context, String body) {

		JsonValue jsonRoot = Json.parse(body);
		Queue<JsonValue> jsonValues = new LinkedList<>();
		jsonValues.add(jsonRoot);
		JsonValue value = null;
		ZuulRequestUrlResolver resolver = new ZuulRequestUrlResolver();
        String zuulUrl = resolver.getZuulServiceUrl(context);
        String targetUrl = resolver.getProxiedServiceUrl(context);
        
		while ((value = jsonValues.poll()) != null) {
			if (value.isArray()) {
				JsonArray jsonArray = value.asArray();
				for(int i = 0; i < jsonArray.size(); i++) {
					JsonValue arrayValue = jsonArray.get(i);
					if(arrayValue.isString()) {
						String arrayValueString = arrayValue.asString();
						if(arrayValueString.startsWith(targetUrl)) {
							arrayValueString = arrayValueString.replace(targetUrl, zuulUrl);
							jsonArray.set(i, arrayValueString);
						}
					} else {
						jsonValues.add(arrayValue);
					}
				}
			} else if (value.isObject()) {
				for (Member member : value.asObject()) {
					String name = member.getName();
					JsonValue memberValue = member.getValue();
					if (memberValue.isString()) {
						String memberString = member.getValue().asString();
						if(memberString.startsWith(targetUrl)) {
							memberString = memberString.replace(targetUrl, zuulUrl);
							value.asObject().set(name, memberString);
						}
						
						
					} else {
						jsonValues.add(memberValue);
					}
				}
			}
		}
		
		return jsonRoot.toString(WriterConfig.PRETTY_PRINT);

	}

}
