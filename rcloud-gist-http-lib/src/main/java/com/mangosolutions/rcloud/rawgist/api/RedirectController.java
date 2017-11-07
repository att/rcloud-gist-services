/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("${gists.route.redirect.from}")
public class RedirectController {

    @Value("${gists.route.redirect.to}")
    private String redirectToUrl;

    @Value("${gists.route.redirect.copyparams:true}")
    private boolean propagateParams = true;

    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public RedirectView performRedirect(RedirectAttributes attributes) {
        RedirectView redirectView = new RedirectView();
        if (StringUtils.isEmpty(redirectToUrl)) {
            redirectView.setStatusCode(HttpStatus.NOT_FOUND);
        } else {
            redirectView.setUrl(redirectToUrl);
            redirectView.setPropagateQueryParams(propagateParams);

        }
        return redirectView;
    }

}
