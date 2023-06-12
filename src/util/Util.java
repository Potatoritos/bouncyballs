package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
    public static final double EPSILON = 0.00001;
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
    public static boolean withinEpsilon(double a, double b) {
//        return Math.abs(a - b) <= Math.max(Math.abs(a), Math.abs(b)) * EPSILON;
        return Math.abs(a-b) <= EPSILON;
    }
    public static double clipWithinEpsilon(double a, double b) {
        if (withinEpsilon(a, b)) {
            return b;
        }
        return a;
    }

    public static double clipWithinEpsilon(double a, double... withinValues) {
        for (double x : withinValues) {
            if (withinEpsilon(a, x)) {
                return x;
            }
        }
        return a;
    }

    public static boolean withinRange(double value, double a, double b) {
        if (b < a) {
            double tmp = b;
            b = a;
            a = tmp;
        }
        return a <= value && value <= b;
    }

    public static double cutMaxMin(double a, double min, double max) {
        return Math.max(Math.min(a, max), min);
    }
    public static float cutMaxMin(float a, float min, float max) {
        return Math.max(Math.min(a, max), min);
    }
}
