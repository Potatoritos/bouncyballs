package game;

import game.GameObject;
import geometry.Line3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3d;

public class Box extends GameObject {
    public final Line3 geometry;
    public Box() {
        super();
        geometry = new Line3();
    }
    public Box(Line3 geometry) {
        this();
        this.geometry.set(geometry);
    }
    public Vector3d getPosition() {
        return geometry.position;
    }
}
