package geometry;

import game.GameObject;
import org.joml.Vector2d;

import static geometry.Geometry.*;

public class CollisionLineX extends CollisionObject {
    public final Line line;
    public final double directionSign;
    public CollisionLineX(GameObject parent, Vector2d position, Vector2d displacement, double directionSign) {
        super(parent);
        line = new Line(position, displacement);
        this.directionSign = directionSign;
    }
    public void reflectLine(Line line, Vector2d intersection, double length) {
        Vector2d reflection = new Vector2d(-line.displacement.x/2, line.displacement.y); //.normalize(line.displacement.length() - length);
        line.position.set(intersection);
        line.displacement.set(reflection);
    }
    public double distance(Vector2d point) {
        return distanceLineSegmentPoint(line, point);
    }
    public boolean intersect(Line line, Vector2d result) {
        if (Math.signum(line.displacement.x) == directionSign)
            return intersectionLineWallX(line, this.line, result);
        else {
            if (!intersectionLineWallX(line, this.line, result))
                System.out.println("no x");
            return false;
        }
    }
}
