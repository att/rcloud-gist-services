package com.mangosolutions.rcloud.rawgist.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.mangosolutions.rcloud.rawgist.model.FileDefinition;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryService;

@Component
public class GistTestHelper {

	@Autowired
	private HazelcastInstance hazelcastInstance;
	
	@Autowired
	private GistRepositoryService service;
	
	public void emptyHazelcast() {
		Config config = hazelcastInstance.getConfig();
		Map<String, MapConfig> mapConfigs = config.getMapConfigs();
		for(Entry<String, MapConfig> entry: mapConfigs.entrySet()) {
			IMap<Object, Object> map = hazelcastInstance.getMap(entry.getKey());
			map.evictAll();
		}
	}
	
	public void clearGistRepository() throws IOException {
		String tmpdir = System.getProperty("java.io.tmpdir");
		File gistFolder = new File(tmpdir + "/gists");
		FileUtils.forceDelete(gistFolder);
		FileUtils.forceMkdir(gistFolder);
		FileUtils.forceMkdir(new File(gistFolder, ".recycle"));
	}
	
	public String createGist(String user, String description, String fileName, String fileContent) throws Exception {
		GistRequest request = new GistRequest();
		request.setDescription(description);
		request.setPublic(false);

		Map<String, FileDefinition> files = new HashMap<>();
		FileDefinition def = new FileDefinition();
		def.setContent(fileContent);
		files.put(fileName, def);
		request.setFiles(files);
		Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
		UserDetails userDetails = new User(user, "gist_user_pwd", authorities);
		GistResponse response = this.service.createGist(request, userDetails);
		return response.getId();
	}
	
}
