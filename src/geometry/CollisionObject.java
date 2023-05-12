package geometry;

import game.GameObject;
import org.joml.Vector2d;

public abstract class CollisionObject {
    public final GameObject parent;
    public CollisionObject(GameObject parent) {
        this.parent = parent;
    }
    public abstract void reflectLine(Line line, Vector2d intersection, double length);
    public abstract double distance(Vector2d point);
    public abstract boolean intersect(Line line, Vector2d result);
}
