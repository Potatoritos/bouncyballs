package geometry;

import game.Ball;
import game.Box;
import geometry.Line3;
import org.joml.Vector3d;

import java.util.ArrayList;

public class CollisionHandler3 {
    private Ball ball;
    private final Line3 ballMotion;
    private final ArrayList<CollisionObject3> collisionObjects;
    private final Vector3d minIntersection;
    private final Sphere ballSphere;
    public CollisionHandler3() {
        ballMotion = new Line3();
        collisionObjects = new ArrayList<>();
        minIntersection = new Vector3d();
        ballSphere = new Sphere();
    }
    public void reset() {
        collisionObjects.clear();
    }
    public void setBall(Ball ball) {
        this.ball = ball;
        ballSphere.set(ball.geometry);
        ballMotion.position.set(ball.geometry.position);
        ballMotion.displacement.set(ball.velocity);
    }
    private void addCollisionObject(CollisionObject3 object) {
        if (object.isNearby(ballSphere)) {
            collisionObjects.add(object);
        }
    }
    public void addWallBox(Box box) {
//        addCollisionObject(new CollisionPlane(box,
//                new Plane(
//                        new Vector3d(box.geometry.x1(), box.geometry.y1()-ball.getRadius(), box.geometry.z1()),
//                        new Vector3d(box.dimensions)
//                )
//        ))·;
    }
}
