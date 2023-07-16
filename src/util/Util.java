package util;

import org.joml.Vector3d;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
    /**
     * Gets the contents of a file
     * @param path the path of the file
     * @return a string containing the file contents in entirety
     */
    public static String getFileSource(String path) {
        try {
            return Files.readString(Paths.get("assets/" + path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not get file source: " + path);
        }
    }

    public static Vector3f vector3dTo3f(Vector3d vector) {
        return new Vector3f((float)vector.x, (float)vector.y, (float)vector.z);
    }
}
