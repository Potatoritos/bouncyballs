package collision;

import game.Ball;

public abstract class CollisionTrigger {
    public final CollisionObject3 collisionObject;
    private boolean isActive;
    public CollisionTrigger(CollisionObject3 collisionObject) {
        this.collisionObject = collisionObject;
        isActive = true;
    }
    public boolean isActive() {
        return isActive;
    }
    public void disable() {
        isActive = false;
    }
    public abstract void onCollision(Ball ball);
}
