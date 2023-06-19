package graphics;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import util.Deletable;

/**
 * A class to help with rendering objects without having to
 * create a GameObject
 */
public class RenderObject implements Deletable {
    public final Mesh mesh;
    public final Vector3f position;
    private final Matrix4f worldMatrix;
    public RenderObject(Mesh mesh, Vector3f position) {
        this.mesh = mesh;
        this.position = new Vector3f().set(position);
        worldMatrix = new Matrix4f();
    }
    public RenderObject(Mesh mesh) {
        this(mesh, new Vector3f());
    }

    /**
     * Gets the transformation matrix that represents the object's position in the world
     * @return the matrix
     */
    public Matrix4f getWorldMatrix() {
        return worldMatrix.identity()
                .translate(position);
    }


    /**
     * Gets the transformation matrix that represents the object's position in the world
     * @param globalRotation the rotation of the level this object belongs to
     * @return the matrix
     */
    public Matrix4f getWorldMatrix(Vector3d globalRotation) {
        return worldMatrix.identity()
                .rotateX((float)globalRotation.x)
                .rotateY((float)globalRotation.y)
                .rotateZ((float)globalRotation.z)
                .translate(position);
    }
    public void render() {
        mesh.render();
    }
    public void delete() {
        mesh.delete();
    }
}
