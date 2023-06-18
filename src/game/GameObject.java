package game;

import collision.CollisionObject3;
import collision.CollisionObjectContainer;
import collision.CollisionTrigger;
import org.joml.Vector4f;
import shape.Line3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

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
    public Matrix4f getWorldMatrix(Vector3d globalRotation) {
        return worldMatrix.identity()
                .rotateX((float)globalRotation.x)
                .rotateY((float)globalRotation.y)
                .rotateZ((float)globalRotation.z)
                .translate((float)getPosition().x, (float)getPosition().y, (float)getPosition().z);
    }
    public void update() {

    }
    public void reflectLine(Line3 line, Vector3d intersection, Vector3d normal) {

    }
}
