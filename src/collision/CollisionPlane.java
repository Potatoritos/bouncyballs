package collision;

import game.GameObject;
import shape.Line3d;
import shape.Plane;
import shape.Sphere;
import org.joml.Vector3d;

import static math.Geometry.*;

/**
 * Defines the collision properties of a plane
 */
public class CollisionPlane extends CollisionObject {
    protected final Plane plane;
    private final Vector3d midpoint;
    public CollisionPlane(GameObject parent, Plane plane) {
        super(parent);
        this.plane = new Plane(plane);
        midpoint = new Vector3d(plane.displacement1).mul(0.5).add(new Vector3d(plane.displacement2).mul(0.5)).add(plane.position);
    }

    @Override
    public void reflectLine(Line3d line, Vector3d intersection) {
        parent.reflectLine(line, intersection, plane.normal());
    }

    @Override
    public boolean isNearby(Sphere ballSphere) {
        return distance(midpoint, ballSphere.position) <= ballSphere.getRadius() + Math.max(plane.displacement1.length(), plane.displacement2.length());
    }

    @Override
    public boolean intersect(Line3d line, Vector3d result) {
        // Return false if line.displacement is moving away from the surface (i.e, when it is within 90° of the normal)
        if (plane.normal().dot(line.displacement) > 0) {
            return false;
        }

        return intersectionLinePlane(line, plane, result);
    }
}
