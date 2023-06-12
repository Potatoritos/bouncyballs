package geometry;

import org.joml.Vector3d;

public class Cylinder {
    public final Vector3d position;
    public final Vector3d axis;
    public double radius;
    public Cylinder(Vector3d position, Vector3d axis, double radius) {
        this.position = position;
        this.axis = axis;
        this.radius = radius;
    }
    public double getRadius() {
        return radius;
    }
    public String toString() {
        return "Cylinder pos=" + position + " axis=" + axis + " r=" + radius;
    }
}
