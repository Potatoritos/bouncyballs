package geometry;

import org.joml.Vector3f;

import java.util.ArrayList;

public class Quad {
    public final Vector3f a;
    public final Vector3f b;
    public final Vector3f c;
    public final Vector3f d;
    public Quad() {
        a = new Vector3f();
        b = new Vector3f();
        c = new Vector3f();
        d = new Vector3f();
    }
    public Quad(Vector3f a, Vector3f b, Vector3f c, Vector3f d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
    public void addToList(ArrayList<Vector3f> list) {
        list.add(a);
        list.add(b);
        list.add(c);
        list.add(d);
    }
}
