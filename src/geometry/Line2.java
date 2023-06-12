package geometry;

import org.joml.Vector2d;

public class Line2 {
    public final Vector2d position;
    public final Vector2d displacement;
    public Line2() {
        this.position = new Vector2d();
        this.displacement = new Vector2d();
    }
    public Line2(Vector2d position, Vector2d displacement) {
        this();
        this.position.set(position);
        this.displacement.set(displacement);
    }
    public Line2(Line2 line) {
        this(line.position, line.displacement);
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
    public String toString() {
        return "Line2 pos=" + position + " d=" + displacement;
    }
}
