package math;

import org.joml.Vector3d;
import org.joml.Vector3f;

public class MathUtil {
    public static final double EPSILON = 0.00001;

    /**
     * @return true if a is within EPSILON of b; false otherwise
     */
    public static boolean withinEpsilon(float a, float b) {
        return java.lang.Math.abs(a - b) <= java.lang.Math.max(java.lang.Math.abs(a), java.lang.Math.abs(b)) * EPSILON;
    }

    /**
     * @return true if a is within EPSILON of b; false otherwise
     */
    public static boolean withinEpsilon(double a, double b) {
//        return Math.abs(a - b) <= Math.max(Math.abs(a), Math.abs(b)) * EPSILON;
        return java.lang.Math.abs(a-b) <= EPSILON;
    }

    /**
     * @return true if all of a's components is within EPSILON of b; false otherwise
     */
    public static boolean withinEpsilon(Vector3d a, Vector3d b) {
        return withinEpsilon(a.x, b.x) && withinEpsilon(a.y, b.y) && withinEpsilon(a.z, b.z);
    }

    /**
     * @return Returns b if a is within EPISLON of b; returns a otherwise
     */
    public static double clipWithinEpsilon(double a, double b) {
        if (withinEpsilon(a, b)) {
            return b;
        }
        return a;
    }
    /**
     * @return Returns the first value in withinValues that is within EPISLON of a; returns a if no values are within EPISLON
     */
    public static double clipWithinEpsilon(double a, double... withinValues) {
        for (double x : withinValues) {
            if (withinEpsilon(a, x)) {
                return x;
            }
        }
        return a;
    }

    /**
     * Interpolates using a cubic function that starts at (0, 0) and ends at (1, 1)
     * @param x a value in [0, 1]
     * @return the y-value of the cubic function, which is in [0, 1]
     */

    public static float cubicInterpolation(float x) {
        return (x-1)*(x-1)*(x-1) + 1;
    }

    /**
     * Interpolates using a cubic function that starts at (0, 0) and ends at (1, 1)
     * @param x a value in [0, 1]
     * @return the y-value of the cubic function, which is in [0, 1]
     */
    public static double cubicInterpolation(double x) {
        return (x-1)*(x-1)*(x-1) + 1;
    }

    /**
     * @return true if value is within a and b, even when a > b
     */
    public static boolean withinRange(double value, double a, double b) {
        if (b < a) {
            double tmp = b;
            b = a;
            a = tmp;
        }
        return a <= value && value <= b;
    }

    /**
     * @return min(a,b). If min(a,b) is negative, returns max(a,b).
     */
    public static double minNonNegative(double a, double b) {
        double m = java.lang.Math.min(a,b);
        if (m < 0) return java.lang.Math.max(a,b);
        return m;
    }

    /**
     * Clips a and b to 0 and 1 if they are within EPSILON of those values, then returns minNonNegative(a, b)
     */
    public static double minNonNegativeClipped(double a, double b) {
        return minNonNegative(clipWithinEpsilon(a, 0, 1), clipWithinEpsilon(b, 0, 1));
    }

    /**
     * Clips a to min or max if it is smaller than min or greater than max, respectively
     * @return the clipped value
     */
    public static double cutMaxMin(double a, double min, double max) {
        return java.lang.Math.max(java.lang.Math.min(a, max), min);
    }

    /**
     * Clips a to min or max if it is smaller than min or greater than max, respectively
     * @return the clipped value
     */
    public static float cutMaxMin(float a, float min, float max) {
        return java.lang.Math.max(java.lang.Math.min(a, max), min);
    }

    /**
     * Inserts a vector into an array
     */
    public static void insertVector(float[] arr, int index, Vector3f vector) {
        arr[3*index] = vector.x;
        arr[3*index + 1] = vector.y;
        arr[3*index + 2] = vector.z;
    }
}
