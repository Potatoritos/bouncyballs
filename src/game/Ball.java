package game;

import math.Geometry;
import org.joml.Vector3f;
import shape.Line3;
import shape.Sphere;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import static math.Geometry.project;
import static math.MathUtil.cubicInterpolation;

/**
 * Represents the balls you have to maneuver into the holes
 */
public class Ball extends GameObject {
    public final Vector3d velocity;
    public final Sphere geometry;
    private final Vector3d deferredVelocity;
    private boolean velocityDeferred;
    private boolean hasReachedGoal;
    private boolean isDead;
    private final FrameTimer explosionTimer;
    private final Vector3d explosionPosition;
    private int holeColor;
    public Ball() {
        super();
        velocity = new Vector3d();
        geometry = new Sphere();
        deferredVelocity = new Vector3d(0, 0, 0);
        explosionTimer = new FrameTimer(72);
        explosionPosition = new Vector3d();
    }
    public Ball(Sphere geometry) {
        this();
        this.geometry.set(geometry);
    }
    @Override
    public void update() {
        // Handle explosion animation
        explosionTimer.update();
        if (explosionTimer.isActive()) {
            geometry.position.set(explosionPosition);
            geometry.setRadius(0.35 + 1.5*cubicInterpolation(explosionTimer.percentage()));
            getColor(0).w = 1 - explosionTimer.fpercentage();
            if (explosionTimer.isOnLastFrame()) {
                isDead = true;
            }
            return;
        }

        // Perform numerical integration (add velocity to position)
        geometry.position.add(velocity);
        if (velocityDeferred) {
            velocity.set(deferredVelocity);
            velocityDeferred = false;
        }
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
    public void triggerExplosionAnimation(Vector3d position) {
        explosionPosition.set(position);
        explosionTimer.start();
    }
    public boolean isInExplosionAnimation() {
        return explosionTimer.isActive();
    }
    public Vector3d getPosition() {
        return geometry.position;
    }
    @Override
    public Matrix4f getWorldMatrix(Vector3d globalRotation) {
        return worldMatrix.identity()
                .rotateX((float)globalRotation.x)
                .rotateY((float)globalRotation.y)
                .rotateZ((float)globalRotation.z)
                .translate((float)getPosition().x, (float)getPosition().y, (float)getPosition().z)
                .scale((float)getRadius());
    }
    @Override
    public void reflectLine(Line3 line, Vector3d intersection, Vector3d normal) {
        // Rebound the ball colliding into this one
        Geometry.reflectLineFixedRebound(line, intersection, normal, 0.022);

        // Rebound this ball as well
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
