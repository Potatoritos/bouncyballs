package math;

import math.Quadratic;
import org.joml.Matrix3d;
import org.joml.Vector2d;
import org.joml.Vector3d;
import shape.*;

import static math.MathUtil.*;

/**
 * A collection of geometry-related math functions
 */
public class Geometry {
    /**
     * Stores the projection of a onto b in result
     * @param result the result. Must not be the same variable as a
     */
    public static void project(Vector3d a, Vector3d b, Vector3d result) {
        result.set(b);
        result.mul(a.dot(b) / b.lengthSquared());
    }
    /**
     * Stores the projection of a onto b in result
     * @param result the result. Must not be the same variable as a
     */
    public static void project(Vector2d a, Vector2d b, Vector2d result) {
        result.set(b);
        result.mul(a.dot(b) / b.lengthSquared());
    }

    /**
     * Reflects a line off of a surface
     * @param line the line
     * @param intersection the point at which the line intersects the surface
     * @param normal the normal of the surface at the point of intersection
     * @param restitution the ratio between the initial and final "velocity" of the line after hitting the surface
     */
    public static void reflectLine(Line3 line, Vector3d intersection, Vector3d normal, double restitution) {
        Vector3d normalComponent = new Vector3d();
        project(line.displacement, normal, normalComponent);

        // Nullify the rebound if it is small
        if (normalComponent.length()*restitution <= 0.0018) {
            restitution = 0;
        }

        line.displacement.sub(normalComponent.mul(1 + restitution));
        line.position.set(intersection);
    }

    /**
     * Reflects a line off of a surface with a fixed final velocity
     * @param line the line
     * @param intersection the point at which the line intersects the surface
     * @param normal the normal of the surface at the point of intersection
     * @param reboundVelocity the velocity of the line after hitting the surface
     */
    public static void reflectLineFixedRebound(Line3 line, Vector3d intersection, Vector3d normal, double reboundVelocity) {
        Vector3d normalComponent = new Vector3d();
        project(line.displacement, normal, normalComponent);
        line.displacement.sub(normalComponent.mul(1 + reboundVelocity/normalComponent.length()));
        line.position.set(intersection);
    }

    /**
     * Sets result to a position along a line
     * @param t the position's distance along the line (a value in [0,1])
     * @return true if t is not -1; false otherwise
     */
    public static boolean scaleLine(Line3 line, double t, Vector3d result) {
        if (t == -1) return false;
        result.set(line.displacement).mul(t).add(line.position);
        return true;
    }

    /**
     * Sets result to a position along a line
     * @param t the position's distance along the line (a value in [0,1])
     * @return true if t is not -1; false otherwise
     */
    public static boolean scaleLine(Line2 line, double t, Vector2d result) {
        if (t == -1) return false;
        result.set(line.displacement).mul(t).add(line.position);
        return true;
    }

    /**
     * Finds the intersection between a line and a plane.
     * Sets result to (t, u, v);
     * t: the POI's distance along the line;
     * u, v: the POI's distance along the plane's direction vectors.
     * Sets t to -1 if there is no intersection.
     */
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

    /**
     * Finds the intersection between a line and the triangle defined by a plane's postition and its two direction vectors
     * @return the POI's distance along the line; -1 if there is no intersection
     */
    public static double intersectionLinePlaneTriangle(Line3 line, Plane plane) {
        Vector3d result = new Vector3d();
        intersectionLinePlaneTUV(line, plane, result);
        double t = result.x, u = result.y, v = result.z;
        if (t < 0 || t > 1 || u < 0 || u > 1 || v < 0 || v > 1 || (u+v) > 1) {
            return -1;
        }
        return t;
    }

    /**
     * Stores into result the intersection between a line and the triangle defined by a plane's postition and its two direction vectors
     * @return true if there exists a POI; false otherwise
     */
    public static boolean intersectionLinePlaneTriangle(Line3 line, Plane plane, Vector3d result) {
        return scaleLine(line, intersectionLinePlaneTriangle(line, plane), result);
    }

    /**
     * Finds the intersection between a line and a plane
     * @return the POI's distance along the line; -1 if there is no intersection
     */
    public static double intersectionLinePlane(Line3 line, Plane plane) {
        Vector3d result = new Vector3d();
        intersectionLinePlaneTUV(line, plane, result);
        double t = result.x, u = result.y, v = result.z;
        if (t < 0 || t > 1 || u < 0 || u > 1 || v < 0 || v > 1) {
            return -1;
        }

        return t;
    }

    /**
     * Stores the point of intersection between a line and a plane into result
     * @return true if there is a POI; false otherwise
     */
    public static boolean intersectionLinePlane(Line3 line, Plane plane, Vector3d result) {
        return scaleLine(line, intersectionLinePlane(line, plane), result);
    }

    /**
     * Finds the intersection between a line and a cylinder
     * @return the POI's distance along the line
     */
    public static double intersectionLineCylinder(Line3 line, Cylinder cylinder) {
        if (line.displacement.lengthSquared() == 0) {
            return -1;
        }
        // Rotate everything such that the cylinder's axis lies on the z-axis
        // Only works for cylinders that are aligned to the X axis, Y axis, Z axis, or XY plane (too lazy to figure out the proper way of doing this)
        Line3 rotatedLine = new Line3(line);
        Line3 rotatedCylinderAxis = new Line3(cylinder.position, cylinder.axis);
        if (cylinder.axis.z == 0) {
            Matrix3d rotationMatrix = new Matrix3d();
            if (cylinder.axis.x == 0) {
                rotationMatrix.rotationX(Math.PI/2);
            } else if (cylinder.axis.y == 0) {
                rotationMatrix.rotationY(Math.PI/2);
            } else {
                rotationMatrix.rotationZ(cylinder.axis.angle(new Vector3d(1, 0, 0)));
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

    /**
     * Stores the intersection between a line and a cylinder into result
     * @return true if there exists a POI; false otherwise
     */
    public static boolean intersectionLineCylinder(Line3 line, Cylinder cylinder, Vector3d result) {
        return scaleLine(line, intersectionLineCylinder(line, cylinder), result);
    }

    /**
     * Finds the intersection between a line and a sphere
     * @return the POI's distance along the line
     */
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

    /**
     * Stores the intersection between a line and a sphere into result
     * @return true if a POI exists; false otherwise
     */
    public static boolean intersectionLineSphere(Line3 line, Sphere sphere, Vector3d result) {
        return scaleLine(line, intersectionLineSphere(line, sphere), result);
    }

    /**
     * Finds the intersection between a line and a circle
     * @return the POI's distance along the line
     */
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

    /**
     * Stores the intersection between a line and a circle into result
     * @return true if a POI exists; false otherwise
     */
    public static boolean intersectionLineCircle(Line2 line, Circle circle, Vector2d result) {
        return scaleLine(line, intersectionLineCircle(line, circle), result);
    }

    /**
     * Finds the distance from a line segment to a point
     * @return the distance
     */
    public static double distanceLineSegmentPoint(Line2 line, Vector2d point) {
        double l = line.displacement.lengthSquared();
        if (l == 0) return distance(line.position, point);
        Vector2d u = new Vector2d();
        point.sub(line.position, u);
        double t = Math.max(0, Math.min(1, u.dot(line.displacement) / l));
        u.set(line.displacement).mul(t).add(line.position);
        return distance(u, point);
    }

    /**
     * Finds the distance from a circle to a point
     * @return the distance
     */
    public static double distanceCirclePoint(Circle circle, Vector2d point) {
        return Math.hypot(circle.position.x-point.x, circle.position.y-point.y) - circle.getRadius();
    }

    /**
     * Finds the distance between two points
     * @return the distance
     */
    public static double distance(Vector2d a, Vector2d b) {
        return Math.hypot(b.x-a.x, b.y-a.y);
    }

    /**
     * Finds the distance between two points
     * @return the distance
     */
    public static double distance(Vector3d a, Vector3d b) {
        return Math.sqrt((b.x-a.x)*(b.x-a.x) + (b.y-a.y)*(b.y-a.y) + (b.z-a.z)*(b.z-a.z));
    }
}
