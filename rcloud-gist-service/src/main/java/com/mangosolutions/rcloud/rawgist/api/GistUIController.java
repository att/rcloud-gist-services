/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.api;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mangosolutions.rcloud.rawgist.model.Fork;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.model.GistIdentity;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;
import com.mangosolutions.rcloud.rawgist.repository.git.CollaborationDataStore;
import com.mangosolutions.rcloud.sessionkeyauth.AnonymousUser;

@Controller()
@RequestMapping(value = "/gists")
public class GistUIController {

    @Autowired
    private GistRepositoryService repository;

    @Autowired
    private ControllerUrlResolver resolver;
    
    @Autowired
    private GistRestController gistRestController;
    
    @Autowired
    private GistCommentRestController gistCommentRestController;

    @Autowired
    private CollaborationDataStore collaborationDataStore;

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
    
//    private GistResponse getGist(String gistId) {
//        User user = new AnonymousUser();
//        GistResponse response = repository.getGist(gistId, user);
//        decorateGistResponse(response, user);
//        return response;
//    }
    
//    private List<Fork> getForks(String gistId) {
//        User user = new AnonymousUser();
//        List<Fork> forks = repository.getForks(gistId, user);
//        decorateForksResponse(forks, user);
//        return forks;
//    }
//    
//    private List
    

//    @RequestMapping(value = "/gists/{gistId}/{commitId}", method = RequestMethod.GET)
//    public GistResponse getGistAtCommit(@PathVariable("gistId") String gistId,
//            @PathVariable("commitId") String commitId) {
//        User user = new AnonymousUser();
//        GistResponse response = repository.getGist(gistId, commitId, user);
//        decorateGistResponse(response, user);
//        return response;
//    }
//
//    @RequestMapping(value = "/gists/{gistId}/forks", method = RequestMethod.GET)
//    @ResponseStatus(HttpStatus.OK)
//    public List<Fork> getForks(@PathVariable("gistId") String gistId) {
//        User user = new AnonymousUser();
//        List<Fork> forks = repository.getForks(gistId, user);
//        decorateForksResponse(forks, user);
//        return forks;
//    }


    //TODO need to move this out into a common class
    private void decorateGistResponse(GistResponse gistResponse, User activeUser) {
        if (gistResponse != null) {
            gistResponse.setUrl(resolver.getGistUrl(gistResponse.getId(), activeUser));
            gistResponse.setCommentsUrl(resolver.getCommentsUrl(gistResponse.getId(), activeUser));
            gistResponse.setForksUrl(resolver.getForksUrl(gistResponse.getId(), activeUser));
            if (gistResponse.getForkOf() != null) {
                Fork forkOf = gistResponse.getForkOf();
                String url = resolver.getGistUrl(forkOf.getId(), activeUser);
                forkOf.setUrl(url);
            }
            this.decorateCollaborators(gistResponse);
        }
    }

    private void decorateForksResponse(List<Fork> forks, User activeUser) {
        for (Fork fork : forks) {
            String forkUrl = resolver.getGistUrl(fork.getId(), activeUser);
            fork.setUrl(forkUrl);
        }
    }

    private void decorateCollaborators(GistResponse gistResponse) {
        if (gistResponse.getOwner() != null && StringUtils.isNotBlank(gistResponse.getOwner().getLogin())) {
            Collection<String> collaboratorNames = collaborationDataStore
                    .getCollaborators(gistResponse.getOwner().getLogin());
            Collection<GistIdentity> collaboratorIdentities = new LinkedList<>();
            for (String collaboratorName : collaboratorNames) {
                GistIdentity collaboratorIdentity = new GistIdentity();
                collaboratorIdentity.setLogin(collaboratorName);
                collaboratorIdentities.add(collaboratorIdentity);
            }
            gistResponse.setCollaborators(collaboratorIdentities);
        }
    }

}
