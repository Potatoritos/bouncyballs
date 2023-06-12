package geometry;

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
        this.radius = radius;
        this.position.set(position);
    }
    public Circle(Circle circle) {
        this(circle.radius, circle.position);
    }
    public double getRadius() {
        return radius;
    }
    public void setRadius(double value) {
        radius = value;
    }
    public String toString() {
        return "Circle pos=" + position + " r=" + radius;
    }
}
