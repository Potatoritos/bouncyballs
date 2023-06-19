package collision;

import game.Ball;

/**
 * Trigger that kills the ball upon collision
 */
public class DeathTrigger extends CollisionTrigger {
    public DeathTrigger(CollisionObject collisionObject) {
        super(collisionObject);
    }
    public void onCollision(Ball ball) {
        ball.setIsDead(true);
    }
}
