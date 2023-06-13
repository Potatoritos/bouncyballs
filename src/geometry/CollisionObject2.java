package geometry;

import game.GameObject;
import org.joml.Vector2d;

public abstract class CollisionObject2 {
    public final GameObject parent;
    public CollisionObject2(GameObject parent) {
        this.parent = parent;
    }
    public abstract void reflectLine(Line2 line, Vector2d intersection, double length);
    public abstract double distance(Vector2d point);
    public abstract boolean intersect(Line2 line, Vector2d result);
}
