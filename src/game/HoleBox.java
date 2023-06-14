package game;

import geometry.Line3;

public class HoleBox extends Box {
    private double radius;
    public HoleBox(Line3 geometry, double radius) {
        super(geometry);
        this.radius = radius;
    }
    public double getRadius() {
        return radius;
    }
}
