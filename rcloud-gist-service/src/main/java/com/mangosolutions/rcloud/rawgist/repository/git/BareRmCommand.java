package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuildIterator;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;

/**
 * Removes files from the bare git repository. This is based
 * upon the JGit RmCommand.  
 */
public class BareRmCommand  extends GitCommand<DirCache> {

	/**
	 * patterns of files that should be removed from the repository.
	 */
	private Collection<String> filepatterns;
	
	/**
	 * The index file to update with the changes.
	 */
	private DirCache index;

	/** Only remove files from index, not from working directory */
	private boolean cached = true;

	/**
	 * Creates the command to perform the operation on the repository 
	 * @param repo the repository to operate on.
	 * @param the index file to change.
	 */
	public BareRmCommand(Repository repo, DirCache index) {
		super(repo);
		filepatterns = new LinkedHashSet<String>();
		this.index = index;
	}

	/**
	 * Adds file patterns to be processed.
	 * @param filepattern
	 *            repository-relative path of file to remove (with
	 *            <code>/</code> as separator)
	 * @return {@code this}
	 */
	public BareRmCommand addFilepattern(String filepattern) {
		checkCallable();
		filepatterns.add(filepattern);
		return this;
	}

	/**
	 * Only remove the specified files from the index.
	 *
	 * @param cached
	 *            true if files should only be removed from index, false if
	 *            files should also be deleted from the working directory
	 * @return {@code this}
	 * @since 2.2
	 */
	public BareRmCommand setCached(boolean cached) {
		checkCallable();
		this.cached = cached;
		return this;
	}

	/**
	 * Executes the {@code Rm} command. Each instance of this class should only
	 * be used for one invocation of the command. Don't call this method twice
	 * on an instance.
	 *
	 * @return the DirCache after Rm
	 */
	public DirCache call() throws GitAPIException,
			NoFilepatternException {

		if (filepatterns.isEmpty()) {
			throw new NoFilepatternException(JGitText.get().atLeastOnePatternIsRequired);
		}
		checkCallable();

		try (final TreeWalk tw = new TreeWalk(repo)) {
			index.lock();
			DirCacheBuilder builder = index.builder();
			tw.reset(); // drop the first empty tree, which we do not need here
			tw.setRecursive(true);
			tw.setFilter(PathFilterGroup.createFromStrings(filepatterns));
			tw.addTree(new DirCacheBuildIterator(builder));

			while (tw.next()) {
				if (!cached) {
					final FileMode mode = tw.getFileMode(0);
					if (mode.getObjectType() == Constants.OBJ_BLOB) {
						final File path = new File(repo.getWorkTree(),
								tw.getPathString());
						// Deleting a blob is simply a matter of removing
						// the file or symlink named by the tree entry.
						delete(path);
					}
				}
			}
			builder.commit();
			setCallable(false);
		} catch (IOException e) {
			throw new JGitInternalException(
					JGitText.get().exceptionCaughtDuringExecutionOfRmCommand, e);
		} finally {
			if (index != null) {
				index.unlock();
			}
		}

		return index;
	}

	private void delete(File p) {
		while (p != null && !p.equals(repo.getWorkTree()) && p.delete()) {
			p = p.getParentFile();
		}
	}

}
