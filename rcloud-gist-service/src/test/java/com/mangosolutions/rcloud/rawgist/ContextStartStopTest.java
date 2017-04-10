package com.mangosolutions.rcloud.rawgist;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { Application.class })
@ContextConfiguration(loader = SpringBootContextLoader.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ContextStartStopTest {

    @Test
    public void testContextStartStop() {

    }
}