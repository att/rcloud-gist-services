/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({ "deletions", "additions", "total" })
public class GitChangeStatus implements Serializable {

    private static final long serialVersionUID = -1548916401329014408L;

    private int deletions;

    private int additions;

    private int total;

    public int getDeletions() {
        return deletions;
    }

    public void setDeletions(int deletions) {
        this.deletions = deletions;
    }

    public int getAdditions() {
        return additions;
    }

    public void setAdditions(int additions) {
        this.additions = additions;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + additions;
        result = prime * result + deletions;
        result = prime * result + total;
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
        GitChangeStatus other = (GitChangeStatus) obj;
        if (additions != other.additions)
            return false;
        if (deletions != other.deletions)
            return false;
        if (total != other.total)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "GitChangeStatus [deletions=" + deletions + ", additions=" + additions + ", total=" + total + "]";
    }

}
