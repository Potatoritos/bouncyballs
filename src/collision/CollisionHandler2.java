package collision;

import game.Ball;
import game.Box;
import shape.Line2;
import org.joml.Vector2d;

import java.util.ArrayList;

import static math.Geometry.*;
import static math.MathUtil.withinEpsilon;

public class CollisionHandler2 {
    private Ball ball;
    private final Line2 ballMotion;
    private final ArrayList<CollisionObject2> collisionObjects;

    private double minDistance;
    private final Vector2d minIntersection;
    private CollisionObject2 minCollisionObject;
    public CollisionHandler2() {
        collisionObjects = new ArrayList<>();
        ballMotion = new Line2();
        minIntersection = new Vector2d();
    }
    public void reset() {
        collisionObjects.clear();
    }
    public void setBall(Ball ball) {
        this.ball = ball;
        ballMotion.position.set(ball.geometry.position.x, ball.geometry.position.y);
        ballMotion.displacement.set(ball.velocity.x, ball.velocity.y);
    }
    private void addCollisionObject(CollisionObject2 object) {
        double distance = object.distance(ballMotion.position), length = ballMotion.displacement.length();
        if (distance <= length+0.2) {
            collisionObjects.add(object);
        }
    }

    public void addBox(Box box) {
        addCollisionObject(new CollisionLineX(box, new Vector2d(box.geometry.position.x-ball.getRadius(), box.geometry.position.y), new Vector2d(0, box.geometry.displacement.y), 1));
        addCollisionObject(new CollisionLineX(box, new Vector2d(box.geometry.position.x+box.geometry.displacement.x+ball.getRadius(), box.geometry.position.y), new Vector2d(0, box.geometry.displacement.y), -1));
        addCollisionObject(new CollisionLineY(box, new Vector2d(box.geometry.position.x, box.geometry.position.y-ball.getRadius()), new Vector2d(box.geometry.displacement.x, 0), 1));
        addCollisionObject(new CollisionLineY(box, new Vector2d(box.geometry.position.x, box.geometry.position.y+box.geometry.displacement.y+ball.getRadius()), new Vector2d(box.geometry.displacement.x, 0), -1));

        addCollisionObject(new CollisionCircle(box, ball.getRadius(), new Vector2d(box.geometry.position.x, box.geometry.position.y)));
        addCollisionObject(new CollisionCircle(box, ball.getRadius(), new Vector2d(box.geometry.position.x+box.geometry.displacement.x, box.geometry.position.y)));
        addCollisionObject(new CollisionCircle(box, ball.getRadius(), new Vector2d(box.geometry.position.x, box.geometry.position.y+box.geometry.displacement.y)));
        addCollisionObject(new CollisionCircle(box, ball.getRadius(), new Vector2d(box.geometry.position.x+box.geometry.displacement.x, box.geometry.position.y+box.geometry.displacement.y)));
    }

    public void processCollisions() {
        int i = 0;
        Vector2d intersection = new Vector2d();
//        while (ballMotion.displacement.lengthSquared() > 0 && ++i < 7) {
        while (i++ < 7) {
            minDistance = Double.POSITIVE_INFINITY;
            for (CollisionObject2 object : collisionObjects) {
                boolean intersects = object.intersect(ballMotion, intersection);
                if (!intersects) {
                    continue;
                }

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

        ball.geometry.position.x = ballMotion.position.x;
        ball.geometry.position.y = ballMotion.position.y;
        ball.velocity.x = ballMotion.displacement.x;
        ball.velocity.y = ballMotion.displacement.y;
    }
}
