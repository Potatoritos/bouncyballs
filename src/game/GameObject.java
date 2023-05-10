package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class GameObject {
    public final Vector3d position;
    protected final Matrix4f worldMatrix;
    public GameObject(Vector3d position) {
        this.position = position;
        worldMatrix = new Matrix4f();
    }
    public Matrix4f getWorldMatrix(Vector2f globalRotation) {
        return worldMatrix.identity()
                .rotateX(globalRotation.x)
                .rotateY(globalRotation.y)
                .translate((float)position.x, (float)position.y, (float)position.z);
    }
}
