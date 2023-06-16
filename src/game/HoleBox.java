package game;

import shape.Line3;

public class HoleBox extends Box {
    private double radius;
    private int holeColor;
    private boolean hasReachedGoal;
    public HoleBox(Line3 geometry, double radius) {
        super(geometry);
        this.radius = radius;
        hasReachedGoal = false;
    }
    public void setHasReachedGoal(boolean value) {
        hasReachedGoal = value;
    }
    public boolean hasReachedGoal() {
        return hasReachedGoal;
    }
    public void setHoleColor(int value) {
        holeColor = value;
    }
    public int getHoleColor() {
        return holeColor;
    }
    public double getRadius() {
        return radius;
    }
}
