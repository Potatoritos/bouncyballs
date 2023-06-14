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
    // note: result should not be the same variable as a
    public static void project(Vector3d a, Vector3d b, Vector3d result) {
        result.set(b);
        result.mul(a.dot(b) / b.lengthSquared());
    }
    public static void project(Vector2d a, Vector2d b, Vector2d result) {
        result.set(b);
        result.mul(a.dot(b) / b.lengthSquared());
    }

    // Reflects a line off of a surface
    // intersection: the point where the line collides with the surface
    // normal, parallel1, parallel2: a normal and 2 parallels to the point of intersection
    // restitution: the ratio between the initial and final "velocity" of the line after hitting the surface
    public static void reflectLine(Line3 line, Vector3d intersection, Vector3d normal, double restitution) {
        Vector3d normalComponent = new Vector3d();
        project(line.displacement, normal, normalComponent);

        // Nullify the rebound if it is small
        if (normalComponent.length()*restitution <= 0.004) {
            restitution = 0;
        }

        line.displacement.sub(normalComponent.mul(1 + restitution));
        line.position.set(intersection);
    }
    public static void reflectLineFixedRebound(Line3 line, Vector3d intersection, Vector3d normal, double reboundVelocity) {
        Vector3d normalComponent = new Vector3d();
        project(line.displacement, normal, normalComponent);
        line.displacement.sub(normalComponent.mul(1 + reboundVelocity/normalComponent.length()));
        line.position.set(intersection);
    }

    // If t is not -1, sets result to line.pos + t*line.displacement and returns true
    // Otherwise, returns false
    public static boolean scaleLine(Line3 line, double t, Vector3d result) {
        if (t == -1) return false;
        result.set(line.displacement).mul(t).add(line.position);
        return true;
    }
    public static boolean scaleLine(Line2 line, double t, Vector2d result) {
        if (t == -1) return false;
        result.set(line.displacement).mul(t).add(line.position);
        return true;
    }

    public static void intersectionLinePlaneTUV(Line3 line, Plane plane, Vector3d result) {
        if (line.displacement.lengthSquared() == 0) {
            result.x = -1;
            return;
        }
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
        result.set(t, u, v);
    }
    public static double intersectionLinePlaneTriangle(Line3 line, Plane plane) {
        Vector3d result = new Vector3d();
        intersectionLinePlaneTUV(line, plane, result);
        double t = result.x, u = result.y, v = result.z;
        if (t < 0 || t > 1 || u < 0 || u > 1 || v < 0 || v > 1 || (u+v) > 1) {
            return -1;
        }
        return t;
    }
    public static boolean intersectionLinePlaneTriangle(Line3 line, Plane plane, Vector3d result) {
        return scaleLine(line, intersectionLinePlaneTriangle(line, plane), result);
    }

    public static double intersectionLinePlane(Line3 line, Plane plane) {
        Vector3d result = new Vector3d();
        intersectionLinePlaneTUV(line, plane, result);
        double t = result.x, u = result.y, v = result.z;
        if (t < 0 || t > 1 || u < 0 || u > 1 || v < 0 || v > 1) {
            return -1;
        }

        return t;
    }
    public static boolean intersectionLinePlane(Line3 line, Plane plane, Vector3d result) {
        return scaleLine(line, intersectionLinePlane(line, plane), result);
    }

    public static double intersectionLineCylinder(Line3 line, Cylinder cylinder) {
        if (line.displacement.lengthSquared() == 0) {
            return -1;
        }
        // Rotate everything such that the cylinder's axis lies on the z-axis
        // Only works for axis-aligned cylinders (too lazy to figure out the proper way of doing this)
        Line3 rotatedLine = new Line3(line);
        Line3 rotatedCylinderAxis = new Line3(cylinder.position, cylinder.axis);
        if (cylinder.axis.z == 0) {
            Matrix3d rotationMatrix = new Matrix3d();
            if (cylinder.axis.x == 0) {
                rotationMatrix.rotationX(Math.PI/2);
            } else if (cylinder.axis.y == 0) {
                rotationMatrix.rotationY(Math.PI/2);
            } else {
                rotationMatrix.rotationZ(cylinder.axis.angle(xHat));
                rotationMatrix.rotateY(Math.PI/2);
            }
            rotatedLine.position.mul(rotationMatrix);
            rotatedLine.displacement.mul(rotationMatrix);
            rotatedCylinderAxis.position.mul(rotationMatrix);
            rotatedCylinderAxis.displacement.mul(rotationMatrix);
        }

        // Project the rotated line and cylinder to the XY plane
        Line2 lineXY = new Line2(
                new Vector2d(rotatedLine.position.x, rotatedLine.position.y),
                new Vector2d(rotatedLine.displacement.x, rotatedLine.displacement.y)
        );
        if (lineXY.displacement.lengthSquared() == 0) {
            return -1;
        }
        Vector2d cylinderPositionXY = new Vector2d(rotatedCylinderAxis.position.x, rotatedCylinderAxis.position.y);
        Circle circle = new Circle(cylinder.radius, cylinderPositionXY);

        // Find the intersection of the projected lines and cylinder (now a circle)
        double t = intersectionLineCircle(lineXY, circle);
        if (t == -1) {
            return -1;
        }

        // Check if the intersection is on the cylinder (in the z-direction)
        double intersectionZ = rotatedLine.position.z + t*rotatedLine.displacement.z;
        double bottom = rotatedCylinderAxis.position.z;
        double top = bottom + rotatedCylinderAxis.displacement.z;
        if (!withinRange(intersectionZ, bottom, top)) {
            return -1;
        }

        return t;
    }
    public static boolean intersectionLineCylinder(Line3 line, Cylinder cylinder, Vector3d result) {
        return scaleLine(line, intersectionLineCylinder(line, cylinder), result);
    }

    public static double intersectionLineSphere(Line3 line, Sphere sphere) {
        if (line.displacement.lengthSquared() == 0) {
            return -1;
        }
        Vector3d lineToSphere = new Vector3d(line.position).sub(sphere.position);
        Quadratic q = new Quadratic(
                line.displacement.lengthSquared(),
                2 * line.displacement.dot(lineToSphere),
                lineToSphere.lengthSquared() - sphere.getRadius()*sphere.getRadius()
        );
        if (q.discriminant() < 0) {
            return -1;
        }
        double t = minNonNegativeClipped(q.solution1(), q.solution2());
        if (t < 0 || t > 1) {
            return -1;
        }
        return t;
    }
    public static boolean intersectionLineSphere(Line3 line, Sphere sphere, Vector3d result) {
        return scaleLine(line, intersectionLineSphere(line, sphere), result);
    }

    public static double intersectionLineCircle(Line2 line, Circle circle) {
        if (line.displacement.lengthSquared() == 0) {
            return -1;
        }
        Vector2d lineToCircle = new Vector2d(line.position).sub(circle.position);
        Quadratic q = new Quadratic(
                line.displacement.lengthSquared(),
                2*line.displacement.dot(lineToCircle),
                lineToCircle.lengthSquared() - circle.getRadius()*circle.getRadius()
        );
        if (q.discriminant() < 0) {
            return -1;
        }
        double t = minNonNegativeClipped(q.solution1(), q.solution2());
        if (t < 0 || t > 1) {
            return -1;
        }
        return t;
    }
    public static boolean intersectionLineCircle(Line2 line, Circle circle, Vector2d result) {
        return scaleLine(line, intersectionLineCircle(line, circle), result);
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
        return Math.hypot(circle.position.x-point.x, circle.position.y-point.y) - circle.getRadius();
    }
    public static double distance(Vector2d a, Vector2d b) {
        return Math.hypot(b.x-a.x, b.y-a.y);
    }
    public static double distance(Vector3d a, Vector3d b) {
        return Math.sqrt((b.x-a.x)*(b.x-a.x) + (b.y-a.y)*(b.y-a.y) + (b.z-a.z)*(b.z-a.z));
    }
}
