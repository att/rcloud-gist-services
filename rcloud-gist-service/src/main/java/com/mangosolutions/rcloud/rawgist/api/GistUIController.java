/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mangosolutions.rcloud.rawgist.model.Fork;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.sessionkeyauth.AnonymousUser;

@Controller()
@RequestMapping(value = "/gists")
public class GistUIController {

    @Autowired
    private GistRestController gistRestController;
    
    @Autowired
    private GistCommentRestController gistCommentRestController;

    @RequestMapping(value = "/{gistId}", method = RequestMethod.GET)
    public String getGist(@PathVariable("gistId") String gistId, Model model) {
        User user = new AnonymousUser();
        GistResponse gist = gistRestController.getGist(gistId, user);
        model.addAttribute("gist", gist);
        List<Fork> forks = gistRestController.getForks(gistId, user);
        model.addAttribute("forks", forks);
        List<GistCommentResponse> comments = gistCommentRestController.getComments(gistId, user);
        model.addAttribute("comments", comments);
        return "gist";
    }
    
    @RequestMapping(value = "/{gistId}/{commitId}", method = RequestMethod.GET)
    public String getGistAtCommit(@PathVariable("gistId") String gistId,
            @PathVariable("commitId") String commitId, Model model) {
        User user = new AnonymousUser();
        GistResponse gist = gistRestController.getGistAtCommit(gistId, commitId, user);
        model.addAttribute("gist", gist);
        List<Fork> forks = gistRestController.getForks(gistId, user);
        model.addAttribute("forks", forks);
        List<GistCommentResponse> comments = gistCommentRestController.getComments(gistId, user);
        model.addAttribute("comments", comments);
        return "gist";
    }
    
//    @RequestMapping(value = "/gists/{gistId}/forks", method = RequestMethod.GET)
//    @ResponseStatus(HttpStatus.OK)
//    public List<Fork> getForks(@PathVariable("gistId") String gistId) {
//        User user = new AnonymousUser();
//        List<Fork> forks = repository.getForks(gistId, user);
//        decorateForksResponse(forks, user);
//        return forks;
//    }

}
