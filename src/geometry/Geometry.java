package geometry;

import org.joml.Vector2d;

import static util.Util.clipWithinEpsilon;
import static util.Util.withinEpsilon;


public class Geometry {
    private static Vector2d u;
    static {
        u = new Vector2d();
    }
    // Stores the projection of a onto b in result
    public static void project(Vector2d a, Vector2d b, Vector2d result) {
        result.set(b);
        result.mul(a.dot(b) / b.dot(b));
    }

    // Finds the intersection (x,y) closest to (b,d) of the
    // line segment and circle given by the parametric equations
    //  ⎡ x = at + b
    //  ⎣ y = ct + d
    // and
    //  ⎡ x = r sin(s) + u
    //  ⎣ y = r cos(s) + v
    // where 0 ≤ t ≤ 1, 0 ≤ s ≤ 2π
    public static boolean intersectionLineCircle(double a, double b, double c, double d, double r, double u, double v, Vector2d result) {
        if (a == 0 && c == 0) {
            return false;
        }
        double A = a*a + c*c;
        double B = 2*a*(b-u) + 2*c*(d-v);
        double C = (b-u)*(b-u) + (d-v)*(d-v) - r*r;
        double D = B*B - 4*A*C;
        if (D < 0) {
            return false;

        }

        double sqrtD = Math.sqrt(D);
        double t1 = (-B + sqrtD) / (2*A);
        double t2 = (-B - sqrtD) / (2*A);

        if ((t1 < 0 || t1 > 1) && (t2 < 0 || t2 > 1) && !withinEpsilon(t1, 0) && !withinEpsilon(t2, 0)) {
            boolean t1Within = withinEpsilon(t1, 0);
            boolean t2Within = withinEpsilon(t2, 0);
            if (t1Within) t1 = 0;
            if (t2Within) t2 = 0;
            if (!t1Within && !t2Within) {
                return false;
            }
//            System.out.printf("bruh t2=%f within=%b\n", t2, withinEpsilon(t2, 0));
        }

        double x1 = a*t1 + b;
        double y1 = c*t1 + d;
        double x2 = a*t2 + b;
        double y2 = c*t2 + d;
        if (Math.hypot(x1-b, y1-d) <= Math.hypot(x2-b, y2-d)) {
            result.set(x1, y1);
        } else {
            result.set(x2, y2);
        }
        return true;
    }

    public static boolean intersectionLineCircle(Line line, Circle circle, Vector2d result) {
        return intersectionLineCircle(line.displacement.x, line.position.x, line.displacement.y, line.position.y, circle.radius, circle.position.x, circle.position.y, result);
    }

    // Finds the intersection (x,y) of the
    // rays given by the parametric equations
    //  ⎡ x = at + b
    //  ⎣ y = ct + d
    // and
    //  ⎡ x = e
    //  ⎣ y = gs + h
    // where 0 ≤ s,t ≤ 1
    public static boolean intersectionLineWallX(double a, double b, double c, double d, double e, double g, double h, Vector2d result) {
        if (a == 0 || g == 0) {
            return false;
        }
        double t = (e-b)/a;
        double s = (c*t + d - h)/g;
        if (t < 0 || t > 1 || s < 0 || s > 1) {
//            t = clipWithinEpsilon(clipWithinEpsilon(t, 0), 1);
//            s = clipWithinEpsilon(clipWithinEpsilon(t, 0), 1);
            boolean t1Within = withinEpsilon(t, 0);
            boolean t2Within = withinEpsilon(s, 0);
//            t = clipWithinEpsilon(t, 0);
//            s = clipWithinEpsilon(s, 0);
            if (t1Within) t = 0;
            if (t2Within) s = 0;
            if (!t1Within && !t2Within) {
                return false;
            }
//            if (t < 0 || t > 1 || s < 0 || s > 1)
//                return false;
        }
        result.set(a*t+b, c*t+d);
        return true;
    }

    public static boolean intersectionLineWallX(Line line, Line wall, Vector2d result) {
        return intersectionLineWallX(line.displacement.x, line.position.x, line.displacement.y, line.position.y, wall.position.x, wall.displacement.y, wall.position.y, result);
    }

    // Finds the intersection (x,y) of the
    // rays given by the parametric equations
    //  ⎡ x = at + b
    //  ⎣ y = ct + d
    // and
    //  ⎡ x = es + f
    //  ⎣ y = g
    // where 0 ≤ s,t ≤ 1
    public static boolean intersectionLineWallY(double a, double b, double c, double d, double e, double f, double g, Vector2d result) {
        boolean r = intersectionLineWallX(c, d, a, b, g, e, f, result);
        result.set(result.y, result.x);
        return r;
    }

    public static boolean intersectionLineWallY(Line line, Line wall, Vector2d result) {
        return intersectionLineWallY(line.displacement.x, line.position.x, line.displacement.y, line.position.y, wall.displacement.x, wall.position.x, wall.position.y, result);
    }

//    public static double distanceLinePoint(Line line, Vector2d point) {
//        return Math.abs((line.x2()-line.x1())*(line.y1()-point.y) - (line.x1()-point.x)*(line.y2()-line.y1())) / Math.hypot(line.x2()-line.x1(), line.y2()-line.y1());
//    }
    public static double distanceLineSegmentPoint(Line line, Vector2d point) {
        double l = line.displacement.lengthSquared();
        if (l == 0) return distance(line.position, point);
        point.sub(line.position, u);
        double t = Math.max(0, Math.min(1, u.dot(line.displacement) / l));
        u.set(line.displacement).mul(t).add(line.position);
        return distance(u, point);
    }
    public static double distanceCirclePoint(Circle circle, Vector2d point) {
        return Math.hypot(circle.position.x-point.x, circle.position.y-point.y) - circle.radius;
    }
    public static double distance(Vector2d a, Vector2d b) {
        return Math.hypot(b.x-a.x, b.y-a.y);
    }
}
