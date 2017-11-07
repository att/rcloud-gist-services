package com.mangosolutions.rcloud.rawgist.repository.git;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class CollaborationDataStore {

    private Map<String, List<String>> collaborators = new HashMap<>();

    public CollaborationDataStore() {

    }

    public CollaborationDataStore(Map<String, List<String>> collaborators) {
        this.collaborators = collaborators;
    }

    public Collection<String> getCollaborators(String user) {
        if (collaborators.containsKey(user) && collaborators.get(user) != null) {
            return new LinkedHashSet<>(collaborators.get(user));
        } else {
            return Collections.emptySet();
        }
    }

    public void updateCollaborators(Map<String, List<String>> collaborators) {
        this.collaborators.clear();
        for (Map.Entry<String, List<String>> entry : collaborators.entrySet()) {
            this.collaborators.put(entry.getKey(), entry.getValue());
        }
    }

}
