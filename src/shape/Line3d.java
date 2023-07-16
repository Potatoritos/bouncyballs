package shape;

import org.joml.Vector3d;

/**
 * Represents a 3-D line defined by a position and a direction
 */
public class Line3d {
    public final Vector3d position;
    public final Vector3d displacement;
    public Line3d() {
        this.position = new Vector3d();
        this.displacement = new Vector3d();
    }
    public Line3d(Vector3d position, Vector3d displacement) {
        this();
        set(position, displacement);
    }
    public Line3d(Line3d line) {
        this();
        set(line);
    }
    public void set(Vector3d position, Vector3d displacement) {
        this.position.set(position);
        this.displacement.set(displacement);
    }
    public void set(Line3d line) {
        set(line.position, line.displacement);
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
    public String toString() {
        return String.format("[Line2 pos=%s d=%s]", position, displacement);
    }
}
