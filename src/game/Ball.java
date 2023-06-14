package game;

import geometry.Geometry;
import geometry.Line3;
import geometry.Sphere;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import static geometry.Geometry.project;

public class Ball extends GameObject {
    public final Vector3d velocity;
    public final Sphere geometry;
    private final Vector3d deferredVelocity;
    private boolean velocityDeferred;
    public Ball() {
        super();
        velocity = new Vector3d();
        geometry = new Sphere();
        deferredVelocity = new Vector3d(0, 0, 0);
        velocityDeferred = false;
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
        Geometry.reflectLineFixedRebound(line, intersection, normal, 0.05);
//        Geometry.reflectLine(line, intersection, normal, 0.5);

        // Rebound self as well
        Vector3d normalComponent = new Vector3d();
        deferredVelocity.set(velocity);
        project(deferredVelocity, normal, normalComponent);
        deferredVelocity.sub(normalComponent);

        normalComponent.set(normal).normalize(0.05);
        deferredVelocity.sub(normalComponent);
        velocityDeferred = true;
    }
}
