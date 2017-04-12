package com.mangosolutions.rcloud.rawgist.repository.git;

import java.util.List;

import com.mangosolutions.rcloud.rawgist.model.GistHistory;

public interface HistoryStore {

	public List<GistHistory> load(String commitId);
	
	public List<GistHistory> save(String commitId, List<GistHistory> history);

}
