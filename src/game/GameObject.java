package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class GameObject {
    protected final Vector3f position;
    protected final Matrix4f worldMatrix;
    public GameObject(Vector3f position) {
        this.position = position;
        worldMatrix = new Matrix4f();
    }
    public Vector3f getPosition() {
        return position;
    }
    public Matrix4f getWorldMatrix(Vector2f globalRotation) {
        return worldMatrix.identity()
                .rotateX(globalRotation.x)
                .rotateY(globalRotation.y)
                .translate(position);
    }
}
