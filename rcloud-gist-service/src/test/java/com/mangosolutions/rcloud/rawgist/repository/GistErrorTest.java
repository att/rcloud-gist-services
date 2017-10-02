/*******************************************************************************
* Copyright (c) 2017 AT&T Intellectual Property, [http://www.att.com]
*
* SPDX-License-Identifier:   MIT
*
*******************************************************************************/
package com.mangosolutions.rcloud.rawgist.repository;

import org.junit.Assert;
import org.junit.Test;

public class GistErrorTest {

    @Test
    public void gistErrorFormattingTest() {

        GistError error = new GistError(GistErrorCode.ERR_COMMENT_NOT_EXIST, "This message should be {}", "happy");
        Assert.assertEquals("This message should be {}", error.getMessage());
        Assert.assertEquals("ERR_COMMENT_NOT_EXIST: This message should be happy", error.getFormattedMessage());
        Assert.assertEquals("ERR_COMMENT_NOT_EXIST: This message should be happy", error.toString());

    }

    @Test
    public void gistErrorFormattingNoParamsTest() {

        GistError error = new GistError(GistErrorCode.ERR_COMMENT_NOT_EXIST,
                "This has a token, but should not be replaced {}");
        Assert.assertEquals("This has a token, but should not be replaced {}", error.getMessage());
        Assert.assertEquals("ERR_COMMENT_NOT_EXIST: This has a token, but should not be replaced {}",
                error.getFormattedMessage());
        Assert.assertEquals("ERR_COMMENT_NOT_EXIST: This has a token, but should not be replaced {}", error.toString());

    }
}
