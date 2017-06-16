/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GistUsersProperties {

    private Map<String, List<String>> collaborations = new HashMap<>();

    public Map<String, List<String>> getCollaborations() {
        return collaborations;
    }

    public void setCollaborations(Map<String, List<String>> collaborations) {
        this.collaborations = collaborations;
    }
}
