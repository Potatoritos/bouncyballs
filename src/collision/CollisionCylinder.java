package collision;

import game.GameObject;
import shape.Cylinder;
import shape.Line3;
import shape.Sphere;
import org.joml.Vector3d;

import static math.Geometry.*;

public class CollisionCylinder extends CollisionObject3 {
    protected final Cylinder cylinder;
    private final Vector3d midpoint;
    public CollisionCylinder(GameObject parent, Cylinder cylinder) {
        super(parent);
        this.cylinder = new Cylinder(cylinder);
        midpoint = new Vector3d(cylinder.axis).mul(0.5).add(cylinder.position);
    }

    @Override
    public void reflectLine(Line3 line, Vector3d intersection, double length) {
        // Project the intersection onto the cylinder's axis
        parent.reflectLine(line, intersection, cylinder.normal(intersection));
    }

    @Override
    public boolean isNearby(Sphere ballSphere) {
        return distance(midpoint, ballSphere.position) <= ballSphere.getRadius() + cylinder.axis.length() + cylinder.getRadius();
    }

    @Override
    public boolean intersect(Line3 line, Vector3d result) {
        boolean intersects = intersectionLineCylinder(line, cylinder, result);
        return intersects && cylinder.normal(result).dot(line.displacement) < 0;
    }
}
