/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.sessionkeyauth;

import org.springframework.security.core.GrantedAuthority;

public interface AuthorityResolver {

    GrantedAuthority resolve(String username);
    
}
