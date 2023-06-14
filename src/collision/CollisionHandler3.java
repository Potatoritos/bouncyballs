package collision;

import game.Ball;
import game.Box;
import game.HoleBox;
import geometry.Cylinder;
import geometry.Line3;
import geometry.Plane;
import geometry.Sphere;
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
    public void addFloorBoxSides(Box box) {
        addWallBox(box);

        addCollisionObject(new CollisionSphere(box,
                new Sphere(new Vector3d(box.geometry.x1(), box.geometry.y1(), box.geometry.z2()), ball.getRadius())
        ));
        addCollisionObject(new CollisionSphere(box,
                new Sphere(new Vector3d(box.geometry.x1(), box.geometry.y2(), box.geometry.z2()), ball.getRadius())
        ));
        addCollisionObject(new CollisionSphere(box,
                new Sphere(new Vector3d(box.geometry.x2(), box.geometry.y2(), box.geometry.z2()), ball.getRadius())
        ));
        addCollisionObject(new CollisionSphere(box,
                new Sphere(new Vector3d(box.geometry.x2(), box.geometry.y1(), box.geometry.z2()), ball.getRadius())
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1(), box.geometry.y1(), box.geometry.z2()),
                        new Vector3d(box.geometry.displacement.x, 0, 0),
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2(), box.geometry.y1(), box.geometry.z2()),
                        new Vector3d(0, box.geometry.displacement.y, 0),
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2(), box.geometry.y2(), box.geometry.z2()),
                        new Vector3d(-box.geometry.displacement.x, 0, 0),
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1(), box.geometry.y2(), box.geometry.z2()),
                        new Vector3d(0, -box.geometry.displacement.y, 0),
                        ball.getRadius()
                )
        ));
    }
    public void addFloorBox(Box box) {
        addFloorBoxSides(box);

        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1(), box.geometry.y1(), box.geometry.z2() + ball.getRadius()),
                        new Vector3d(box.geometry.displacement.x, 0, 0),
                        new Vector3d(0, box.geometry.displacement.y, 0)
                )
        ));
    }
    public void addHoleBox(HoleBox box) {
        // The hole, when spherically extruded, forms a torus
        // Approximate this torus using 8 cylinders (because i am NOT figuring out how to reflect a line off of a torus)

        // Assumes that the box is 1x1
        double circleTop = 0.5 - box.getRadius();
        double top = box.geometry.z2()+ball.getRadius();

        // Planes surrounding the hole (horizontal and vertical)
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1(), box.geometry.y1(), top),
                        new Vector3d(box.geometry.displacement.x, 0, 0),
                        new Vector3d(0, circleTop, 0)
                )
        ));
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1(), box.geometry.y1(), top),
                        new Vector3d(0, box.geometry.displacement.y, 0),
                        new Vector3d(circleTop, 0, 0)
                )
        ));
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x2(), box.geometry.y2(), top),
                        new Vector3d(-box.geometry.displacement.x, 0, 0),
                        new Vector3d(0, -circleTop, 0)
                )
        ));
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x2(), box.geometry.y2(), top),
                        new Vector3d(0, -box.geometry.displacement.y, 0),
                        new Vector3d(-circleTop, 0, 0)
                )
        ));

        // Cylinders surrounding the hole (horizontal and vertical)
        double cylinderLength = 2*box.getRadius();
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1()+circleTop, box.geometry.y1()+circleTop, box.geometry.z2()),
                        new Vector3d(cylinderLength, 0, 0),
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1()+circleTop, box.geometry.y1()+circleTop, box.geometry.z2()),
                        new Vector3d(0, cylinderLength, 0),
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2()-circleTop, box.geometry.y2()-circleTop, box.geometry.z2()),
                        new Vector3d(-cylinderLength, 0, 0),
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2()-circleTop, box.geometry.y2()-circleTop, box.geometry.z2()),
                        new Vector3d(0, -cylinderLength, 0),
                        ball.getRadius()
                )
        ));

        // Triangles surrounding the hole (diagonal)
        double s = box.getRadius()/(Math.sqrt(2)/2 + 1);
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1()+circleTop, box.geometry.y1()+circleTop, top),
                        new Vector3d(s, 0, 0),
                        new Vector3d(0, s, 0)
                )
        ));
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x2()-circleTop, box.geometry.y1()+circleTop, top),
                        new Vector3d(-s, 0, 0),
                        new Vector3d(0, s, 0)
                )
        ));
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x2()-circleTop, box.geometry.y2()-circleTop, top),
                        new Vector3d(-s, 0, 0),
                        new Vector3d(0, -s, 0)
                )
        ));
        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1()+circleTop, box.geometry.y2()-circleTop, top),
                        new Vector3d(s, 0, 0),
                        new Vector3d(0, -s, 0)
                )
        ));

        // Cylinders surrounding the hole (diagonal)
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1()+circleTop, box.geometry.y1()+circleTop+s, box.geometry.z2()),
                        new Vector3d(s, -s, 0),
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2()-circleTop-s, box.geometry.y1()+circleTop, box.geometry.z2()),
                        new Vector3d(s, s, 0),
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2()-circleTop, box.geometry.y2()-circleTop-s, box.geometry.z2()),
                        new Vector3d(-s, s, 0),
                        ball.getRadius()
                )
        ));
        addCollisionObject(new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1()+circleTop+s, box.geometry.y2()-circleTop, box.geometry.z2()),
                        new Vector3d(-s, -s, 0),
                        ball.getRadius()
                )
        ));

        // Inverted cylinder through the hole
        addCollisionObject(new CollisionCylinderInverted(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1()+0.5, box.geometry.x2()+0.5, box.geometry.z1()-ball.getRadius()),
                        new Vector3d(0, 0, box.geometry.displacement.z+ball.getRadius()),
                        box.getRadius() - ball.getRadius()
                )
        ));

        addFloorBoxSides(box);
    }
    public void processCollisions() {
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

        ball.geometry.position.set(ballMotion.position);
        ball.velocity.set(ballMotion.displacement);
    }
}
