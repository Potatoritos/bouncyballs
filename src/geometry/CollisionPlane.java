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
        return distance(midpoint, ballSphere.position) <= ballSphere.getRadius() + Math.max(plane.displacement1.length(), plane.displacement2.length()) + 0.2;
    }

    @Override
    public boolean intersect(Line3 line, Vector3d result) {
        return intersectionLinePlane(line, plane, result);
    }
}
