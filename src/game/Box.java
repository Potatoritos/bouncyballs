package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Box extends GameObject {
    protected final Vector3f dimensions;
    public Box(Vector3f position, Vector3f dimensions) {
        super(position);
        this.dimensions = dimensions;
    }
    public Vector3f getDimensions() {
        return dimensions;
    }
}
