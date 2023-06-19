package collision;

import game.Ball;
import game.HoleBox;
import org.joml.Vector3d;

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
        } else {
            ball.triggerExplosionAnimation(new Vector3d(parent.geometry.displacement).mul(0.5).add(parent.geometry.position));
        }
    }
}
