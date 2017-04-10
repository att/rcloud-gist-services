package com.mangosolutions.rcloud.rawgist.repository.git;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.CheckoutOp;
import org.ajoberstar.grgit.operation.OpenOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.mangosolutions.rcloud.rawgist.model.GistResponse;

public class ReadHistoricalGistOperation extends ReadGistOperation {

	private static final Logger logger = LoggerFactory.getLogger(ReadHistoricalGistOperation.class);

	private String commitId;

	@Override
	public GistResponse call() {

		// switch the working copy to that commit
		OpenOp openOp = new OpenOp();
		openOp.setDir(this.getLayout().getGistFolder());
		try (Grgit git = openOp.call()) {
			switchToCommit(git);
			try {
				return readGist(git);
			} finally {
				switchToHead(git);
			}
		}
	}

	private void switchToCommit(Grgit git) {
		if(!StringUtils.isEmpty(commitId)) {
			CheckoutOp checkoutOp = new CheckoutOp(git.getRepository());
			checkoutOp.setBranch(commitId);
			checkoutOp.call();
		}
	}
	
	private void switchToHead(Grgit git) {
		if(!StringUtils.isEmpty(commitId)) {
			CheckoutOp checkoutOp = new CheckoutOp(git.getRepository());
			checkoutOp.setBranch("master");
			checkoutOp.call();
		}
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

}
