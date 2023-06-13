package geometry;

import game.Ball;
import game.Box;
import geometry.Line3;
import org.joml.Vector3d;

import java.util.ArrayList;

import static geometry.Geometry.distance;

public class CollisionHandler3 {
    private Ball ball;
    private final Line3 ballMotion;
    private final ArrayList<CollisionObject3> collisionObjects;
    private final Vector3d minIntersection;
    private CollisionObject3 minCollisionObject;
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
        Vector3d up = new Vector3d(0, 0, box.geometry.displacement.z);
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1(), box.geometry.y1()-ball.getRadius(), box.geometry.z1()),
                        new Vector3d(box.geometry.displacement.x, 0, 0),
                        up
                )
        ));
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x2()+ball.getRadius(), box.geometry.y1(), box.geometry.z1()),
                        new Vector3d(0, box.geometry.displacement.y, 0),
                        up
                )
        ));
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x2(), box.geometry.y2()+ball.getRadius(), box.geometry.z1()),
                        new Vector3d(-box.geometry.displacement.x, 0, 0),
                        up
                )
        ));
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1()-ball.getRadius(), box.geometry.y2(), box.geometry.z1()),
                        new Vector3d(0, -box.geometry.displacement.y, 0),
                        up
                )
        ));

        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                         box.geometry.position,
                        up,
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2(), box.geometry.y1(), box.geometry.z1()),
                        up,
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2(), box.geometry.y2(), box.geometry.z1()),
                        up,
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1(), box.geometry.y2(), box.geometry.z1()),
                        up,
                        ball.getRadius()
                )
        ));
    }
    public void processCollisions() {
        System.out.println(collisionObjects.size());
        int i = 0;
        Vector3d intersection = new Vector3d();
        while (++i < 7) {
            double minDistance = Double.POSITIVE_INFINITY;
            for (CollisionObject3 object : collisionObjects) {
                if (!object.intersect(ballMotion, intersection)) {
                    continue;
                }
                double distance = distance(intersection, ballMotion.position);
                if (distance <= minDistance) {
                    minDistance = distance;
                    minIntersection.set(intersection);
                    minCollisionObject = object;
                }
            }
            if (minDistance == Double.POSITIVE_INFINITY) {
                i--;
                break;
            }
            minCollisionObject.reflectLine(ballMotion, minIntersection, minDistance);
        }

        System.out.printf("----------- i=%d\n", i);

        ball.geometry.position.set(ballMotion.position);
        ball.velocity.set(ballMotion.displacement);
    }
}
