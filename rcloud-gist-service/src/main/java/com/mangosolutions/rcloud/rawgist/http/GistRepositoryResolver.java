package com.mangosolutions.rcloud.rawgist.http;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.OpenOp;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;

import com.mangosolutions.rcloud.rawgist.repository.git.RepositoryLayout;
import com.mangosolutions.rcloud.rawgist.repository.git.RepositoryStorageLocator;

public class GistRepositoryResolver<C> implements RepositoryResolver<C> {

    private Collection<RepositoryStorageLocator> locators = new CopyOnWriteArrayList<>();
    
    
    public GistRepositoryResolver(Collection<RepositoryStorageLocator> locators) {
        this.locators.addAll(locators);
    }

    @Override
    public Repository open(final C req, final String name)
            throws RepositoryNotFoundException, ServiceNotEnabledException {
        // resolve the storage location
        File gistRepositoryFolder = this.getGistRepositoryFolder(name);
        OpenOp openOp = new OpenOp();
        openOp.setDir(gistRepositoryFolder);
        Grgit git = openOp.call();
        return git.getRepository().getJgit().getRepository();
    }

    private File getGistRepositoryFolder(String name) throws RepositoryNotFoundException {
        File gistRepositoryFolder = getRepositoryRootLocation(name);
        return getGitRepositoryFolder(gistRepositoryFolder, name);
    }

    private File getGitRepositoryFolder(File gistRepositoryFolder, String name) throws RepositoryNotFoundException {
        File gitRepositoryFolder;
        RepositoryLayout layout = new RepositoryLayout(gistRepositoryFolder);
        gitRepositoryFolder = layout.getBareFolder();
        if(!gitRepositoryFolder.exists()) {
            throw new RepositoryNotFoundException(name);
        }
        return gitRepositoryFolder;
    }

    private File getRepositoryRootLocation(String name) throws RepositoryNotFoundException {
        for(RepositoryStorageLocator locator: locators) {
            File repositoryFolder = locator.getStorageFolder(name);
            if (repositoryFolder.exists()) {
                return repositoryFolder;
            }
        }
        throw new RepositoryNotFoundException(name);
    }
}
