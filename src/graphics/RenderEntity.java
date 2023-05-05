package graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RenderEntity {
    private final Mesh mesh;
    private final Vector3f position;
//    private final Vector3f rotation;
//    private float scale;
    private final Matrix4f worldMatrix;
    public RenderEntity(Mesh mesh, Vector3f position) {
        this.mesh = mesh;
        this.position = position;
        worldMatrix = new Matrix4f();
    }
    public RenderEntity(Mesh mesh) {
        this(mesh, new Vector3f());
    }
    public Vector3f getPosition() {
        return position;
    }
//    public Vector3f getRotation() {
//        return rotation;
//    }
//    public float getScale() {
//        return scale;
//    }
//    public void setScale(float value) {
//        scale = value;
//    }
    public Matrix4f getWorldMatrix() {
        return worldMatrix.identity()
                .translate(position);
//                .rotateX(rotation.x)
//                .rotateY(rotation.y)
//                .rotateZ(rotation.z)
//                .scale(scale);
    }
    public Matrix4f getWorldMatrix(float globalRotationX, float globalRotationY) {
        return worldMatrix.identity()
                .rotateX(globalRotationX)
                .rotateY(globalRotationY)
                .translate(position);
//                .rotateX(rotation.x)
//                .rotateY(rotation.y)
//                .rotateZ(rotation.z)
//                .scale(scale);
    }
    public Mesh getMesh() {
        return mesh;
    }
    public void delete() {
        mesh.delete();
    }
}
