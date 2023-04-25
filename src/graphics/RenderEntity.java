package graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RenderEntity {
    private final Mesh mesh;
    private final Vector3f position;
    private final Vector3f rotation;
    private float scale;
    private final Matrix4f worldMatrix;
    public RenderEntity(Mesh mesh) {
        this.mesh = mesh;
        position = new Vector3f();
        rotation = new Vector3f();
        scale = 1;
        worldMatrix = new Matrix4f();
    }
    public Vector3f getPosition() {
        return position;
    }
    public Vector3f getRotation() {
        return rotation;
    }
    public float getScale() {
        return scale;
    }
    public void setScale(float value) {
        scale = value;
    }
    public Matrix4f getWorldMatrix() {
        return worldMatrix.identity()
                .translate(position)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z)
                .scale(scale);
    }
    public Mesh getMesh() {
        return mesh;
    }
    public void delete() {
        mesh.delete();
    }
}
