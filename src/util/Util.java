package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
    public static final float EPSILON = 0.00001f;
    public static String getFileSource(String path) {
        try {
            return Files.readString(Paths.get("assets/" + path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not get file source: " + path);
        }
    }

    public static boolean withinEpsilon(float a, float b) {
        return Math.abs(a - b) <= Math.max(Math.abs(a), Math.abs(b)) * EPSILON;
    }

    public static double cutMaxMin(double a, double min, double max) {
        return Math.max(Math.min(a, max), min);
    }
    public static float cutMaxMin(float a, float min, float max) {
        return Math.max(Math.min(a, max), min);
    }
}
