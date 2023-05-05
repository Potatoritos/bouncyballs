package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Tile {
    protected final Vector3f position;
    protected final Vector3f dimensions;
    protected final Matrix4f worldMatrix;
    public Tile(Vector3f position, Vector3f dimensions) {
        this.position = position;
        this.dimensions = dimensions;
        worldMatrix = new Matrix4f();
    }
    public Vector3f getPosition() {
        return position;
    }
    public Vector3f getDimensions() {
        return dimensions;
    }
    public Matrix4f getWorldMatrix(Vector2f globalRotation) {
        return worldMatrix.identity()
                .rotateX(globalRotation.x)
                .rotateY(globalRotation.y)
                .translate(position);
    }
}
