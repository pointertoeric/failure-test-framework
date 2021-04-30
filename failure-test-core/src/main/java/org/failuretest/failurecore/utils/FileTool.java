package org.failuretest.failurecore.utils;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class FileTool {

    public static String resolvePath(String path) {
        String resolvPath;
        if (path.startsWith("~")) { // relative path in home folder
            resolvPath = System.getProperty("user.home") + path.substring(1);
        } else {
            resolvPath = Paths.get(path).toAbsolutePath().toString();
        }
        return resolvPath;
    }

    public static String readFile(String path) throws IOException {
        String realPath = resolvePath(path);
        if (Files.exists(Paths.get(realPath))) {
            return FileUtils.readFileToString(new File(realPath), StandardCharsets.UTF_8);
        } else { // class path
            return IOUtils.toString(FileTool.class.getClassLoader().getResourceAsStream(path), StandardCharsets.UTF_8);
        }
    }

    public static Properties loadProperties(String path) throws IOException {
        Properties properties = new Properties();
        String realPath = resolvePath(path);
        if (Files.exists(Paths.get(realPath))) {
            try (FileReader reader = new FileReader(realPath)) {
                properties.load(reader);
            }
        } else { // class path
            try (InputStream inputStream = FileTool.class.getClassLoader().getResourceAsStream(path)) {
                properties.load(inputStream);
            }
        }
        return properties;
    }
}
