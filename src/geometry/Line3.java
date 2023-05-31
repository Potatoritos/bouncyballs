package geometry;

import org.joml.Vector3d;

public class Line3 {
    public final Vector3d position;
    public final Vector3d displacement;
    public Line3() {
        this.position = new Vector3d();
        this.displacement = new Vector3d();
    }
    public Line3(Vector3d position, Vector3d displacement) {
        this();
        this.position.set(position);
        this.displacement.set(displacement);
    }
    public double x1() {
        return position.x;
    }
    public double y1() {
        return position.y;
    }
    public double z1() {
        return position.z;
    }
    public double x2() {
        return position.x + displacement.x;
    }
    public double y2() {
        return position.y + displacement.y;
    }
    public double z2() {
        return position.z + displacement.z;
    }
}
