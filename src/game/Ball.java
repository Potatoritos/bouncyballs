package game;

import math.Geometry;
import shape.Line3;
import shape.Sphere;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import static math.Geometry.project;

public class Ball extends GameObject {
    public final Vector3d velocity;
    public final Sphere geometry;
    private final Vector3d deferredVelocity;
    private boolean velocityDeferred;
    private boolean hasReachedGoal;
    private boolean isDead;
    private int holeColor;
    public Ball() {
        super();
        velocity = new Vector3d();
        geometry = new Sphere();
        deferredVelocity = new Vector3d(0, 0, 0);
        velocityDeferred = false;
        hasReachedGoal = false;
    }
    public Ball(Sphere geometry) {
        this();
        this.geometry.set(geometry);
    }
    public void setHasReachedGoal(boolean value) {
        hasReachedGoal = value;
    }
    public boolean hasReachedGoal() {
        return hasReachedGoal;
    }
    public void setHoleColor(int value) {
        holeColor = value;
    }
    public void setIsDead(boolean value) {
        isDead = value;
    }
    public boolean isDead() {
        return isDead;
    }
    public int getHoleColor() {
        return holeColor;
    }
    public double getRadius() {
        return geometry.getRadius();
    }
    @Override
    public void update() {
        geometry.position.add(velocity);
        if (velocityDeferred) {
            velocity.set(deferredVelocity);
            velocityDeferred = false;
        }
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
    @Override
    public void reflectLine(Line3 line, Vector3d intersection, Vector3d normal) {
        // Rebound the ball colliding into this one
        Geometry.reflectLineFixedRebound(line, intersection, normal, 0.022);

        // Rebound self as well
        // Velocity is deferred to the next frame to ensure that collisions are handled before the velocity adds to position
        Vector3d normalComponent = new Vector3d();
        deferredVelocity.set(velocity);
        project(deferredVelocity, normal, normalComponent);
        deferredVelocity.sub(normalComponent);

        normalComponent.set(normal).normalize(0.022);
        deferredVelocity.sub(normalComponent);
        velocityDeferred = true;
    }
}
