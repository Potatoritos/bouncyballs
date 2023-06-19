package shape;

import org.joml.Vector3d;

/**
 * Represents a sphere defined by a position and a radius
 */
public class Sphere {
    private double radius;
    public final Vector3d position;
    public Sphere() {
        radius = 0;
        position = new Vector3d();
    }
    public Sphere(Vector3d position, double radius) {
        this();
        set(position, radius);
    }
    public Sphere(Sphere sphere) {
        this();
        set(sphere);
    }
    public void set(Vector3d position, double radius) {
        this.position.set(position);
        this.radius = radius;
    }
    public void set(Sphere sphere) {
        set(sphere.position, sphere.radius);
    }
    public double getRadius() {
        return radius;
    }
    public void setRadius(double value) {
        radius = value;
    }
    /**
     * Get the normal vector of a point on the sphere
     * @param point the point on the sphere
     * @return the vector
     */
    public Vector3d normal(Vector3d point) {
        return new Vector3d(point).sub(position);
    }
    public String toString() {
        return String.format("[Sphere pos=%s r=%s]", position, radius);
    }
}
