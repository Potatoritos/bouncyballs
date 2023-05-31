package game;

import game.GameObject;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3d;

public class Ball extends GameObject {
    public Vector3d velocity;
    private double radius;
    public Ball(Vector3d position, double radius) {
        super(position);
        velocity = new Vector3d();
        this.radius = radius;
    }
    public double getRadius() {
        return radius;
    }
    public void update() {
        position.add(velocity);
    }
    public Matrix4f getWorldMatrix(Vector3d globalRotation) {
        return worldMatrix.identity()
                .rotateX((float)globalRotation.x)
                .rotateY((float)globalRotation.y)
                .translate((float)position.x, (float)position.y, (float)position.z)
                .scale((float)radius);
    }
}
