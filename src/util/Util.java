package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
    public static String getFileSource(String path) {
        try {
            return Files.readString(Paths.get("assets/" + path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not get file source: " + path);
        }
    }
}
