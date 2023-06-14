package collision;

import game.GameObject;
import geometry.Geometry;
import geometry.Line3;
import geometry.Sphere;
import org.joml.Vector3d;

import static geometry.Geometry.*;

public class CollisionSphere extends CollisionObject3 {
    private final Sphere sphere;
    public CollisionSphere(GameObject parent, Sphere sphere) {
        super(parent);
        this.sphere = new Sphere(sphere);
    }

    @Override
    public void reflectLine(Line3 line, Vector3d intersection, double length) {
        Vector3d normal = sphere.normal(intersection);
        Vector3d parallel1 = new Vector3d(-(normal.y + normal.z)/normal.x, 1, 1);
        Vector3d parallel2 = new Vector3d(normal).cross(parallel1);

        Geometry.reflectLine(line, intersection, normal, parallel1, parallel2, 0.5);
    }

    @Override
    public boolean isNearby(Sphere ballSphere) {
        return distance(sphere.position, ballSphere.position) <= sphere.getRadius() + ballSphere.getRadius() + 0.2;
    }

    @Override
    public boolean intersect(Line3 line, Vector3d result) {
        boolean intersects = intersectionLineSphere(line, sphere, result);
        return intersects && sphere.normal(result).dot(line.displacement) < 0;
    }
}