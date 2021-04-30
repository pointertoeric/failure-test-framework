package org.failuretest.failurecore.utils;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class FileToolTest {

    String homeDir = System.getProperty("user.home");

    @Test
    public void testRelativePathToHome() {
        String relativePath = "~/test.java";
        String path = FileTool.resolvePath(relativePath);
        assertEquals(homeDir + relativePath.substring(1), path);
    }

    @Test
    public void testRelativePath() {
        // relative current dir
        String relativePath = "test.java";
        String path = FileTool.resolvePath(relativePath);
        assertEquals(Paths.get("").toAbsolutePath().toString() + "/test.java", path);
    }

    @Test
    public void testAbsolutePath() {
        String absolutePath = "/test/test.java";
        String path = FileTool.resolvePath(absolutePath);
        assertEquals(absolutePath, path);
    }

    @Test
    public void testLoadProperties() {
        Properties properties = null;
        try {
            properties = FileTool.loadProperties("config.properties");
        } catch (IOException e) {e.printStackTrace();}
        assertNotNull(properties);
    }
}
