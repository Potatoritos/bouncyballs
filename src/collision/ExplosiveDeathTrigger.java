package collision;

import game.Ball;

public class ExplosiveDeathTrigger extends CollisionTrigger {
    public ExplosiveDeathTrigger(CollisionObject collisionObject) {
        super(collisionObject);
    }
    public void onCollision(Ball ball) {
        if (!ball.isInExplosionAnimation()) {
            ball.triggerExplosionAnimation(ball.getPosition());
        }
    }
}
