package geometry;

import game.GameObject;
import org.joml.Vector2d;
import org.joml.Vector3d;

public abstract class CollisionObject3 {
    public final GameObject parent;
    public CollisionObject3(GameObject parent) {
        this.parent = parent;
    }
    public abstract void reflectLine(Line3 line, Vector3d intersection, double length);
    public abstract boolean isNearby(Sphere ballSphere);
    public abstract boolean intersect(Line3 line, Vector3d result);
}
