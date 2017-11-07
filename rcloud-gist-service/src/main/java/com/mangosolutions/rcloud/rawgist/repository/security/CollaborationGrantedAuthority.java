/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.util.StringUtils;
import org.springframework.security.core.GrantedAuthority;

public class CollaborationGrantedAuthority implements GrantedAuthority {

    private static final long serialVersionUID = -413651046802286010L;

    private static final String COLLABORATOR_ROLE = "COLLABORATOR_ROLE";
    
    private Set<String> collaborations = Collections.emptySet();

    public CollaborationGrantedAuthority() {
        this(null);
    }
    
    public CollaborationGrantedAuthority(String[] collaborations) {
        if(collaborations != null) {
            this.collaborations = new HashSet<>(Arrays.asList(collaborations));
        }
    }

    @Override
    public String getAuthority() {
        return COLLABORATOR_ROLE + ":" + StringUtils.join(collaborations, ",");
    }
    
    public Collection<String> getCollaborations() {
        return Collections.unmodifiableSet(collaborations);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((collaborations == null) ? 0 : collaborations.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CollaborationGrantedAuthority other = (CollaborationGrantedAuthority) obj;
        if (collaborations == null) {
            if (other.collaborations != null)
                return false;
        } else if (!collaborations.equals(other.collaborations))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CollaborationGrantedAuthority [collaborations=" + collaborations + "]";
    }

}
