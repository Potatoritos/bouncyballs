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
        set(position, displacement);
    }
    public Line2(Line2 line) {
        this();
        set(line);
    }
    public void set(Vector2d position, Vector2d displacement) {
        this.position.set(position);
        this.displacement.set(displacement);
    }
    public void set(Line2 line) {
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
    public String toString() {
        return String.format("[Line2 pos=%s d=%s]", position, displacement);
    }
}
