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

		for (Map.Entry<String, String> entry : zuulRequestHeaders.entrySet()) {
			if (StringUtils.isNotBlank(protocol) && StringUtils.isNotBlank(host)) {
				break;
			}
			if ("x-forwarded-host".equals(entry.getKey())) {
				host = entry.getValue();
				if (host.contains(":")) {
					String[] hp = host.split(":");
					host = hp[0];
					try {
						port = Integer.valueOf(hp[1]);
					} catch (NumberFormatException e) {
						// ignore
					}
				}
			}
			if ("x-forwarded-proto".equals(entry.getKey())) {
				protocol = entry.getValue();
			}
			if ("x-forwarded-port".equals(entry.getKey())) {
				port = Integer.valueOf(entry.getValue());
			}
		}

		try {
			URL url = new URL(protocol, host, port, "");
			return url.toString();
		} catch (MalformedURLException e) {
			logger.warn("Could not convert parameters to a URL protocol: {}, host: {}, port: {}", protocol, host, port);
		}

		return null;
	}

}
