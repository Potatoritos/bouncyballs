package collision;

import shape.Sphere;

import java.util.ArrayList;

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
