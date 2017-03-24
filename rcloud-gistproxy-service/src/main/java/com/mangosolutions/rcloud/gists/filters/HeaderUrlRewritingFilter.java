/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.gists.filters;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.filter;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public final class HeaderUrlRewritingFilter extends ZuulFilter {

	private static final Logger log = LoggerFactory.getLogger(HeaderUrlRewritingFilter.class);

	public static final ImmutableSet<String> DEFAULT_WHITELIST = ImmutableSet.of("Link", "Location");

	private final ImmutableSet<String> whitelist = DEFAULT_WHITELIST;

	private int order = 100;


	public HeaderUrlRewritingFilter(int order) {
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
		return containsHeaders(RequestContext.getCurrentContext());
	}

	private static boolean containsHeaders(final RequestContext context) {
		assert context != null;
		return !context.getZuulResponseHeaders().isEmpty();
	}

	@Override
	public Object run() {
		try {
			rewriteHeaders(RequestContext.getCurrentContext(), this.whitelist);
		} catch (final Exception e) {
			Throwables.propagate(e);
		}
		return null;
	}

	public ImmutableSet<String> getWhitelist() {
		return this.whitelist;
	}

	private static void rewriteHeaders(final RequestContext context, final Collection<String> whitelist) {
        assert context != null;
        ZuulRequestUrlResolver resolver = new ZuulRequestUrlResolver();
        String zuulUrl = resolver.getZuulServiceUrl(context);
        String targetUrl = resolver.getProxiedServiceUrl(context);
        if(StringUtils.isNotBlank(zuulUrl) & StringUtils.isNotBlank(targetUrl)) {
	        filterHeaders(context, whitelist, zuulUrl, targetUrl);
        }
    }

	private static void filterHeaders(final RequestContext context, final Collection<String> whitelist,
			String originUrl, String targetUrl) {
		for (final Pair<String, String> header : context.getZuulResponseHeaders()) {
		    if (caseInsensitiveContains(whitelist, header.first())) {
		        header.setSecond(header.second().replace(targetUrl, originUrl));
		        log.debug("Rewrote header: {} to {}", header.first(), header.second() );
		    }
		}
	}



	private static boolean caseInsensitiveContains(final Collection<String> collection, final String value) {
		return !filter(collection, new CaseInsensitiveEqualityPredicate(value)).isEmpty();
	}

	private static class CaseInsensitiveEqualityPredicate implements Predicate<String> {

		private final String referenceValue;

		public CaseInsensitiveEqualityPredicate(final String referenceValue) {
			this.referenceValue = checkNotNull(referenceValue);
		}

		@Override
		public boolean apply(final String input) {
			return this.referenceValue.equalsIgnoreCase(input);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof CaseInsensitiveEqualityPredicate) {
				final @SuppressWarnings("unchecked") CaseInsensitiveEqualityPredicate that = (CaseInsensitiveEqualityPredicate) obj;
				return Objects.equal(this.referenceValue, that.referenceValue);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.referenceValue);
		}
	}
}
