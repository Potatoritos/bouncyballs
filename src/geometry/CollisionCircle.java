package geometry;

import game.GameObject;
import org.joml.Vector2d;

import static geometry.Geometry.distanceCirclePoint;
import static geometry.Geometry.intersectionLineCircle;

public class CollisionCircle extends CollisionObject {
    private final Circle circle;
    public CollisionCircle(GameObject parent, double radius, Vector2d position) {
        super(parent);
        circle = new Circle(radius, position);
    }
    public void reflectLine(Line line, Vector2d intersection, double length) {

    }
    public double distance(Vector2d point) {
        return distanceCirclePoint(circle, point);
    }
    public boolean intersect(Line line, Vector2d result) {
        return intersectionLineCircle(line, circle, result);
    }
}
