package collision;

import game.GameObject;
import shape.Cylinder;
import shape.Line3;
import org.joml.Vector3d;

import static math.Geometry.intersectionLineCylinder;

/**
 * Defines the collision properties of the inside of a cylinder
 */
public class CollisionCylinderInverted extends CollisionCylinder {
    public CollisionCylinderInverted(GameObject parent, Cylinder cylinder) {
        super(parent, cylinder);
    }

    @Override
    public boolean intersect(Line3 line, Vector3d result) {
        boolean intersects = intersectionLineCylinder(line, cylinder, result);
        return intersects && cylinder.normal(result).dot(line.displacement) >= 0;
    }
}
