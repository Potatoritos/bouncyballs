package geometry;

import org.joml.Vector2d;

public class Line {
    public final Vector2d position;
    public final Vector2d displacement;
    public Line() {
        this.position = new Vector2d();
        this.displacement = new Vector2d();
    }
    public Line(Vector2d position, Vector2d displacement) {
        this.position = position;
        this.displacement = displacement;
    }
    public double x1() {
        return position.x;
    }
    public double y1() {
        return position.y;
    }
    public double x2() {
        return position.x + displacement.x;
    }
    public double y2() {
        return position.y + displacement.y;
    }
}
