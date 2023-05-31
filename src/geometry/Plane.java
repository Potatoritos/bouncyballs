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
    public Plane(Vector3d pos, Vector3d displacement1, Vector3d displacement2) {
        this();
        this.position.set(pos);
        this.displacement1.set(displacement1);
        this.displacement2.set(displacement2);
    }
}
