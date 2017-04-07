package com.mangosolutions.rcloud.rawgist.repository.git;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.mangosolutions.rcloud.rawgist.model.GistHistory;

@Component
public class GistHistoryStore implements HistoryStore {

	@Override
	@Cacheable(value = "historystore", key = "#gistId")
	public List<GistHistory> load(String gistId) {
		return new ArrayList<GistHistory>();
	}

	@Override
	@CachePut(cacheNames = "historystore", key = "#gistId")
	public List<GistHistory> save(String gistId, List<GistHistory> history) {
		return history;
	}

}
