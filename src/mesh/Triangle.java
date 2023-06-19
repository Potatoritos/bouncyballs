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
        this.a = a;
        this.b = b;
        this.c = c;
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
