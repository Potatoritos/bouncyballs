package collision;

import game.GameObject;
import shape.Line3;
import shape.Sphere;
import org.joml.Vector3d;

import static math.Geometry.*;

/**
 * Defines the collision properties of a sphere
 */
public class CollisionSphere extends CollisionObject {
    private final Sphere sphere;
    public CollisionSphere(GameObject parent, Sphere sphere) {
        super(parent);
        this.sphere = new Sphere(sphere);
    }

    @Override
    public void reflectLine(Line3 line, Vector3d intersection) {
        parent.reflectLine(line, intersection, sphere.normal(intersection));
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
