package collision;

import game.GameObject;
import shape.Line3;
import shape.Plane;
import org.joml.Vector3d;

import static math.Geometry.*;

public class CollisionPlaneTriangle extends CollisionPlane {
    public CollisionPlaneTriangle(GameObject parent, Plane plane) {
        super(parent, plane);
    }

    @Override
    public boolean intersect(Line3 line, Vector3d result) {
        // Return false if line.displacement is moving away from the surface (i.e, when it is within 90° of the normal)
        if (plane.normal().dot(line.displacement) > 0) {
            return false;
        }

        return intersectionLinePlaneTriangle(line, plane, result);
    }
}
