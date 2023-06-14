package collision;

import game.GameObject;
import geometry.Cylinder;
import geometry.Line3;
import org.joml.Vector3d;

import static geometry.Geometry.intersectionLineCylinder;

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
