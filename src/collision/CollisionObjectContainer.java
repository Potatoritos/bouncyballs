package collision;

import shape.Sphere;

import java.util.ArrayList;

/**
 * Stores collision objects; rejects collision objects that aren't close to a ball
 * @param <T> the type of collision object that is stored
 */
public class CollisionObjectContainer <T extends HasCollisionObject> {
    public final ArrayList<T> objects;
    public CollisionObjectContainer() {
        objects = new ArrayList<>();
    }
    public void add(Sphere ballSphere,T object) {
        if (object.getCollisionObject().isNearby(ballSphere)) {
            objects.add(object);
        }
    }
}
