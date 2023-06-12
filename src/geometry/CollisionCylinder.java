package geometry;

import game.GameObject;
import org.joml.Vector3d;

public class CollisionCylinder extends CollisionObject3 {
    private final Cylinder cylinder;
    public CollisionCylinder(GameObject parent, Cylinder cylinder) {
        super(parent);
        this.cylinder = cylinder;
    }

    @Override
    public void reflectLine(Line3 line, Vector3d intersection, double length) {
        
    }

    @Override
    public double approximateDistance(Vector3d point) {
        return 0;
    }

    @Override
    public boolean intersect(Line3 line, Vector3d result) {
        return false;
    }
}
