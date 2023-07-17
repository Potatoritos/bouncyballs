package mesh;

import org.joml.Vector3f;

import java.util.ArrayList;

/**
 * Represents a triangle defined by 3 points
 */
public class Triangle {
    public final Vector3f a;
    public final Vector3f b;
    public final Vector3f c;
    public Triangle() {
        a = new Vector3f();
        b = new Vector3f();
        c = new Vector3f();
    }

    /**
     * The points a, b, c should be in counter-clockwise order
     */
    public Triangle(Vector3f a, Vector3f b, Vector3f c) {
        this();
        set (a, b, c);
    }

    public Triangle(Vector3f v) {
        this();
        set(v, v, v);
    }

    public void set(Vector3f a, Vector3f b, Vector3f c) {
        this.a.set(a);
        this.b.set(b);
        this.c.set(c);
    }

    /**
     * Adds this triangle's points to a list
     */
    public void addToList(ArrayList<Vector3f> list) {
        list.add(a);
        list.add(b);
        list.add(c);
    }
}
