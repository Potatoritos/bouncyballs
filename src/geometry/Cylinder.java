package geometry;

import org.joml.Vector3d;

public class Cylinder {
    public final Vector3d position;
    public final Vector3d axis;
    public double radius;
    public Cylinder() {
        position = new Vector3d();
        axis = new Vector3d();
        radius = 0;
    }
    public Cylinder(Vector3d position, Vector3d axis, double radius) {
        this();
        this.position.set(position);
        this.axis.set(axis);
        this.radius = radius;
    }
    public Cylinder(Cylinder cylinder) {
        this(cylinder.position, cylinder.axis, cylinder.radius);
    }
    public double getRadius() {
        return radius;
    }
    public void setRadius(double value) {
        radius = value;
    }
    public String toString() {
        return "Cylinder pos=" + position + " axis=" + axis + " r=" + radius;
    }
}
