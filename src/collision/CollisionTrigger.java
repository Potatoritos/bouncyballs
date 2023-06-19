package collision;

import game.Ball;

/**
 * A trigger that runs a function whenever a ball intersects with the underlying collision object.
 * Does not rebound ball (it lets balls pass through)
 */
public abstract class CollisionTrigger implements HasCollisionObject {
    public final CollisionObject collisionObject;
    private boolean isActive;
    public CollisionTrigger(CollisionObject collisionObject) {
        this.collisionObject = collisionObject;
        isActive = true;
    }
    public boolean isActive() {
        return isActive;
    }
    public void disable() {
        isActive = false;
    }

    /**
     * Defines what happens when a ball collides with this trigger
     * @param ball the ball that collided with this trigger
     */
    public abstract void onCollision(Ball ball);
    public CollisionObject getCollisionObject() {
        return collisionObject;
    }
}
