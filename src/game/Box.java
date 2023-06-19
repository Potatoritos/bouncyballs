package game;

import collision.CollisionObjectContainer;
import math.Geometry;
import shape.Line3;
import org.joml.Vector3d;

/**
 * Represents any game object that is a rectangular prism (e.g, walls, floor)
 */
public class Box extends GameObject {
    public final Line3 geometry;
    public Box() {
        super();
        geometry = new Line3();
    }
    public Box(Line3 geometry) {
        this();
        this.geometry.set(geometry);
    }
    public Vector3d getPosition() {
        return geometry.position;
    }
    @Override
    public void reflectLine(Line3 line, Vector3d intersection, Vector3d normal) {
        Geometry.reflectLine(line, intersection, normal, 0.5);
    }
}
