package collision;

import game.Ball;
import game.HoleBox;
import org.joml.Vector3d;

public class GoalTrigger extends CollisionTrigger {
    private final HoleBox parent;
    public GoalTrigger(CollisionObject3 collisionObject, HoleBox parent) {
        super(collisionObject);
        this.parent = parent;
    }
    public void onCollision(Ball ball) {
        if (ball.getHoleColor() == parent.getHoleColor()) {
            ball.setHasReachedGoal(true);
            parent.setHasReachedGoal(true);
        } else {
            System.out.println("wrong hole");
            ball.triggerExplosionAnimation();
        }
    }
}
