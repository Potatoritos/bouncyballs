package collision;

import game.Ball;

public class DeathTrigger extends CollisionTrigger {
    public DeathTrigger(CollisionObject3 collisionObject) {
        super(collisionObject);
    }
    public void onCollision(Ball ball) {
        ball.setIsDead(true);
    }
}
