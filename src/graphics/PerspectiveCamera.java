package graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class PerspectiveCamera {
    public final Vector3f position;
    public final Vector3f rotation;
    private final Matrix4f viewMatrix;
    private float fov;
    private float zNear;
    private float zFar;
    private float aspectRatio;
    private final Matrix4f projectionMatrix;
    public PerspectiveCamera() {
        position = new Vector3f();
        rotation = new Vector3f();
        viewMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
    }
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
    public float getFov() {
        return fov;
    }
    public void setFov(float value) {
        fov = value;
    }
    public float getZNear() {
        return zNear;
    }
    public void setZNear(float value) {
        zNear = value;
    }
    public float getZFar() {
        return zFar;
    }
    public void setZFar(float value) {
        zFar = value;
    }
    public float getAspectRatio() {
        return aspectRatio;
    }
    public void setAspectRatio(float value) {
        aspectRatio = value;
    }
    public void updateProjectionMatrix() {
        projectionMatrix.identity().perspective(fov, aspectRatio, zNear, zFar);
    }
    public Matrix4f getViewMatrix() {
         return viewMatrix.identity()
                 .rotateXYZ(rotation)
                 .translate(-position.x, -position.y, -position.z);
    }
    public Matrix4f getViewMatrix(Matrix4f worldMatrix) {
        return getViewMatrix().mul(worldMatrix);
    }
}
