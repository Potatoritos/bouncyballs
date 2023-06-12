package geometry;

import math.Quadratic;
import org.joml.Matrix3d;
import org.joml.Vector2d;
import org.joml.Vector3d;

import static math.MathUtil.*;


public class Geometry {
    private static final Vector3d xHat = new Vector3d(1, 0, 0);
    private static final Vector3d yHat = new Vector3d(0, 1, 0);
    private static final Vector3d zHat = new Vector3d(0, 0, 1);

    // Stores the projection of a onto b in result
    public static void project(Vector2d a, Vector2d b, Vector2d result) {
        result.set(b);
        result.mul(a.dot(b) / b.lengthSquared());
    }
    public static void project(Vector3d a, Vector3d b, Vector3d result) {
        result.set(b);
        result.mul(a.dot(b) / b.lengthSquared());
    }

    public static boolean intersectionLinePlane(Line3 line, Plane plane, Vector3d result) {
        // set t = (plane.d1 × plane.d2) • (line.pos - plane.pos) / -line.d • (plane.d1 × plane.d2)
        Vector3d r = new Vector3d();
        plane.displacement1.cross(plane.displacement2, r);

        Vector3d posDifference = new Vector3d();
        line.position.sub(plane.position, posDifference); // line.pos - plane.pos

        double t = r.dot(posDifference);
        Vector3d lineNegative = new Vector3d();
        line.displacement.negate(lineNegative);
        double denom = lineNegative.dot(r);
        t /= denom;

        // set u = (plane.d2 × -line.d) • (line.pos - plane.pos) / -line.d • (plane.d1 × plane.d2)
        plane.displacement2.cross(lineNegative, r);
        double u = r.dot(posDifference);
        u /= denom;

        // set v = (-line.d × plane.d1) • (line.pos - plane.pos) / -line.d • (plane.d1 × plane.d2)
        lineNegative.cross(plane.displacement1, r);
        double v = r.dot(posDifference);
        v /= denom;

        t = clipWithinEpsilon(t, 0, 1);
        u = clipWithinEpsilon(u, 0, 1);
        v = clipWithinEpsilon(v, 0, 1);
        if (t < 0 || t > 1 || u < 0 || u > 1 || v < 0 || v > 1) {
            return false;
        }

        result.set(line.displacement).mul(t).add(line.position);
        return true;
    }

    public static void arbitraryTangent(Vector3d normal, Vector3d result) {
        if (normal.x == 0) {
            result.set(xHat);
        } else if (normal.y == 0) {
            result.set(yHat);
        } else if (normal.z == 0) {
            result.set(zHat);
        } else {
            result.set(1/normal.x, -1/normal.y, 0);
        }
    }
    public static boolean intersectionLineCylinder(Line3 line, Cylinder cylinder, Vector3d result) {
        // Rotate everything such that the cylinder's axis lies on the z-axis
        // Only works for axis-aligned cylinders (too lazy to figure out the proper way of doing this)
        Line3 rotatedLine = new Line3(line);
        Line3 rotatedCylinderAxis = new Line3(
                new Vector3d(cylinder.position),
                new Vector3d(cylinder.axis)
        );
        Matrix3d rotationMatrix = new Matrix3d().identity();
        if (cylinder.axis.x == 0 && cylinder.axis.z == 0) {
            rotationMatrix.rotationX(Math.PI/2);
        } else if (cylinder.axis.y == 0 && cylinder.axis.z == 0) {
            rotationMatrix.rotationY(Math.PI/2);
        }
        rotatedLine.position.mul(rotationMatrix);
        rotatedLine.displacement.mul(rotationMatrix);
        rotatedCylinderAxis.position.mul(rotationMatrix);
        rotatedCylinderAxis.displacement.mul(rotationMatrix);

        // Project the rotated line and cylinder to the XY plane
        Line2 lineXY = new Line2(
                new Vector2d(rotatedLine.position.x, rotatedLine.position.y),
                new Vector2d(rotatedLine.displacement.x, rotatedLine.displacement.y)
        );
        if (lineXY.displacement.lengthSquared() == 0) {
            return false;
        }
        Vector2d cylinderPositionXY = new Vector2d(rotatedCylinderAxis.position.x, rotatedCylinderAxis.position.y);
        Circle circle = new Circle(cylinder.radius, new Vector2d(cylinderPositionXY));

        // Find the intersection of the projected lines and cylinder (now a circle)
        Vector3d circleIntersectionResult = new Vector3d();
        if (!intersectionLineCircleT(lineXY, circle, circleIntersectionResult)) {
            return false;
        }

        // Check if the intersection is on the cylinder (in the z-direction)
        double t = circleIntersectionResult.z;
        double intersectionZ = rotatedLine.position.z + t*rotatedLine.displacement.z;
        double bottom = rotatedCylinderAxis.position.z;
        double top = bottom + rotatedCylinderAxis.displacement.z;
        if (!withinRange(intersectionZ, bottom, top)) {
            return false;
        }

        result.set(line.displacement).mul(t).add(line.position);
        return true;
    }
    public static boolean intersectionLineCylinderOld(Line3 line, Cylinder cylinder, Vector3d result) {
        Vector3d x1 = cylinder.position;
        Vector3d x2 = new Vector3d(x1).add(cylinder.axis);

        Vector3d u1 = new Vector3d(x1).cross(x2);
        Vector3d u2 = new Vector3d(x1).cross(line.position);
        Vector3d u3 = new Vector3d(line.position).cross(x2);

        Vector3d u4 = new Vector3d(x1).cross(line.displacement);
        Vector3d u5 = new Vector3d(line.displacement).cross(x2);

        Vector3d A = new Vector3d(u1).sub(u2).sub(u3);
        Vector3d B = new Vector3d(u4).add(u5);

        double u6 = new Vector3d(x1).sub(x2).lengthSquared() * cylinder.getRadius() * cylinder.getRadius();

        double a = B.x*B.x + B.y*B.y + B.z*B.z;
        double b = -2 * (A.x*B.x + A.y*B.y + A.z*B.z);
        double c = A.x*A.x + A.y*A.y + A.z*A.z - u6;

        double discrim = b*b - 4*a*c;
        if (discrim < 0) {
            System.out.println("Discrim");
            return false;
        }

        double t1 = (-b - discrim) / (2*a);
        double t2 = (-b + discrim) / (2*a);

        System.out.printf("ts %f %f\n", t1, t2);

        if (t1 < 0) t1 = t2;
        if (t2 < 0) t2 = t1;
        double t = Math.min(t1, t2);

        if (t < 0 || t > 1) {
            System.out.println("t");
            return false;
        }

        result.set(line.displacement).mul(t).add(line.position);

        Vector3d u7 = new Vector3d(result).sub(x1);
        Vector3d u8 = new Vector3d(x2).sub(x1);
        double check = u7.dot(u8);
        if (check < 0 || check > u8.lengthSquared()) {
            System.out.println("check");
            return false;
        }

        return true;
    }

    public static boolean intersectionLineSphere(Line3 line, Sphere sphere, Vector3d result) {
        double a = line.displacement.lengthSquared();

        Vector3d sphereToLine = new Vector3d();
        line.position.sub(sphere.position, sphereToLine);

        double b = 2 * line.displacement.dot(sphereToLine);
        double c = sphereToLine.lengthSquared() - sphere.getRadius()*sphere.getRadius();

        double discrim = b*b - 4*a*c;
        if (discrim < 0) {
            return false;
        }
        discrim = Math.sqrt(discrim);

        double t1 = (-b - discrim) / (2*a);
        double t2 = (-b + discrim) / (2*a);

        System.out.printf("t1=%f, t2=%f\n", t1, t2);

        if (t1 < 0) t1 = t2;
        if (t2 < 0) t2 = t1;
        double t = Math.min(t1, t2);

        if (t < 0 || t > 1) {
            return false;
        }

        result.set(line.displacement).mul(t).add(line.position);
        return true;
    }

    public static boolean intersectionLineCircleT(double a, double b, double c, double d, double r, double u, double v, Vector3d result) {
        if (a == 0 && c == 0) {
            return false;
        }
        Quadratic q = new Quadratic(
            a*a + c*c,
            2*a*(b-u) + 2*c*(d-v),
            (b-u)*(b-u) + (d-v)*(d-v) - r*r
        );
        if (q.discriminant() < 0) {
            return false;
        }

        double t = minNonNegativeClipped(q.solution1(), q.solution2());
        if (t < 0 || t > 1) {
            return false;
        }
        result.set(a*t + b, c*t + d, t);
        return true;
    }

    public static boolean intersectionLineCircleT(Line2 line, Circle circle, Vector3d result) {
        return intersectionLineCircleT(line.displacement.x, line.position.x, line.displacement.y, line.position.y, circle.radius, circle.position.x, circle.position.y, result);
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
        Vector3d resultT = new Vector3d();
        boolean intersects = intersectionLineCircleT(a, b, c, d, r, u, v, resultT);
        result.set(resultT.x, resultT.y);
        return intersects;
    }

    public static boolean intersectionLineCircle(Line2 line, Circle circle, Vector2d result) {
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
            t = clipWithinEpsilon(clipWithinEpsilon(t, 0), 1);
            s = clipWithinEpsilon(clipWithinEpsilon(t, 0), 1);
//            boolean t1Within = withinEpsilon(t, 0);
//            boolean t2Within = withinEpsilon(s, 0);
//            t = clipWithinEpsilon(t, 0);
//            s = clipWithinEpsilon(s, 0);
//            if (t1Within) t = 0;
//            if (t2Within) s = 0;
//            if (!t1Within && !t2Within) {
//                return false;
//            }
            if (t < 0 || t > 1 || s < 0 || s > 1)
                return false;
        }
        result.set(a*t+b, c*t+d);
        return true;
    }

    public static boolean intersectionLineWallX(Line2 line, Line2 wall, Vector2d result) {
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

    public static boolean intersectionLineWallY(Line2 line, Line2 wall, Vector2d result) {
        return intersectionLineWallY(line.displacement.x, line.position.x, line.displacement.y, line.position.y, wall.displacement.x, wall.position.x, wall.position.y, result);
    }

//    public static double distanceLinePoint(Line line, Vector2d point) {
//        return Math.abs((line.x2()-line.x1())*(line.y1()-point.y) - (line.x1()-point.x)*(line.y2()-line.y1())) / Math.hypot(line.x2()-line.x1(), line.y2()-line.y1());
//    }
    public static double distanceLineSegmentPoint(Line2 line, Vector2d point) {
        double l = line.displacement.lengthSquared();
        if (l == 0) return distance(line.position, point);
        Vector2d u = new Vector2d();
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
    public static double distance(Vector3d a, Vector3d b) {
        return Math.sqrt((b.x-a.x)*(b.x-a.x) + (b.y-a.y)*(b.y-a.y) + (b.z-a.z)*(b.z-a.z));
    }
}
