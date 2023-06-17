package collision;

import game.Ball;
import game.Box;
import game.HoleBox;
import shape.Cylinder;
import shape.Line3;
import shape.Plane;
import shape.Sphere;
import org.joml.Vector3d;

import java.util.ArrayList;

import static math.Geometry.distance;

public class CollisionHandler3 {
    private Ball ball;
    private final Line3 ballMotion;
    private final ArrayList<CollisionObject3> collisionObjects;
    private final ArrayList<CollisionTrigger> triggers;
    private final Vector3d minIntersection;
    private CollisionObject3 minCollisionObject;
    private final Sphere ballSphere;
    public CollisionHandler3() {
        ballMotion = new Line3();
        collisionObjects = new ArrayList<>();
        triggers = new ArrayList<>();
        minIntersection = new Vector3d();
        ballSphere = new Sphere();
    }
    public void reset() {
        collisionObjects.clear();
        triggers.clear();
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
    private void addTrigger(CollisionTrigger trigger) {
        if (trigger.collisionObject.isNearby(ballSphere)) {
            triggers.add(trigger);
        }
    }
    public void processCollisions() {
        int i = 0;
        Vector3d intersection = new Vector3d();
        // Limit the max. number of iterations to avoid infinite loops
        while (i++ < 11) {
            // See if the ball collides with any triggers
            for (CollisionTrigger trigger : triggers) {
                if (trigger.isActive() && trigger.collisionObject.intersect(ballMotion, intersection)) {
                    trigger.onCollision(ball);
                    trigger.disable();
                }
            }

            // Get the collision object that collides with the ball at the closest point to the ball
            double minDistance = Double.POSITIVE_INFINITY;
            for (CollisionObject3 object : collisionObjects) {
                if (object.intersect(ballMotion, intersection)) {
                    double distance = distance(intersection, ballMotion.position);
                    if (distance <= minDistance) {
                        minDistance = distance;
                        minIntersection.set(intersection);
                        minCollisionObject = object;
                    }
                }
            }
            // Break if the ball does not collide with any objects
            if (minDistance == Double.POSITIVE_INFINITY) {
                break;
            }

            // Reflect the ball off of the aforementioned closest collision object
            minCollisionObject.reflectLine(ballMotion, minIntersection, minDistance);
        }

        ball.geometry.position.set(ballMotion.position);
        ball.velocity.set(ballMotion.displacement);
    }

    public void addFallDeathTrigger() {
        addTrigger(new DeathTrigger(new CollisionPlane(null, new Plane(
                new Vector3d(-100, -100, -3),
                new Vector3d(200, 0, 0),
                new Vector3d(0, 200, 0)
        ))));
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
        addTrigger(new GoalTrigger(new CollisionPlane(box, new Plane(
                box.geometry.position,
                new Vector3d(box.geometry.displacement.x, 0, 0),
                new Vector3d(0, box.geometry.displacement.y, 0)
        )), box));

        // Act as a regular tile if the correct ball has fallen into the hole
        if (box.hasReachedGoal()) {
            addFloorBox(box);
            return;
        }

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
    public void addBallCollision(Ball ball) {
        addCollisionObject(new CollisionSphere(ball,
                new Sphere(
                        ball.getPosition(),
                        ball.getRadius() + this.ball.getRadius()
                )
        ));
    }
}
