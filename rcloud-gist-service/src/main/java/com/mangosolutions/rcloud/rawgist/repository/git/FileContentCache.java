package com.mangosolutions.rcloud.rawgist.repository.git;

import com.mangosolutions.rcloud.rawgist.model.FileContent;

public interface FileContentCache {
	
	FileContent load(String contentId, String path);
	
	FileContent save(String contentId, String path, FileContent content);

}
