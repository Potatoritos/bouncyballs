package geometry;

import game.GameObject;
import org.joml.Vector3d;

import static geometry.Geometry.*;

public class CollisionPlane extends CollisionObject3 {
    private final Plane plane;
    public CollisionPlane(GameObject parent, Plane plane) {
        super(parent);
        this.plane = plane;
    }

    @Override
    public void reflectLine(Line3 line, Vector3d intersection, double length) {
        Vector3d parallel1 = new Vector3d(), parallel2 = new Vector3d();
        project(line.displacement, plane.displacement1, parallel1);
        project(line.displacement, plane.displacement2, parallel2);
        Vector3d normal = plane.getNormal();
        project(line.displacement, normal, normal);
        normal.negate().mul(0.5);

        line.displacement.set(parallel1).add(parallel2).add(normal);
        line.position.set(intersection);
    }

    @Override
    public double approximateDistance(Vector3d point) {
        return distance(plane.position, point) - Math.max(plane.displacement1.length(), plane.displacement2.length());
    }

    @Override
    public boolean intersect(Line3 line, Vector3d result) {
        return intersectionLinePlane(line, plane, result);
    }
}
