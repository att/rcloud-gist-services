/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserAuthorityResolver implements AuthorityResolver {

    public static final GrantedAuthority USER_AUTHORITY = new SimpleGrantedAuthority("ROLE_USER");
    
    @Override
    public GrantedAuthority resolve(String username) {
        if(StringUtils.isNotBlank(username)) {
            return USER_AUTHORITY;
        }
        return null;
    }

}
