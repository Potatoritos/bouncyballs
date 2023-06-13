package game;

import org.joml.Matrix4f;
import org.joml.Vector3d;

public abstract class GameObject {
    protected final Matrix4f worldMatrix;
    public GameObject() {
        worldMatrix = new Matrix4f();
    }
    public abstract Vector3d getPosition();
    public Matrix4f getWorldMatrix(Vector3d globalRotation) {
        return worldMatrix.identity()
                .rotateX((float)globalRotation.x)
                .rotateY((float)globalRotation.y)
                .translate((float)getPosition().x, (float)getPosition().y, (float)getPosition().z);
    }
}
