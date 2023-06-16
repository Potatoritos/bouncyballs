package collision;

import game.Ball;
import game.Box;
import game.HoleBox;
import shape.Cylinder;
import shape.Line3;
import shape.Plane;
import shape.Sphere;
import org.joml.Vector3d;

import static math.Geometry.distance;

public class CollisionHandler3 {
    private Ball ball;
    private final Line3 ballMotion;
    private final CollisionObjectContainer<CollisionObject3> collisions;
    private final CollisionObjectContainer<CollisionTrigger> triggers;
//    private final ArrayList<CollisionObject3> collisionObjects;
//    private final ArrayList<CollisionTrigger> triggers;
    private final Vector3d minIntersection;
    private CollisionObject3 minCollisionObject;
    private final Sphere ballSphere;
    public CollisionHandler3() {
        ballMotion = new Line3();
        collisions = new CollisionObjectContainer<>();
        triggers = new CollisionObjectContainer<>();
        minIntersection = new Vector3d();
        ballSphere = new Sphere();
    }
    public void reset() {
        collisions.objects.clear();
        triggers.objects.clear();
    }
    public void setBall(Ball ball) {
        this.ball = ball;
        ballSphere.set(ball.geometry);
        ballMotion.position.set(ball.geometry.position);
        ballMotion.displacement.set(ball.velocity);
    }
    public void processCollisions() {
        int i = 0;
        Vector3d intersection = new Vector3d();
        // Limit the max. number of iterations to avoid infinite loops
        while (i++ < 10) {
            // See if the ball collides with any triggers
            for (CollisionTrigger trigger : triggers.objects) {
                if (trigger.isActive() && trigger.collisionObject.intersect(ballMotion, intersection)) {
                    trigger.onCollision(ball);
                    trigger.disable();
                }
            }

            // Get the collision object that collides with the ball at the closest point to the ball
            double minDistance = Double.POSITIVE_INFINITY;
            for (CollisionObject3 object : collisions.objects) {
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
        triggers.add(ballSphere, new DeathTrigger(new CollisionPlane(null, new Plane(
                new Vector3d(-100, -100, -2),
                new Vector3d(200, 0, 0),
                new Vector3d(0, 200, 0)
        ))));
    }

    public void addWallBox(Box box) {

    }
    public void addFloorBoxSides(Box box) {
        addWallBox(box);

        collisions.add(ballSphere, new CollisionSphere(box,
                new Sphere(new Vector3d(box.geometry.x1(), box.geometry.y1(), box.geometry.z2()), ball.getRadius())
        ));
        collisions.add(ballSphere, new CollisionSphere(box,
                new Sphere(new Vector3d(box.geometry.x1(), box.geometry.y2(), box.geometry.z2()), ball.getRadius())
        ));
        collisions.add(ballSphere, new CollisionSphere(box,
                new Sphere(new Vector3d(box.geometry.x2(), box.geometry.y2(), box.geometry.z2()), ball.getRadius())
        ));
        collisions.add(ballSphere, new CollisionSphere(box,
                new Sphere(new Vector3d(box.geometry.x2(), box.geometry.y1(), box.geometry.z2()), ball.getRadius())
        ));
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1(), box.geometry.y1(), box.geometry.z2()),
                        new Vector3d(box.geometry.displacement.x, 0, 0),
                        ball.getRadius()
                )
        ));
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2(), box.geometry.y1(), box.geometry.z2()),
                        new Vector3d(0, box.geometry.displacement.y, 0),
                        ball.getRadius()
                )
        ));
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2(), box.geometry.y2(), box.geometry.z2()),
                        new Vector3d(-box.geometry.displacement.x, 0, 0),
                        ball.getRadius()
                )
        ));
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1(), box.geometry.y2(), box.geometry.z2()),
                        new Vector3d(0, -box.geometry.displacement.y, 0),
                        ball.getRadius()
                )
        ));
    }
    public void addFloorBox(Box box) {
        addFloorBoxSides(box);

        collisions.add(ballSphere, new CollisionPlane(box,
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
        collisions.add(ballSphere, new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1(), box.geometry.y1(), top),
                        new Vector3d(box.geometry.displacement.x, 0, 0),
                        new Vector3d(0, circleTop, 0)
                )
        ));
        collisions.add(ballSphere, new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1(), box.geometry.y1(), top),
                        new Vector3d(0, box.geometry.displacement.y, 0),
                        new Vector3d(circleTop, 0, 0)
                )
        ));
        collisions.add(ballSphere, new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x2(), box.geometry.y2(), top),
                        new Vector3d(-box.geometry.displacement.x, 0, 0),
                        new Vector3d(0, -circleTop, 0)
                )
        ));
        collisions.add(ballSphere, new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x2(), box.geometry.y2(), top),
                        new Vector3d(0, -box.geometry.displacement.y, 0),
                        new Vector3d(-circleTop, 0, 0)
                )
        ));

        // Cylinders surrounding the hole (horizontal and vertical)
        double cylinderLength = 2*box.getRadius();
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1()+circleTop, box.geometry.y1()+circleTop, box.geometry.z2()),
                        new Vector3d(cylinderLength, 0, 0),
                        ball.getRadius()
                )
        ));
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1()+circleTop, box.geometry.y1()+circleTop, box.geometry.z2()),
                        new Vector3d(0, cylinderLength, 0),
                        ball.getRadius()
                )
        ));
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2()-circleTop, box.geometry.y2()-circleTop, box.geometry.z2()),
                        new Vector3d(-cylinderLength, 0, 0),
                        ball.getRadius()
                )
        ));
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2()-circleTop, box.geometry.y2()-circleTop, box.geometry.z2()),
                        new Vector3d(0, -cylinderLength, 0),
                        ball.getRadius()
                )
        ));

        // Triangles surrounding the hole (diagonal)
        double s = box.getRadius()/(Math.sqrt(2)/2 + 1);
        collisions.add(ballSphere, new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1()+circleTop, box.geometry.y1()+circleTop, top),
                        new Vector3d(s, 0, 0),
                        new Vector3d(0, s, 0)
                )
        ));
        collisions.add(ballSphere, new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x2()-circleTop, box.geometry.y1()+circleTop, top),
                        new Vector3d(-s, 0, 0),
                        new Vector3d(0, s, 0)
                )
        ));
        collisions.add(ballSphere, new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x2()-circleTop, box.geometry.y2()-circleTop, top),
                        new Vector3d(-s, 0, 0),
                        new Vector3d(0, -s, 0)
                )
        ));
        collisions.add(ballSphere, new CollisionPlane(box,
                new Plane(
                        new Vector3d(box.geometry.x1()+circleTop, box.geometry.y2()-circleTop, top),
                        new Vector3d(s, 0, 0),
                        new Vector3d(0, -s, 0)
                )
        ));

        // Cylinders surrounding the hole (diagonal)
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1()+circleTop, box.geometry.y1()+circleTop+s, box.geometry.z2()),
                        new Vector3d(s, -s, 0),
                        ball.getRadius()
                )
        ));
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2()-circleTop-s, box.geometry.y1()+circleTop, box.geometry.z2()),
                        new Vector3d(s, s, 0),
                        ball.getRadius()
                )
        ));
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x2()-circleTop, box.geometry.y2()-circleTop-s, box.geometry.z2()),
                        new Vector3d(-s, s, 0),
                        ball.getRadius()
                )
        ));
        collisions.add(ballSphere, new CollisionCylinder(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1()+circleTop+s, box.geometry.y2()-circleTop, box.geometry.z2()),
                        new Vector3d(-s, -s, 0),
                        ball.getRadius()
                )
        ));

        // Inverted cylinder through the hole
        collisions.add(ballSphere, new CollisionCylinderInverted(box,
                new Cylinder(
                        new Vector3d(box.geometry.x1()+0.5, box.geometry.x2()+0.5, box.geometry.z1()-ball.getRadius()),
                        new Vector3d(0, 0, box.geometry.displacement.z+ball.getRadius()),
                        box.getRadius() - ball.getRadius()
                )
        ));

        addFloorBoxSides(box);

        triggers.add(ballSphere, new GoalTrigger(new CollisionPlane(box, new Plane(
                box.geometry.position,
                new Vector3d(box.geometry.displacement.x, 0, 0),
                new Vector3d(0, box.geometry.displacement.y, 0)
        )), box));
    }
    public void addBallCollision(Ball ball) {
        collisions.add(ballSphere, new CollisionSphere(ball,
                new Sphere(
                        ball.getPosition(),
                        ball.getRadius() + this.ball.getRadius()
                )
        ));
    }
}
