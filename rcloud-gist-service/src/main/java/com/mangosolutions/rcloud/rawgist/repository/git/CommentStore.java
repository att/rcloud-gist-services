package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.util.List;

import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;

public interface CommentStore {

	List<GistCommentResponse> load(File store);
	
	List<GistCommentResponse> save(File store, List<GistCommentResponse> comments);
	
}
