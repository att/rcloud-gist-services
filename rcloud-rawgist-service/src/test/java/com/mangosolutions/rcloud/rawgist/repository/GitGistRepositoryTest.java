package com.mangosolutions.rcloud.rawgist.repository;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangosolutions.rcloud.rawgist.model.FileDefinition;
import com.mangosolutions.rcloud.rawgist.model.GistComment;
import com.mangosolutions.rcloud.rawgist.model.GistCommentResponse;
import com.mangosolutions.rcloud.rawgist.model.GistRequest;
import com.mangosolutions.rcloud.rawgist.model.GistResponse;



@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GitGistRepositoryTest {

	private GitGistRepository repository;
	
	private GitGistCommentRepository commentRepository;

	private String gistId;
	
	private UserDetails userDetails;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setup() {
		File repositoryFolder = folder.getRoot();
		gistId = UUID.randomUUID().toString();
		Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
		userDetails = new User("gist_user", "gist_user_pwd", authorities);
		repository = new GitGistRepository(repositoryFolder, gistId, objectMapper, userDetails);
		this.populateTestRepository();
		commentRepository = new GitGistCommentRepository(repositoryFolder, gistId, objectMapper);
	}
	
	public void populateTestRepository() {
		String expectedDescription = "This is a cool gist";
		String expectedFilename = "i_am_file_1.R";
		
		String initialContent = "I am the content of the file";
		String newFilename = "i_am_file_2.R";
		String newContent = "I am the content of a different file";
		this.createGist(expectedDescription, new String[]{expectedFilename, initialContent});
		this.updateGist(new String[]{newFilename, newContent});
	}
	
	@Test
	public void getEmptyCommentsTest() {
		List<GistCommentResponse> comments = commentRepository.getComments(userDetails);
		Assert.assertEquals(0, comments.size());
	}

	@Test
	public void addCommentTest() {
		GistComment comment = new GistComment();
		String expectedComment = "I am a comment"; 
		comment.setBody(expectedComment);
		GistCommentResponse response = commentRepository.createComment(comment, this.userDetails);
		Assert.assertEquals(expectedComment, response.getBody());
		Assert.assertEquals(Long.valueOf(1), response.getId()); //Slightly fragile makes an assumption about the id of the comments
		Assert.assertEquals(this.userDetails.getUsername(), response.getUser().getLogin());
		List<GistCommentResponse> comments = commentRepository.getComments(userDetails);
		Assert.assertEquals(1, comments.size());
	}
	
	@Test
	public void editCommentTest() {
		GistComment comment = new GistComment();
		comment.setBody("initial comment");
		GistCommentResponse response = commentRepository.createComment(comment, this.userDetails);
		String expectedComment = "updated comment"; 
		comment.setBody(expectedComment);
		response = commentRepository.editComment(response.getId(), comment, this.userDetails);
		Assert.assertEquals(expectedComment, response.getBody());
		Assert.assertEquals(Long.valueOf(1), response.getId()); //Slightly fragile makes an assumption about the id of the comments
		Assert.assertEquals(this.userDetails.getUsername(), response.getUser().getLogin());
		List<GistCommentResponse> comments = commentRepository.getComments(userDetails);
		Assert.assertEquals(1, comments.size());
	}
	
	@Test
	public void deleteCommentTest() {
		GistComment comment = new GistComment();
		comment.setBody("initial comment");
		GistCommentResponse response = commentRepository.createComment(comment, this.userDetails);
		long commentId = response.getId();
		String expectedComment = "anther comment"; 
		comment.setBody(expectedComment);
		response = commentRepository.createComment(comment, this.userDetails);
		
		commentRepository.deleteComment(commentId, this.userDetails);
		List<GistCommentResponse> comments = commentRepository.getComments(userDetails);
		Assert.assertEquals(1, comments.size());
		response = comments.get(0);
		Assert.assertEquals(expectedComment, response.getBody());
		Assert.assertEquals(Long.valueOf(2), response.getId()); //Slightly fragile makes an assumption about the id of the comments
		Assert.assertEquals(this.userDetails.getUsername(), response.getUser().getLogin());
	}
	
	@Test
	public void createLotsOfCommentsTest() {
		String commentBody = "I am a comment, add a number on me";
		for(int i = 0; i < 1000; i++) {
			GistComment comment = new GistComment();
			comment.setBody(commentBody + i);
			commentRepository.createComment(comment, this.userDetails);
		}
		List<GistCommentResponse> comments = commentRepository.getComments(this.userDetails);
		Assert.assertEquals(1000, comments.size());
		for(int i = 0; i < comments.size(); i++) {
			GistCommentResponse response = comments.get(i);
			String comment = response.getBody();
			Assert.assertEquals(commentBody + i, comment);
			Assert.assertEquals(Long.valueOf(i + 1), response.getId());
		}
	}
	
	private GistResponse updateGist(String[] contents) {
		GistRequest request = createGistRequest(null, contents);
		return repository.editGist(request, userDetails);
	}

	private GistResponse createGist(String description, String[]... contents) {
		GistRequest request = createGistRequest(description, contents);
		return repository.createGist(request, userDetails);
	}

	private GistRequest createGistRequest(String description, String[]... contents) {
		GistRequest request = new GistRequest();
		request.setDescription(description);
		Map<String, FileDefinition> files = new HashMap<>();
		for(String[] content: contents) {
			String fileName = content[0];
			FileDefinition fileDefinition = null;
			if(content.length > 1 && content[1] != null) {
				String fileContents = content[1];
				fileDefinition = new FileDefinition();
				fileDefinition.setContent(fileContents);
			}
			if(content.length > 2 && content[2] != null) {
				fileDefinition = fileDefinition == null? new FileDefinition(): fileDefinition;
				String newName = content[2];
				fileDefinition.setFilename(newName);
			}
			files.put(fileName, fileDefinition);
		}
		request.setFiles(files);
		return request;
	}
	


}
