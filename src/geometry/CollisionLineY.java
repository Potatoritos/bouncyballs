package geometry;

import game.GameObject;
import org.joml.Vector2d;

import static geometry.Geometry.*;

public class CollisionLineY extends CollisionObject2 {
    public final Line2 line;
    public final double directionSign;
    public CollisionLineY(GameObject parent, Vector2d position, Vector2d displacements, double directionSign) {
        super(parent);
        line = new Line2(position, displacements);
        this.directionSign = directionSign;
    }
    public void reflectLine(Line2 line, Vector2d intersection, double length) {
        Vector2d reflection = new Vector2d(line.displacement.x, -line.displacement.y/2); //.normalize(line.displacement.length() - length);
        line.position.set(intersection);
        line.displacement.set(reflection);
    }
    public double distance(Vector2d point) {
        return distanceLineSegmentPoint(line, point);
    }
    public boolean intersect(Line2 line, Vector2d result) {
        if (Math.signum(line.displacement.y) == directionSign)
            return intersectionLineWallY(line, this.line, result);
        else {
            return false;
        }
    }
}
