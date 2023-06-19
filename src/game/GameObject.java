package game;

import org.joml.Vector4f;
import shape.Line3;
import org.joml.Matrix4f;
import org.joml.Vector3d;

/**
 * Represents an object that exists in a level
 */
public abstract class GameObject {
    protected final Matrix4f worldMatrix;
    private final Vector4f[] color;
    public GameObject() {
        worldMatrix = new Matrix4f();
        color = new Vector4f[2];
        color[0] = new Vector4f();
        color[1] = new Vector4f();
    }
    public Vector4f getColor(int index) {
        return color[index];
    }
    public abstract Vector3d getPosition();

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
                .translate((float)getPosition().x, (float)getPosition().y, (float)getPosition().z);
    }
    public void update() {

    }

    /**
     * Reflects a ball off of this object
     * @param line a line representing the ball's motion
     * @param intersection the point at which the ball intersects with the object
     * @param normal the normal of the surface at the point of intersection
     */
    public void reflectLine(Line3 line, Vector3d intersection, Vector3d normal) {

    }
}
