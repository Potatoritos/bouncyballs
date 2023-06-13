package geometry;

import org.joml.Vector3d;

public class Plane {
    public final Vector3d position;
    public final Vector3d displacement1;
    public final Vector3d displacement2;
    public Plane() {
        position = new Vector3d();
        displacement1 = new Vector3d();
        displacement2 = new Vector3d();
    }
    public Plane(Vector3d position, Vector3d displacement1, Vector3d displacement2) {
        this();
        set(position, displacement1, displacement2);
    }
    public Plane(Plane plane) {
        this();
        set(plane);
    }
    public void set(Vector3d position, Vector3d displacement1, Vector3d displacement2) {
        this.position.set(position);
        this.displacement1.set(displacement1);
        this.displacement2.set(displacement2);
    }
    public void set(Plane plane) {
        set(plane.position, plane.displacement1, plane.displacement2);
    }

    public Vector3d getNormal() {
        Vector3d normal = new Vector3d(displacement1);
        normal.cross(displacement2);
        return normal;
    }
}
