package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Ball extends GameObject {
    public Vector3f velocity;
    private float radius;
    public Ball(Vector3f position, float radius) {
        super(position);
        velocity = new Vector3f();
        this.radius = radius;
    }
    public float getRadius() {
        return radius;
    }
    public void update() {
        position.add(velocity);
    }
    public Matrix4f getWorldMatrix(Vector2f globalRotation) {
        return worldMatrix.identity()
                .rotateX(globalRotation.x)
                .rotateY(globalRotation.y)
                .translate(position)
                .scale(radius);
    }
}
