package shape;

import org.joml.Vector3f;

/**
 * Represents a 3-D line defined by a position and a direction
 */
public class Line3f {
    public final Vector3f position;
    public final Vector3f displacement;
    public Line3f() {
        this.position = new Vector3f();
        this.displacement = new Vector3f();
    }
    public Line3f(Vector3f position, Vector3f displacement) {
        this();
        set(position, displacement);
    }
    public Line3f(Line3f line) {
        this();
        set(line);
    }
    public void set(Vector3f position, Vector3f displacement) {
        this.position.set(position);
        this.displacement.set(displacement);
    }
    public void set(Line3f line) {
        set(line.position, line.displacement);
    }
    public float x1() {
        return position.x;
    }
    public float y1() {
        return position.y;
    }
    public float z1() {
        return position.z;
    }
    public float x2() {
        return position.x + displacement.x;
    }
    public float y2() {
        return position.y + displacement.y;
    }
    public float z2() {
        return position.z + displacement.z;
    }
    public String toString() {
        return String.format("[Line2 pos=%s d=%s]", position, displacement);
    }
}
