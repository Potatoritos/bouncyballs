package game;

import game.GameObject;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3d;

public class Box extends GameObject {
    public final Vector3d dimensions;
    public Box(Vector3d position, Vector3d dimensions) {
        super(position);
        this.dimensions = dimensions;
    }
}
