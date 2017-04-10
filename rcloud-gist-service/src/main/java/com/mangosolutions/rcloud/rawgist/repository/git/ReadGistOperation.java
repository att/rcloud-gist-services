package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.activation.MimetypesFileTypeMap;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.OpenOp;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.mangosolutions.rcloud.rawgist.model.FileContent;
import com.mangosolutions.rcloud.rawgist.model.GistHistory;
import com.mangosolutions.rcloud.rawgist.model.GistIdentity;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;
import com.mangosolutions.rcloud.rawgist.repository.GistCommentRepository;
import com.mangosolutions.rcloud.rawgist.repository.GistError;
import com.mangosolutions.rcloud.rawgist.repository.GistErrorCode;
import com.mangosolutions.rcloud.rawgist.repository.GistRepositoryError;

public class ReadGistOperation implements Callable<GistResponse> {

	private static final Logger logger = LoggerFactory.getLogger(ReadGistOperation.class);

	private UserDetails user;
	
	private GistCommentRepository commentRepository;
	
	private RepositoryLayout layout;
	
	private MetadataStore metadataStore;
	
	private HistoryStore historyStore;
	
	private String gistId;
	
	@Override
	public GistResponse call() {
		OpenOp openOp = new OpenOp();
		openOp.setDir(layout.getGistFolder());
		try (Grgit git = openOp.call()) {
			return this.readGist(git);
		}
	}

	protected GistResponse readGist(Grgit git) {
		GistResponse response = new GistResponse();

		Map<String, FileContent> files = new LinkedHashMap<String, FileContent>();
		Collection<File> fileList = FileUtils.listFiles(layout.getGistFolder(), FileFileFilter.FILE, FileFilterUtils
				.and(TrueFileFilter.INSTANCE, FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(".git"))));
		for (File file : fileList) {
			FileContent content = readContent(file);
			files.put(file.getName(), content);
		}
		response.setFiles(files);
		response.setComments(this.commentRepository.getComments(user).size());
		List<GistHistory> history = getHistory(git);
		response.setHistory(history);
		applyMetadata(response);
		return response;
	}

	private FileContent readContent(File file) {
		FileContent content = new FileContent();
		try {
			content.setFilename(file.getName());
			content.setContent(FileUtils.readFileToString(file, CharEncoding.UTF_8));
			content.setSize(file.length());
			content.setTruncated(false);
			// TODO the language
			String language = FilenameUtils.getExtension(file.getName());
			if (!GitGistRepository.B64_BINARY_EXTENSION.equals(language) && !StringUtils.isEmpty(language)) {
				content.setLanguage(language);
			}
			// TODO mimetype
			content.setType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file));
		} catch (IOException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_READABLE,
					"Could not read content of {} for gist {}", file.getName(), gistId);
			logger.error(error.getFormattedMessage() + " with path {}", file);
			throw new GistRepositoryError(error, e);
		}
		return content;
	}

	private void applyMetadata(GistResponse response) {

		GistMetadata metadata = this.getMetadata();
		response.setId(metadata.getId());
		response.setDescription(metadata.getDescription());
		response.setDescription(metadata.getDescription());
		response.setCreatedAt(metadata.getCreatedAt());
		response.setUpdatedAt(metadata.getUpdatedAt());
		response.setPublic(metadata.isPublic());
		if (!StringUtils.isEmpty(metadata.getOwner())) {
			GistIdentity owner = new GistIdentity();
			owner.setLogin(metadata.getOwner());
			response.setOwner(owner);
			response.setUser(owner);
		}
		response.addAdditionalProperties(metadata.getAdditionalProperties());
	}
	
	public GistMetadata getMetadata() {
		return metadataStore.load(layout.getMetadataFile());
	}

	private List<GistHistory> getHistory(Grgit git) {
		String gistId = this.gistId;
		List<GistHistory> history = historyStore.load(gistId);
		GitHistoryOperation historyOperation = new GitHistoryOperation();
		historyOperation.setRepository(git.getRepository());
		historyOperation.setknownHistory(history);
		history = historyOperation.call();
		historyStore.save(gistId, history);
		return history;
	}
	
	public UserDetails getUser() {
		return user;
	}

	public void setUser(UserDetails user) {
		this.user = user;
	}

	public GistCommentRepository getCommentRepository() {
		return commentRepository;
	}

	public void setCommentRepository(GistCommentRepository commentRepository) {
		this.commentRepository = commentRepository;
	}

	public RepositoryLayout getLayout() {
		return layout;
	}

	public void setLayout(RepositoryLayout layout) {
		this.layout = layout;
	}

	public MetadataStore getMetadataStore() {
		return metadataStore;
	}

	public void setMetadataStore(MetadataStore metadataStore) {
		this.metadataStore = metadataStore;
	}

	public HistoryStore getHistoryStore() {
		return historyStore;
	}

	public void setHistoryStore(HistoryStore historyStore) {
		this.historyStore = historyStore;
	}

	public String getGistId() {
		return gistId;
	}

	public void setGistId(String gistId) {
		this.gistId = gistId;
	}



	
	
}
