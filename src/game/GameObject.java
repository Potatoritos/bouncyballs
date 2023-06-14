package game;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public abstract class GameObject {
    protected final Matrix4f worldMatrix;
    public final Vector3f color1;
    public final Vector3f color2;
    public GameObject() {
        worldMatrix = new Matrix4f();
        color1 = new Vector3f();
        color2 = new Vector3f();
    }
    public abstract Vector3d getPosition();
    public Matrix4f getWorldMatrix(Vector3d globalRotation) {
        return worldMatrix.identity()
                .rotateX((float)globalRotation.x)
                .rotateY((float)globalRotation.y)
                .translate((float)getPosition().x, (float)getPosition().y, (float)getPosition().z);
    }
}
