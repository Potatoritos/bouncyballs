package game;

import org.joml.Vector2d;

import static util.Geometry.*;

public class CollisionHandler {
    private final Vector2d result;
    private final Vector2d newPosition;
    private final Vector2d newVelocity;
    private final Vector2d normal;
    private final Vector2d boxPosition;
    private final Vector2d ballPosition;
    private final Vector2d reflection;
    private final Vector2d u;
    private double minLengthX;
    private double minLengthY;
    public CollisionHandler() {
        result = new Vector2d();
        newPosition = new Vector2d();
        newVelocity = new Vector2d();
        normal = new Vector2d();
        boxPosition = new Vector2d();
        ballPosition = new Vector2d();
        reflection = new Vector2d();
        u = new Vector2d();
        reset();
    }
    public void reset() {
        minLengthX = Float.POSITIVE_INFINITY;
        minLengthY = Float.POSITIVE_INFINITY;
    }
    public boolean hasCollidedX() {
        return minLengthX != Float.POSITIVE_INFINITY;
    }
    public boolean hasCollidedY() {
        return minLengthY != Float.POSITIVE_INFINITY;
    }
    public void applyResult(Ball ball) {
        if (hasCollidedX()) {
            ball.velocity.x = newVelocity.x;
            ball.position.x = newPosition.x;
        }
        if (hasCollidedY()) {
            ball.velocity.y = newVelocity.y;
            ball.position.y = newPosition.y;
        }
    }
    public void handleCandidateX(double length, double positionX, double velocityX) {
        if (length < minLengthX) {
            minLengthX = length;
            newPosition.x = positionX;
            newVelocity.x = velocityX;
        }
    }
    public void handleCandidateY(double length, double positionY, double velocityY) {
        if (length < minLengthY) {
            minLengthY = length;
            newPosition.y = positionY;
            newVelocity.y = velocityY;
        }
    }
    private void handleCircleCircleCollision(Ball ball, double positionX, double positionY) {
        boxPosition.set(positionX, positionY);
        result.sub(boxPosition, normal).normalize();
        u.set(ball.velocity.x, ball.velocity.y);

        if (Math.abs(u.angle(normal)) >= 3*Math.PI/4) {
            ballPosition.set(ball.position.x, ball.position.y);
            double lengthX = Math.abs(result.x - ball.position.x);
            double lengthY = Math.abs(result.y - ball.position.y);

            result.sub(ballPosition, u);

            // set reflection to u reflected by the normal
            // this is given by u - (2)(u•normal)(normal);
            reflection.set(u);
            normal.mul(2*u.dot(normal), u);
            reflection.sub(u);

            reflection.normalize(ball.velocity.length()/2);

            handleCandidateX(lengthX, result.x, reflection.x);
            handleCandidateY(lengthY, result.y, reflection.y);
        }
    }
    public void processCollision(Ball ball, Box box) {
        if (
                (ball.velocity.x > 0 && intersectionRayWallX(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, box.position.x-ball.getRadius(), box.dimensions.y, box.position.y, result))
                || (ball.velocity.x < 0 && intersectionRayWallX(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, box.position.x+box.dimensions.x+ball.getRadius(), box.dimensions.y, box.position.y, result))
        ) {
            double length = Math.abs(result.x - ball.position.x);
            handleCandidateX(length, result.x, -ball.velocity.x/2);
        }

        if (
                (ball.velocity.y > 0 && intersectionRayWallY(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, box.dimensions.x, box.position.x, box.position.y - ball.getRadius(), result))
                || (ball.velocity.y < 0 && intersectionRayWallY(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, box.dimensions.x, box.position.x, box.position.y + box.dimensions.y + ball.getRadius(), result))
        ) {
            double length = Math.abs(result.y - ball.position.y);
            handleCandidateY(length, result.y, -ball.velocity.y/2);
        }

        if (intersectionRayCircle(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, ball.getRadius(), box.position.x, box.position.y, result)) {
            handleCircleCircleCollision(ball, box.position.x, box.position.y);
        }
        if (intersectionRayCircle(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, ball.getRadius(), box.position.x+box.dimensions.x, box.position.y, result)) {
            handleCircleCircleCollision(ball, box.position.x, box.position.y);
        }
        if (intersectionRayCircle(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, ball.getRadius(), box.position.x+box.dimensions.x, box.position.y+box.dimensions.y, result)) {
            handleCircleCircleCollision(ball, box.position.x, box.position.y);
        }
        if (intersectionRayCircle(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, ball.getRadius(), box.position.x, box.position.y+box.dimensions.y, result)) {
            handleCircleCircleCollision(ball, box.position.x, box.position.y);
        }
    }

}
