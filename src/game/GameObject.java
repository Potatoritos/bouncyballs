package game;

import org.joml.Matrix4f;
import org.joml.Vector3d;

public class GameObject {
    public final Vector3d position;
    protected final Matrix4f worldMatrix;
    public GameObject(Vector3d position) {
        this.position = position;
        worldMatrix = new Matrix4f();
    }
    public Matrix4f getWorldMatrix(Vector3d globalRotation) {
        return worldMatrix.identity()
                .rotateX((float)globalRotation.x)
                .rotateY((float)globalRotation.y)
                .translate((float)position.x, (float)position.y, (float)position.z);
    }
}
