package geometry;

import org.joml.Vector3d;

public class Sphere {
    private double radius;
    public final Vector3d position;
    public Sphere() {
        radius = 0;
        position = new Vector3d();
    }
    public Sphere(Vector3d position, double radius) {
        this();
        this.position.set(position);
        this.radius = radius;
    }
    public Sphere(Sphere sphere) {
        this(sphere.position, sphere.radius);
    }
    public double getRadius() {
        return radius;
    }
}
