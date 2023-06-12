package math;

public class MathUtil {
    public static final double EPSILON = 0.00001;
    public static boolean withinEpsilon(float a, float b) {
        return java.lang.Math.abs(a - b) <= java.lang.Math.max(java.lang.Math.abs(a), java.lang.Math.abs(b)) * EPSILON;
    }
    public static boolean withinEpsilon(double a, double b) {
//        return Math.abs(a - b) <= Math.max(Math.abs(a), Math.abs(b)) * EPSILON;
        return java.lang.Math.abs(a-b) <= EPSILON;
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

    // Returns min(a,b). If min(a,b) is negative, returns max(a,b).
    public static double minNonNegative(double a, double b) {
        double m = java.lang.Math.min(a,b);
        if (m < 0) return java.lang.Math.max(a,b);
        return m;
    }

    public static double minNonNegativeClipped(double a, double b) {
        return minNonNegative(clipWithinEpsilon(a, 0, 1), clipWithinEpsilon(b, 0, 1));
    }

    public static double cutMaxMin(double a, double min, double max) {
        return java.lang.Math.max(java.lang.Math.min(a, max), min);
    }
    public static float cutMaxMin(float a, float min, float max) {
        return java.lang.Math.max(java.lang.Math.min(a, max), min);
    }
}
