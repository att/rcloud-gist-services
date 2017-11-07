/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.git;

import java.util.List;

import com.mangosolutions.rcloud.rawgist.model.GistHistory;

public interface HistoryCache {

    public List<GistHistory> load(String commitId);

    public List<GistHistory> save(String commitId, List<GistHistory> history);

}
