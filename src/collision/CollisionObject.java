package collision;

import game.GameObject;
import shape.Line3;
import shape.Sphere;
import org.joml.Vector3d;

/**
 * Defines the collision properties of a shape for use in CollisionHandler
 */
public abstract class CollisionObject implements HasCollisionObject {
    public final GameObject parent;
    public CollisionObject(GameObject parent) {
        this.parent = parent;
    }

    /**
     * Denotes the function used to reflect a line off of the shape
     * @param line the line
     * @param intersection the point of intersection between the line and the shape
     */
    public abstract void reflectLine(Line3 line, Vector3d intersection);

    /**
     * Determines, very approximately (may overestimate by a lot),
     * whether a ball is close enough to intersect the shape
     * @param ballSphere the geometry of the ball
     */
    public abstract boolean isNearby(Sphere ballSphere);

    /**
     * Denotes the function used to find the intersection between a line and the shape
     * @param line the line
     * @param result the vector to store the point of intersection in
     */
    public abstract boolean intersect(Line3 line, Vector3d result);
    public CollisionObject getCollisionObject() {
        return this;
    }
}
