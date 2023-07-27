package game;

import audio.AudioHandler;
import audio.AudioSource;
import math.Geometry;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import shape.Line3d;
import shape.Sphere;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import static math.Geometry.project;
import static math.MathUtil.cubicInterpolation;
import static util.Util.vector3dTo3f;

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

    private double lastCollisionSpeed;
    private final AudioSource collisionSound;
    private final AudioSource snapSound;
    private final AudioSource goalSound;
    private final AudioSource explosionSound;
    private boolean shouldSnap;
    private final Vector3f previousPosition;
    private boolean shouldSplash;
    private boolean hasSplashed;
    private final Vector3f snapPosition;
    private boolean shouldExplode;
    public Ball(AudioHandler audioHandler) {
        super();
        velocity = new Vector3d();
        geometry = new Sphere();
        deferredVelocity = new Vector3d(0, 0, 0);
        explosionTimer = new FrameTimer(72);
        explosionPosition = new Vector3d();

        collisionSound = new AudioSource(audioHandler.clackSound, false, false);
        snapSound = new AudioSource(audioHandler.snapSound, false, false);
        goalSound = new AudioSource(audioHandler.splashSound, false, false);
        explosionSound = new AudioSource(audioHandler.explosionSound, false, false);
        previousPosition = new Vector3f(-727, 0, 0);
        snapPosition = new Vector3f();
    }
    public Ball(Sphere geometry, AudioHandler audioHandler) {
        this(audioHandler);
        this.geometry.set(geometry);
    }
    public void update(Matrix3f globalRotationMatrix) {
        explosionTimer.advanceFrame();

        Vector3f position = vector3dTo3f(geometry.position).mul(globalRotationMatrix);
        if (explosionTimer.isActive()) {
            geometry.position.set(explosionPosition);
            geometry.setRadius(1.5 - 1.5*cubicInterpolation(cubicInterpolation(explosionTimer.percentage())));

            // fade out the explosion
//            getColor(0).w = Math.max(0, 1 - 2*explosionTimer.fpercentage());

            if (explosionTimer.isOnLastFrame()) {
                isDead = true;
            }
            if (explosionTimer.getFrame() == 2) {
                explosionSound.setPosition(position);
                explosionSound.play();
            }
            return;
        }

        geometry.position.add(velocity);
        if (velocityDeferred) {
            velocity.set(deferredVelocity);
            velocityDeferred = false;
        }

//        if (previousPosition.x == -727) {
//            previousPosition.set(position);
//        }
//        Vector3f velocity = new Vector3f(position).sub(previousPosition);
//        previousPosition.set(position);
//        Vector3f velocity = vector3dTo3f(this.velocity);
        if (lastCollisionSpeed > 0.001) {
            collisionSound.setPosition(position);
            float gain = Math.min(4, (float)lastCollisionSpeed / 0.01f);
            collisionSound.setGain(gain);
            collisionSound.play();
            lastCollisionSpeed = 0;
        }
        if (shouldSnap) {
            snapSound.setPosition(snapPosition.mul(globalRotationMatrix));
            snapSound.play();
            shouldSnap = false;
        }
        if (shouldSplash && !hasSplashed) {
            goalSound.setPosition(position);
            goalSound.play();
            hasSplashed = true;
        }
    }
    public void setLastCollisionSpeed(double value) {
        lastCollisionSpeed = value;
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
    public void queueSnap(Vector3f position) {
        shouldSnap = true;
        snapPosition.set(position);
    }
    public void queueSplash() {
        shouldSplash = true;
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
    public void reflectLine(Line3d line, Vector3d intersection, Vector3d normal) {
        // Rebound the ball colliding into this one
        Geometry.reflectLineFixedRebound(line, intersection, normal, 0.03);

        // Rebound this ball as well
        // Velocity is deferred to the next frame to ensure that collisions are handled before the velocity adds to position
        Vector3d normalComponent = new Vector3d();
        deferredVelocity.set(velocity);
        project(deferredVelocity, normal, normalComponent);
        deferredVelocity.sub(normalComponent);

        normalComponent.set(normal).normalize(0.03);
        deferredVelocity.sub(normalComponent);
        velocityDeferred = true;

        queueSnap(vector3dTo3f(intersection));
    }

    public void delete() {
        collisionSound.delete();
        snapSound.delete();
        goalSound.delete();
        explosionSound.delete();
//        buffer.delete();
    }
}
