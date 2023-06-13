package geometry;

import org.joml.Vector3d;

import static geometry.Geometry.project;

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
        set(position, axis, radius);
    }
    public Cylinder(Cylinder cylinder) {
        this();
        set(cylinder);
    }
    public void set(Vector3d position, Vector3d axis, double radius) {
        this.position.set(position);
        this.axis.set(axis);
        this.radius = radius;
    }
    public void set(Cylinder cylinder) {
        set(cylinder.position, cylinder.axis, cylinder.radius);
    }
    public double getRadius() {
        return radius;
    }
    public void setRadius(double value) {
        radius = value;
    }
    // Get the normal vector of a point on the cylinder
    public Vector3d normal(Vector3d point) {
        Vector3d u = new Vector3d(point).sub(position);
        Vector3d projection = new Vector3d();
        project(u, axis, projection);
        projection.add(position);
        return u.set(point).sub(projection);
    }
    public String toString() {
        return String.format("[Cylinder pos=%s axis=%s r=%s]", position, axis, radius);
    }
}
