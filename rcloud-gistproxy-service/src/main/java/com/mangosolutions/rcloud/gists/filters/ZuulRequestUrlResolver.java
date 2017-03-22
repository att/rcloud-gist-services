/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.gists.filters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.zuul.context.RequestContext;

public class ZuulRequestUrlResolver {

	private static final Logger logger = LoggerFactory.getLogger(ZuulRequestUrlResolver.class);

	public String getProxiedServiceUrl(RequestContext context) {
		return context.getRouteHost().toString();
	}

	public String getZuulServiceUrl(RequestContext context) {
		Map<String, String> zuulRequestHeaders = context.getZuulRequestHeaders();

		String protocol = null;
		String host = null;
		int port = -1;
		String path = "";

		for (Map.Entry<String, String> entry : zuulRequestHeaders.entrySet()) {
			if ("x-forwarded-host".equals(entry.getKey())) {
				host = entry.getValue();
				if (host.contains(":")) {
					String[] hp = host.split(":");
					host = hp[0];
					if (port == -1) {
						try {
							port = Integer.valueOf(hp[1]);
						} catch (NumberFormatException e) {
							// ignore
						}
					}
				}
			}
			if ("x-forwarded-proto".equals(entry.getKey())) {
				protocol = entry.getValue();
			}
			if ("x-forwarded-port".equals(entry.getKey())) {
				try {
					port = Integer.valueOf(entry.getValue());
				} catch (NumberFormatException e) {
					// ignore
				}
			}
			if ("x-forwarded-prefix".equals(entry.getKey())) {
				String value = entry.getValue();
				if (StringUtils.isNotBlank(value)) {
					path = value;
				}
			}
		}

		try {
			URL url = new URL(protocol, host, port, path);
			return url.toString();
		} catch (MalformedURLException e) {
			logger.warn("Could not convert parameters to a URL protocol: {}, host: {}, port: {}", protocol, host, port);
		}

		return null;
	}

}
