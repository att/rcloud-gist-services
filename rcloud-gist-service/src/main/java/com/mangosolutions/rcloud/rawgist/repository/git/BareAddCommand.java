package com.mangosolutions.rcloud.rawgist.repository.git;

import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;
import static org.eclipse.jgit.lib.FileMode.GITLINK;
import static org.eclipse.jgit.lib.FileMode.TYPE_GITLINK;
import static org.eclipse.jgit.lib.FileMode.TYPE_TREE;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.errors.FilterFailedException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuildIterator;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;
import org.eclipse.jgit.treewalk.TreeWalk.OperationType;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;

public class BareAddCommand extends GitCommand<DirCache> {

	private Collection<String> filepatterns;

	private WorkingTreeIterator workingTreeIterator;

	private boolean update = false;
	
	private DirCache dirCache;
	
	
	protected BareAddCommand(Repository repo) {
		super(repo);
		filepatterns = new LinkedList<String>();
	}
	
	protected BareAddCommand(Repository repo, DirCache dirCache) {
		this(repo);
		this.dirCache = dirCache;
	}
	
	public BareAddCommand addFilepattern(String filepattern) {
		checkCallable();
		filepatterns.add(filepattern);
		return this;
	}
	
	public BareAddCommand addDirCache(DirCache dirCache) {
		this.dirCache = dirCache;
		return this;
	}

	/**
	 * Allow clients to provide their own implementation of a FileTreeIterator
	 * @param f
	 * @return {@code this}
	 */
	public BareAddCommand setWorkingTreeIterator(WorkingTreeIterator f) {
		workingTreeIterator = f;
		return this;
	}

	@Override
	public DirCache call() throws GitAPIException {
		if (filepatterns.isEmpty()) {
			throw new NoFilepatternException(JGitText.get().atLeastOnePatternIsRequired);
		}
		checkCallable();
		boolean addAll = filepatterns.contains("."); //$NON-NLS-1$
		try (ObjectInserter inserter = repo.newObjectInserter();
				NameConflictTreeWalk tw = new NameConflictTreeWalk(repo)) {
			tw.setOperationType(OperationType.CHECKIN_OP);
			dirCache.lock();
			DirCacheBuilder builder = dirCache.builder();
			tw.addTree(new DirCacheBuildIterator(builder));
			if (workingTreeIterator == null)
				workingTreeIterator = new FileTreeIterator(repo);
			workingTreeIterator.setDirCacheIterator(tw, 0);
			tw.addTree(workingTreeIterator);
			if (!addAll) {
				tw.setFilter(PathFilterGroup.createFromStrings(filepatterns));
			}

			byte[] lastAdded = null;

			while (tw.next()) {
				DirCacheIterator c = tw.getTree(0, DirCacheIterator.class);
				WorkingTreeIterator f = tw.getTree(1, WorkingTreeIterator.class);
				if (c == null && f != null && f.isEntryIgnored()) {
					// file is not in index but is ignored, do nothing
					continue;
				} else if (c == null && update) {
					// Only update of existing entries was requested.
					continue;
				}

				DirCacheEntry entry = c != null ? c.getDirCacheEntry() : null;
				if (entry != null && entry.getStage() > 0
						&& lastAdded != null
						&& lastAdded.length == tw.getPathLength()
						&& tw.isPathPrefix(lastAdded, lastAdded.length) == 0) {
					// In case of an existing merge conflict the
					// DirCacheBuildIterator iterates over all stages of
					// this path, we however want to add only one
					// new DirCacheEntry per path.
					continue;
				}

				if (tw.isSubtree() && !tw.isDirectoryFileConflict()) {
					tw.enterSubtree();
					continue;
				}

				if (f == null) { // working tree file does not exist
					if (entry != null
							&& (!update || GITLINK == entry.getFileMode())) {
						builder.add(entry);
					}
					continue;
				}

				if (entry != null && entry.isAssumeValid()) {
					// Index entry is marked assume valid. Even though
					// the user specified the file to be added JGit does
					// not consider the file for addition.
					builder.add(entry);
					continue;
				}

				if ((f.getEntryRawMode() == TYPE_TREE
						&& f.getIndexFileMode(c) != FileMode.GITLINK) ||
						(f.getEntryRawMode() == TYPE_GITLINK
								&& f.getIndexFileMode(c) == FileMode.TREE)) {
					// Index entry exists and is symlink, gitlink or file,
					// otherwise the tree would have been entered above.
					// Replace the index entry by diving into tree of files.
					tw.enterSubtree();
					continue;
				}

				byte[] path = tw.getRawPath();
				if (entry == null || entry.getStage() > 0) {
					entry = new DirCacheEntry(path);
				}
				FileMode mode = f.getIndexFileMode(c);
				entry.setFileMode(mode);

				if (GITLINK != mode) {
					entry.setLength(f.getEntryLength());
					entry.setLastModified(f.getEntryLastModified());
					long len = f.getEntryContentLength();
					try (InputStream in = f.openEntryStream()) {
						ObjectId id = inserter.insert(OBJ_BLOB, len, in);
						entry.setObjectId(id);
					}
				} else {
					entry.setLength(0);
					entry.setLastModified(0);
					entry.setObjectId(f.getEntryObjectId());
				}
				builder.add(entry);
				lastAdded = path;
			}
			inserter.flush();
			builder.commit();
			setCallable(false);
		} catch (IOException e) {
			Throwable cause = e.getCause();
			if (cause != null && cause instanceof FilterFailedException)
				throw (FilterFailedException) cause;
			throw new JGitInternalException(
					JGitText.get().exceptionCaughtDuringExecutionOfAddCommand, e);
		} finally {
			if (dirCache != null) {
				dirCache.unlock();
			}
		}
		return dirCache;

	}

}
