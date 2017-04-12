package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.activation.MimetypesFileTypeMap;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.OpenOp;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
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
	
	public static final String REF_HEAD_MASTER = "refs/heads/master";

	private UserDetails user;
	
	private GistCommentRepository commentRepository;
	
	private RepositoryLayout layout;
	
	private MetadataStore metadataStore;
	
	private HistoryCache historyStore;
	
	private FileContentCache fileContentCache = new FileContentCache() {

		@Override
		public FileContent load(String contentId) {
			return null;
		}

		@Override
		public FileContent save(String contentId, FileContent content) {
			return content;
		}
		
	};
	
	private String gistId;
	
	private String commitId = null;
	
	@Override
	public GistResponse call() {
		OpenOp openOp = new OpenOp();
		openOp.setDir(layout.getGistFolder());
		try (Grgit git = openOp.call()) {
			
			return this.readGist(git);
		}
	}

	protected GistResponse readGist(Grgit git) {
		try {
			Repository repository = git.getRepository().getJgit().getRepository();
			RevCommit revCommit = resolveCommit(repository);
			GistResponse response = new GistResponse();
	
			Map<String, FileContent> fileContent;
			fileContent = getFileContent(repository, revCommit);
			response.setFiles(fileContent);
			response.setComments(this.commentRepository.getComments(user).size());
			List<GistHistory> history = getHistory(git, revCommit);
			response.setHistory(history);
			applyMetadata(response);
			return response;
		} catch (IOException e) {
			GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_READABLE,
					"Could not read content of gist {}", gistId);
			logger.error(error.getFormattedMessage() + " with path {}", this.layout.getRootFolder(), e);
			throw new GistRepositoryError(error, e);
		}
	}

	private Map<String, FileContent> getFileContent(Repository repository, RevCommit commit) throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		Map<String, FileContent> fileContent = new LinkedHashMap<String, FileContent>();
		RevTree tree = commit.getTree();
		try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setPostOrderTraversal(false);

            while(treeWalk.next()) {
            	
            	FileContent content = readContent(repository, treeWalk);
            	fileContent.put(content.getFilename(), content);
            }
        }
		return fileContent;
	}
	
	private RevCommit resolveCommit(Repository repository) throws IOException {
        try (RevWalk revWalk = new RevWalk(repository)) {
        	if(StringUtils.isEmpty(commitId)) {
        		Ref head = repository.exactRef(REF_HEAD_MASTER);
        		return revWalk.parseCommit(head.getObjectId());
        	} else {
        		return revWalk.parseCommit(ObjectId.fromString(this.commitId));
        	}
        }
	}

	private FileContent readContent(Repository repository, TreeWalk treeWalk) {
		
		ObjectId objectId = treeWalk.getObjectId(0);
		FileContent content = fileContentCache.load(objectId.getName());
		if(content == null) {
			content = new FileContent(); 
			String fileName = treeWalk.getPathString();
			try {
				content.setFilename(fileName);
				ObjectLoader loader = repository.open(objectId);
				
				content.setContent(new String(loader.getBytes(), Charsets.UTF_8));
				content.setSize(loader.getSize());
				content.setTruncated(false);
				String language = FilenameUtils.getExtension(fileName);
				if (!GitGistRepository.B64_BINARY_EXTENSION.equals(language) && !StringUtils.isEmpty(language)) {
					content.setLanguage(language);
				}
				content.setType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileName));
				fileContentCache.save(objectId.getName(), content);
			} catch (IOException e) {
				GistError error = new GistError(GistErrorCode.ERR_GIST_CONTENT_NOT_READABLE,
						"Could not read content of {} for gist {}", fileName, gistId);
				logger.error(error.getFormattedMessage() + " with path {}", this.layout.getRootFolder(), e);
				throw new GistRepositoryError(error, e);
			}
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

	private List<GistHistory> getHistory(Grgit git, RevCommit commit) {
		GitHistoryOperation historyOperation = new GitHistoryOperation();
		historyOperation.setCommitId(commitId);
		historyOperation.setRepository(git.getRepository());
		historyOperation.setHistoryStore(historyStore);
		return historyOperation.call();
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

	public void setMetadataStore(MetadataStore metadataStore) {
		this.metadataStore = metadataStore;
	}
	
	public MetadataStore getMetadataStore() {
		return this.metadataStore;
	}

	public void setHistorycache(HistoryCache historyStore) {
		this.historyStore = historyStore;
	}

	public String getGistId() {
		return gistId;
	}

	public void setGistId(String gistId) {
		this.gistId = gistId;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public void setFileContentCache(FileContentCache fileContentCache) {
		this.fileContentCache = fileContentCache;
	}

}
