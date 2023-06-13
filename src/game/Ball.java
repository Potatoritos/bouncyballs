package game;

import game.GameObject;
import geometry.Sphere;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3d;

public class Ball extends GameObject {
    public Vector3d velocity;
    public final Sphere geometry;
    public Ball() {
        super();
        velocity = new Vector3d();
        geometry = new Sphere();
    }
    public Ball(Sphere geometry) {
        this();
        this.geometry.set(geometry);
    }
    public double getRadius() {
        return geometry.getRadius();
    }
    public void update() {
        geometry.position.add(velocity);
    }
    public Vector3d getPosition() {
        return geometry.position;
    }
    public Matrix4f getWorldMatrix(Vector3d globalRotation) {
        return worldMatrix.identity()
                .rotateX((float)globalRotation.x)
                .rotateY((float)globalRotation.y)
                .translate((float)getPosition().x, (float)getPosition().y, (float)getPosition().z)
                .scale((float)getRadius());
    }
}
