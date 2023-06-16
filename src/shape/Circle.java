package shape;

import org.joml.Vector2d;

public class Circle {
    private double radius;
    public final Vector2d position;
    public Circle() {
        radius = 0;
        position = new Vector2d();
    }
    public Circle(double radius, Vector2d position) {
        this();
        set(radius, position);
    }
    public Circle(Circle circle) {
        this();
        set(circle);
    }
    public void set(double radius, Vector2d position) {
        this.radius = radius;
        this.position.set(position);
    }
    public void set(Circle circle) {
        set(circle.radius, circle.position);
    }
    public double getRadius() {
        return radius;
    }
    public void setRadius(double value) {
        radius = value;
    }
    public String toString() {
        return String.format("[Circle pos=%s r=%s]", position, radius);
    }
}
