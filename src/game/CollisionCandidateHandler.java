package game;

import org.joml.Vector2f;

public class CollisionCandidateHandler {

    private final Vector2f newPosition;
    private final Vector2f newVelocity;
    private float minLength;
    public CollisionCandidateHandler() {
        newPosition = new Vector2f();
        newVelocity = new Vector2f();
        reset();
    }
    public void reset() {
        minLength = Float.POSITIVE_INFINITY;
    }
    public void applyResult(Ball ball) {
        ball.velocity.x = newVelocity.x;
        ball.velocity.y = newVelocity.y;
        if (!Float.isNaN(newPosition.x)) ball.position.x = newPosition.x;
        if (!Float.isNaN(newPosition.y)) ball.position.y = newPosition.y;
    }
    public boolean hasCollided() {
        return minLength != Float.POSITIVE_INFINITY;
    }
    public void handleCandidate(Vector2f positionCandidate, float velocityCandidateX, float velocityCandidateY) {
        float length = positionCandidate.lengthSquared();
        if (length < minLength) {
            minLength = length;
            newPosition.set(positionCandidate);
            newVelocity.set(velocityCandidateX, velocityCandidateY);
        }
    }
}
