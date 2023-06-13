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
    public Quad(Vector3f v) {
        this();
        this.a.set(v);
        this.b.set(v);
        this.c.set(v);
        this.d.set(v);
    }
    public Quad(Vector3f a, Vector3f b, Vector3f c, Vector3f d) {
        this();
        this.a.set(a);
        this.b.set(b);
        this.c.set(c);
        this.d.set(d);
    }
    public void addToList(ArrayList<Vector3f> list) {
        list.add(a);
        list.add(b);
        list.add(c);
        list.add(d);
    }
}