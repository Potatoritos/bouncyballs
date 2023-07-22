package collision;

import game.Ball;
import game.HoleBox;
import org.joml.Vector3d;

/**
 * Trigger that marks the ball and associated hole as having reached their goal.
 * Kills any balls that do not match with the color of the hole
 */
public class GoalTrigger extends CollisionTrigger {
    private final HoleBox parent;
    public GoalTrigger(CollisionObject collisionObject, HoleBox parent) {
        super(collisionObject);
        this.parent = parent;
    }
    public void onCollision(Ball ball) {
        if (ball.getHoleColor() == parent.getHoleColor()) {
            ball.setHasReachedGoal(true);
            parent.setHasReachedGoal(true);
        } else if (!ball.isInExplosionAnimation()) {
            ball.triggerExplosionAnimation(new Vector3d(parent.geometry.displacement).mul(0.5).add(parent.geometry.position));
        }
    }
}
