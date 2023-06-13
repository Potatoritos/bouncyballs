package geometry;

import game.GameObject;
import org.joml.Vector3d;

import static geometry.Geometry.*;

public class CollisionPlane extends CollisionObject3 {
    private final Plane plane;
    private final Vector3d midpoint;
    public CollisionPlane(GameObject parent, Plane plane) {
        super(parent);
        this.plane = new Plane(plane);
        midpoint = new Vector3d(plane.displacement1).mul(0.5).add(new Vector3d(plane.displacement2).mul(0.5)).add(plane.position);
    }

    @Override
    public void reflectLine(Line3 line, Vector3d intersection, double length) {
        Vector3d parallel1 = new Vector3d(plane.displacement1);
        Vector3d parallel2 = new Vector3d(plane.displacement2);
        Vector3d normal = plane.getNormal();
        Geometry.reflectLine(line, intersection, normal, parallel1, parallel2, 0.5);
    }

    @Override
    public boolean isNearby(Sphere ballSphere) {
        return distance(midpoint, ballSphere.position) <= ballSphere.getRadius() + Math.max(plane.displacement1.length(), plane.displacement2.length());
    }

    @Override
    public boolean intersect(Line3 line, Vector3d result) {
        // Return false if line.displacement is moving away from the surface (i.e, when it is within 90° of the normal)
        if (plane.getNormal().dot(line.displacement) > 0) {
            return false;
        }

        return intersectionLinePlane(line, plane, result);
    }

    public String toString() {
        return plane.toString();
    }
}
