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
        Geometry.reflectLine(line, intersection, sphere.normal(intersection), 0.5);
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
