package shape;

import org.joml.Vector2d;

/**
 * Represents a 2-D line defined by a position and a direction
 */
public class Line2d {
    public final Vector2d position;
    public final Vector2d displacement;
    public Line2d() {
        this.position = new Vector2d();
        this.displacement = new Vector2d();
    }
    public Line2d(Vector2d position, Vector2d displacement) {
        this();
        set(position, displacement);
    }
    public Line2d(Line2d line) {
        this();
        set(line);
    }
    public void set(Vector2d position, Vector2d displacement) {
        this.position.set(position);
        this.displacement.set(displacement);
    }
    public void set(Line2d line) {
        set(line.position, line.displacement);
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

    /**
     * @return true if the point is in the rectangle defined by corners (x1(), y1()) and (x2(), y2()); false otherwise
     */
    public boolean isPointInRectangle(Vector2d point) {
        return x1() <= point.x && point.x <= x2() && y1() <= point.y && point.y <= y2();
    }
    public String toString() {
        return String.format("[Line2 pos=%s d=%s]", position, displacement);
    }
}
