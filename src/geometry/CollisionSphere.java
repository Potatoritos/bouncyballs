package geometry;

import game.GameObject;
import org.joml.Vector3d;

import static geometry.Geometry.*;

public class CollisionSphere extends CollisionObject3 {
    private final Sphere sphere;
    public CollisionSphere(GameObject parent, Sphere sphere) {
        super(parent);
        this.sphere = sphere;
    }

    @Override
    public void reflectLine(Line3 line, Vector3d intersection, double length) {
        Vector3d normal = new Vector3d(intersection);
        normal.sub(sphere.position);
        Vector3d parallel1 = new Vector3d(-(normal.y + normal.z)/normal.x, 1, 1);
        Vector3d parallel2 = new Vector3d(normal).cross(parallel1);

        project(line.displacement, parallel1, parallel1);
        project(line.displacement, parallel2, parallel2);
        project(line.displacement, normal, normal);
        normal.negate().mul(0.5);

        line.displacement.set(parallel1).add(parallel2).add(normal);
        line.position.set(intersection);
    }

    @Override
    public double approximateDistance(Vector3d point) {
        return distance(sphere.position, point) - sphere.getRadius();
    }

    @Override
    public boolean intersect(Line3 line, Vector3d result) {
        return intersectionLineSphere(line, sphere, result);
    }
}
