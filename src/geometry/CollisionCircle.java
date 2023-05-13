package geometry;

import game.GameObject;
import org.joml.Vector2d;

import static geometry.Geometry.*;

public class CollisionCircle extends CollisionObject {
    private final Circle circle;
    public CollisionCircle(GameObject parent, double radius, Vector2d position) {
        super(parent);
        circle = new Circle(radius, position);
    }
    public void reflectLine(Line line, Vector2d intersection, double length) {
        Vector2d normal = new Vector2d();
        intersection.sub(circle.position, normal).normalize();
        Vector2d reflectionNormal = new Vector2d();
        project(line.displacement, normal, reflectionNormal);
        Vector2d reflectionTangent = new Vector2d();
        project(line.displacement, normal.perpendicular(), reflectionTangent);

        line.position.set(intersection);
        line.displacement.set(reflectionNormal.negate().mul(0.5)).add(reflectionTangent);
    }
    public double distance(Vector2d point) {
        return distanceCirclePoint(circle, point);
    }
    public boolean intersect(Line line, Vector2d result) {
        boolean intersects = intersectionLineCircle(line, circle, result);
        if (!intersects) return false;
        Vector2d normal = new Vector2d();
        result.sub(circle.position, normal).normalize();
        return Math.abs(line.displacement.angle(normal)) >= Math.PI/2;
    }
}
