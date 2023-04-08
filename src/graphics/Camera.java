package graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private final Vector3f position;
    private final Vector3f rotation;
    private final Matrix4f viewMatrix;
    public Camera() {
        position = new Vector3f();
        rotation = new Vector3f();
        viewMatrix = new Matrix4f();
    }
    public Vector3f getPosition() {
        return position;
    }
    public Vector3f getRotation() {
        return rotation;
    }
    public Matrix4f getViewMatrix() {
         viewMatrix.identity()
                 .rotateXYZ(rotation)
                 .translate(-position.x, -position.y, -position.z);
         return viewMatrix;
    }
}
