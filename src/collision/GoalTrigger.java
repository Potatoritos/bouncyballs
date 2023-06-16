package collision;

import game.Ball;
import game.HoleBox;

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
            System.out.println("goal reached " + ball.getHoleColor());
        } else {
            ball.setIsDead(true);
            System.out.println("killed ball from goaltrigger");
        }
    }
}
