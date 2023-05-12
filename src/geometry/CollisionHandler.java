package geometry;

import org.joml.Vector2d;

import java.util.ArrayList;

import static geometry.Geometry.*;
import static util.Util.withinEpsilon;

public class CollisionHandler {
    private Ball ball;
    private final Line ballMotion;
    private final ArrayList<CollisionObject> collisionObjects;

    private double minDistance;
    private final Vector2d minIntersection;
    private CollisionObject minCollisionObject;
    public CollisionHandler() {
        collisionObjects = new ArrayList<>();
        ballMotion = new Line();
        minIntersection = new Vector2d();
    }
    public void reset() {
        collisionObjects.clear();
//        System.out.println("=======================");
    }
    public void setBall(Ball ball) {
        this.ball = ball;
        ballMotion.position.set(ball.position.x, ball.position.y);
        ballMotion.displacement.set(ball.velocity.x, ball.velocity.y);
    }
    private void addCollisionObject(CollisionObject object) {
        double distance = object.distance(ballMotion.position), length = ballMotion.displacement.length();
        if (distance <= length+0.2) {
            collisionObjects.add(object);
        }
    }

    public void addBox(Box box) {
        addCollisionObject(new CollisionLineX(box, new Vector2d(box.position.x-ball.getRadius(), box.position.y), new Vector2d(0, box.dimensions.y), 1));
        addCollisionObject(new CollisionLineX(box, new Vector2d(box.position.x+box.dimensions.x+ball.getRadius(), box.position.y), new Vector2d(0, box.dimensions.y), -1));
        addCollisionObject(new CollisionLineY(box, new Vector2d(box.position.x, box.position.y-ball.getRadius()), new Vector2d(box.dimensions.x, 0), 1));
        addCollisionObject(new CollisionLineY(box, new Vector2d(box.position.x, box.position.y+box.dimensions.y+ball.getRadius()), new Vector2d(box.dimensions.x, 0), -1));

//        addCollisionObject(new CollisionCircle(box, ball.getRadius(), new Vector2d(box.position.x, box.position.y)));
//        addCollisionObject(new CollisionCircle(box, ball.getRadius(), new Vector2d(box.position.x+box.dimensions.x, box.position.y)));
//        addCollisionObject(new CollisionCircle(box, ball.getRadius(), new Vector2d(box.position.x, box.position.y+box.dimensions.y)));
//        addCollisionObject(new CollisionCircle(box, ball.getRadius(), new Vector2d(box.position.x+box.dimensions.x, box.position.y+box.dimensions.y)));
    }

    public void processCollisions() {
        int i = 0;
        Vector2d intersection = new Vector2d();
        while (ballMotion.displacement.lengthSquared() > 0 && ++i < 7) {
            minDistance = Double.POSITIVE_INFINITY;
            for (CollisionObject object : collisionObjects) {
                boolean intersects = object.intersect(ballMotion, intersection);
                if (!intersects) continue;

                double distance = distance(intersection, ballMotion.position);
                if (distance <= minDistance) {
                    minDistance = distance;
                    minIntersection.set(intersection);
                    minCollisionObject = object;
                }
            }

            if (minDistance == Double.POSITIVE_INFINITY) break;

            minCollisionObject.reflectLine(ballMotion, minIntersection, minDistance);
        }

        ball.position.x = ballMotion.position.x;
        ball.position.y = ballMotion.position.y;
        ball.velocity.x = ballMotion.displacement.x;
        ball.velocity.y = ballMotion.displacement.y;
        System.out.println(collisionObjects.size());
    }
}
