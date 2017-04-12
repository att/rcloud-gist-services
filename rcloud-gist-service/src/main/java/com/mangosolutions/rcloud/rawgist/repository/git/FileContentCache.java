package com.mangosolutions.rcloud.rawgist.repository.git;

import com.mangosolutions.rcloud.rawgist.model.FileContent;

public interface FileContentCache {
	
	FileContent load(String contentId);
	
	FileContent save(String contentId, FileContent content);

}
