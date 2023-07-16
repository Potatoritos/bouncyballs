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

/**
 * Handles collisions between balls and boxes/other balls
 */
/*
 * To accomplish this, each shape is first spherically extruded.
 * For instance, boxes become rounded boxes, consisting of planar
 * (from the original box's faces), cylindrical (from the original
 * box's edges), and spherical (from the original box's corners)
 * faces.
 *
 * This allows us to represent the ball as a point instead of a
 * sphere, and by extension, the reflections of the ball off of
 * surfaces as reflections of the line
 *      ball.position + t * ball.velocity.
 * off of the spherically-extruded shapes.
 *
 * Then, we can apply the following sequence of steps:
 *
 * For each ball,
 * 1.   Get all collision objects that are near it
 * 2.   Find the intersections between the ball's motion line and
 *      all of the above objects
 * 3.   Reflect the line off of the intersection found above that
 *      is closest to the ball's position
 * 4.   Repeat steps 1-3 until the line no longer intersects any
 *      objects
 */
public class CollisionHandler {
    private Ball ball;
    private final Line3 ballMotion;
    private final ArrayList<CollisionObject> collisionObjects;
    private final ArrayList<CollisionTrigger> triggers;
    private final Vector3d minIntersection;
    private CollisionObject minCollisionObject;
    private final Sphere ballSphere;
    public CollisionHandler() {
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

    /**
     * Sets the ball to process collisions for
     */
    public void setBall(Ball ball) {
        this.ball = ball;
        ballSphere.set(ball.geometry);
        ballMotion.position.set(ball.geometry.position);
        ballMotion.displacement.set(ball.velocity);
    }

    private void addCollisionObject(CollisionObject object) {
        if (object.isNearby(ballSphere)) {
            collisionObjects.add(object);
        }
    }
    private void addTrigger(CollisionTrigger trigger) {
        if (trigger.collisionObject.isNearby(ballSphere)) {
            triggers.add(trigger);
        }
    }

    /**
     * Handle collisions using the steps outlined in the topmost comment
     */
    public void processCollisions() {
        int i = 0;
        Vector3d intersection = new Vector3d();

        boolean collided = false;

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
            for (CollisionObject object : collisionObjects) {
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
            minCollisionObject.reflectLine(ballMotion, minIntersection);
//            if (minCollisionObject.parent instanceof Ball) {
//                ball.queueSnap();
//            }

            collided = true;
        }

        if (collided) {
            Vector3d v = new Vector3d(ballMotion.displacement).sub(ball.velocity);
            ball.setLastCollisionSpeed(v.length());
        }

        ball.geometry.position.set(ballMotion.position);
        ball.velocity.set(ballMotion.displacement);
    }

    /**
     * Add the death trigger that kills balls that fall off the board
     */
    public void addFallDeathTrigger() {
        addTrigger(new DeathTrigger(new CollisionPlane(null, new Plane(
                new Vector3d(-100, -100, -3),
                new Vector3d(200, 0, 0),
                new Vector3d(0, 200, 0)
        ))));
    }

    /**
     * Add the collision objects that make up the walls of a box
     * @param box the box
     */
    public void addBoxWallColliders(Box box) {
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
    /**
     * Add the collision objects that make up the sides of the
     * floor of a box
     * @param box the box
     */
    public void addBoxFloorSideColliders(Box box) {
        addBoxWallColliders(box);

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
    /**
     * Add the collision objects that make up the floor of the box
     * @param box the box
     */
    public void addBoxFloorColliders(Box box) {
        addBoxFloorSideColliders(box);

        addCollisionObject(new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1(), box.geometry.y1(), box.geometry.z2() + ball.getRadius()),
                        new Vector3d(box.geometry.displacement.x, 0, 0),
                        new Vector3d(0, box.geometry.displacement.y, 0)
                )
        ));
    }
    /**
     * Add the collision objects that make up hole boxes
     * @param box the box
     */
    public void addHoleBoxColliders(HoleBox box) {
        addTrigger(new GoalTrigger(new CollisionPlane(box, new Plane(
                box.geometry.position,
                new Vector3d(box.geometry.displacement.x, 0, 0),
                new Vector3d(0, box.geometry.displacement.y, 0)
        )), box));

        // Act as a regular tile if the correct ball has fallen into the hole
        if (box.hasReachedGoal()) {
            addBoxFloorColliders(box);
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

        addBoxFloorSideColliders(box);
    }

    /**
     * Add the collision objects that make up a ball
     * @param ball the ball
     */
    public void addBallColliders(Ball ball) {
        addCollisionObject(new CollisionSphere(ball,
                new Sphere(
                        ball.getPosition(),
                        ball.getRadius() + this.ball.getRadius()
                )
        ));
    }
}
