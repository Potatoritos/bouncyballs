package geometry;

import org.joml.Vector2d;

import static geometry.Geometry.*;

public class CollisionHandlerOld {
    private final Vector2d result;
    private final Vector2d newPosition;
    private final Vector2d newVelocity;
    private final Vector2d normal;
    private final Vector2d tangent;
    private final Vector2d boxPosition;
    private final Vector2d ballPosition;
    private final Vector2d reflectionNormal;
    private final Vector2d reflectionTangent;
    private final Vector2d u;
    private double minLengthX;
    private double minLengthY;
    private double minLengthCircle;
    private int numberCollisions;
    public CollisionHandlerOld() {
        result = new Vector2d();
        newPosition = new Vector2d();
        newVelocity = new Vector2d();
        normal = new Vector2d();
        tangent = new Vector2d();
        boxPosition = new Vector2d();
        ballPosition = new Vector2d();
        reflectionNormal = new Vector2d();
        reflectionTangent = new Vector2d();
        u = new Vector2d();
        reset();
    }
    public void reset() {
        minLengthX = Double.POSITIVE_INFINITY;
        minLengthY = Double.POSITIVE_INFINITY;
        newPosition.zero();
        newVelocity.zero();
        numberCollisions = 0;
//        minLengthCircle =
//        System.out.println("==========");
    }
    public boolean hasCollidedX() {
        return minLengthX != Double.POSITIVE_INFINITY;
    }
    public boolean hasCollidedY() {
        return minLengthY != Double.POSITIVE_INFINITY;
    }
//    private boolean
    public void applyResult(Ball ball) {
//        if (hasCollidedX()) {
//            ball.velocity.x = newVelocity.x;
//            ball.position.x = newPosition.x;
////            System.out.printf("hascollidedx %f %f\n", ball.velocity.x, ball.position.x);
//        }
//        if (hasCollidedY()) {
//            ball.velocity.y = newVelocity.y;
//            ball.position.y = newPosition.y;
//        }
        if (numberCollisions > 0) {
            newPosition.mul(1.0/numberCollisions);
            newVelocity.mul(1.0/numberCollisions);
            ball.position.x = newPosition.x;
            ball.position.y = newPosition.y;
            ball.velocity.x = newVelocity.x;
            ball.velocity.y = newVelocity.y;
        }
    }
    public void addCollision(Vector2d position, Vector2d velocity) {
        newPosition.add(position);
        newVelocity.add(velocity);
        numberCollisions++;
    }
    public void handleCandidateX(double length, double positionX, double velocityX) {
        if (length < minLengthX) {
            minLengthX = length;
            newPosition.x = positionX;
            newVelocity.x = velocityX;
//            System.out.printf("handleCandidateX %f %f\n", positionX, velocityX);

        }
    }
    public void handleCandidateY(double length, double positionY, double velocityY) {
        if (length < minLengthY) {
            minLengthY = length;
            newPosition.y = positionY;
            newVelocity.y = velocityY;
//            System.out.printf("handleCandidateY %f %f\n", positionY, velocityY);
        }
    }
    private void handleCircleCircleCollision(Ball ball, double positionX, double positionY) {
        boxPosition.set(positionX, positionY);
        result.sub(boxPosition, normal).normalize();
        u.set(ball.velocity.x, ball.velocity.y);

        if (Math.abs(u.angle(normal)) >= Math.PI/2) {
//            double factor = 1;
//            if (Math.abs(u.angle(normal)) >= 3*Math.PI/4) {
//                factor = 0.5;
//            }

//            ballPosition.set(ball.position.x, ball.position.y);
            double lengthX = Math.abs(result.x - ball.position.x);
            double lengthY = Math.abs(result.y - ball.position.y);

//            result.sub(ballPosition, u);



//            double lengthToCompensate = u.length() - ball.velocity.length();

//            // set reflection to u reflected by the normal
//            // this is given by u - (2)(u•normal)(normal)
//            reflection.set(u);
//            normal.mul(2*u.dot(normal), u);
//            reflection.sub(u);

            tangent.set(normal).perpendicular();

            project(u, normal, reflectionNormal);
            project(u, tangent, reflectionTangent);
            u.set(reflectionNormal.negate().mul(0.5)).add(reflectionTangent);
            ball.velocity.y = u.y;
            ball.velocity.x = u.x;
            ball.position.x = result.x;
            ball.position.y = result.y;
            addCollision(result, u);
//            handleCandidateX(lengthX, result.x, u.x);
//            handleCandidateY(lengthY, result.y, u.y);

//            reflection.normalize(ball.velocity.length()*factor);
//            if (Double.isNaN(reflection.x) || Double.isNaN(reflection.y)) {
//                reflection.set(0, 0);
//            }
//            System.out.printf("CIRCLE pos %f %f | ilength %f %f | ballpos %f %f | ballvel %f %f | result %f %f | reflection %f %f | normal %f %f\n", positionX, positionY, lengthX, lengthY, ball.position.x, ball.position.y, ball.velocity.x, ball.velocity.y, result.x, result.y, reflection.x, reflection.y, normal.x, normal.y);
//            handleCandidateX(lengthX, result.x + lengthToCompensate*reflection.x, reflection.x);
//            handleCandidateY(lengthY, result.y + lengthToCompensate*reflection.y, reflection.y);
        }
    }
    public void processCollision(Ball ball, Box box) {
        boolean wall = false;
        if (
                (ball.velocity.x > 0 && intersectionLineWallX(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, box.position.x-ball.getRadius(), box.dimensions.y, box.position.y, result))
                || (ball.velocity.x < 0 && intersectionLineWallX(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, box.position.x+box.dimensions.x+ball.getRadius(), box.dimensions.y, box.position.y, result))
        ) {
            double length = Math.abs(result.x - ball.position.x);
            u.set(-ball.velocity.x/2, ball.velocity.y);
            ball.velocity.x = u.x;
            ball.velocity.y = u.y;
            addCollision(result, u);
//            handleCandidateX(length, result.x, -ball.velocity.x/2);
//            System.out.printf("WALL %f | ballpos %f %f\n", length, ball.position.x, ball.position.y);
            System.out.println("WALL X");
        }

        if (
                (ball.velocity.y > 0 && intersectionLineWallY(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, box.dimensions.x, box.position.x, box.position.y - ball.getRadius(), result))
                || (ball.velocity.y < 0 && intersectionLineWallY(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, box.dimensions.x, box.position.x, box.position.y + box.dimensions.y + ball.getRadius(), result))
        ) {
//            System.out.println("wally");
            double length = Math.abs(result.y - ball.position.y);
            u.set(ball.velocity.x, -ball.velocity.y/2);
            ball.velocity.x = u.x;
            ball.velocity.y = u.y;
            addCollision(result, u);
//            handleCandidateY(length, result.y, -ball.velocity.y/2);
            System.out.println("WALL Y");
        }

        if (intersectionLineCircle(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, ball.getRadius(), box.position.x, box.position.y, result)) {
            handleCircleCircleCollision(ball, box.position.x, box.position.y);
            System.out.println("CIRCLE 1");
        }
        if (intersectionLineCircle(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, ball.getRadius(), box.position.x+box.dimensions.x, box.position.y, result)) {
            handleCircleCircleCollision(ball, box.position.x, box.position.y);
            System.out.println("CIRCLE 2");
        }
        if (intersectionLineCircle(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, ball.getRadius(), box.position.x+box.dimensions.x, box.position.y+box.dimensions.y, result)) {
            handleCircleCircleCollision(ball, box.position.x, box.position.y);
            System.out.println("CIRCLE 3");
        }
        if (intersectionLineCircle(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, ball.getRadius(), box.position.x, box.position.y+box.dimensions.y, result)) {
            handleCircleCircleCollision(ball, box.position.x, box.position.y);
            System.out.println("CIRCLE 4");
        }
    }

}
