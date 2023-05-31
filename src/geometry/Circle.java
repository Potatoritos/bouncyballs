package geometry;

import org.joml.Vector2d;

public class Circle {
    public final double radius;
    public final Vector2d position;
    public Circle(double radius, Vector2d position) {
        this.radius = radius;
        this.position = new Vector2d().set(position);
    }
}
