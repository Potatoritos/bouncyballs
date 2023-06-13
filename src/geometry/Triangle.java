package geometry;

import org.joml.Vector3f;

import java.util.ArrayList;

public class Triangle {
    public final Vector3f a;
    public final Vector3f b;
    public final Vector3f c;
    public Triangle() {
        a = new Vector3f();
        b = new Vector3f();
        c = new Vector3f();
    }
    public Triangle(Vector3f a, Vector3f b, Vector3f c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
    public void addToList(ArrayList<Vector3f> list) {
        list.add(a);
        list.add(b);
        list.add(c);
    }
}
