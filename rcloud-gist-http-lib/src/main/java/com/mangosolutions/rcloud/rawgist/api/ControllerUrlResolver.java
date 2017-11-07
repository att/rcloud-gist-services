/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.api;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
public class ControllerUrlResolver {

    public String getGistUrl(String gistId, User activeUser) {
        String url = null;
        if (gistId != null) {
            url = linkTo(methodOn(GistRestController.class).getGist(gistId, activeUser)).withSelfRel().getHref();
        }
        return url;
    }

    public String getCommentsUrl(String gistId, User activeUser) {
        String url = null;
        if (gistId != null) {
            url = linkTo(methodOn(GistCommentRestController.class).getComments(gistId, activeUser)).withSelfRel()
                    .getHref();
        }
        return url;
    }

    public String getCommentUrl(String gistId, Long commentId, User activeUser) {
        String url = null;
        if (gistId != null && commentId != null) {
            url = linkTo(methodOn(GistCommentRestController.class).getComment(gistId, commentId, activeUser))
                    .withSelfRel().getHref();
        }
        return url;
    }

    public String getForksUrl(String gistId, User activeUser) {
        String url = null;
        if (gistId != null) {
            url = linkTo(methodOn(GistRestController.class).forkGist(gistId, activeUser)).withSelfRel().getHref();
        }
        return url;
    }

}
