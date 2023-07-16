package game;

import shape.Line3d;

/**
 * Represents the holes you have to get the balls into
 */
public class HoleBox extends Box {
    private double radius;
    private int holeColor;
    private boolean hasReachedGoal;
    private final FrameTimer coverTimer;
    public final HoleBoxCover cover;

    /**
     * @param geometry the dimensions of the box
     * @param radius the radius of the circular hole
     */
    public HoleBox(Line3d geometry, double radius) {
        super(geometry);
        this.radius = radius;
        hasReachedGoal = false;
        coverTimer = new FrameTimer(48);
        coverTimer.start();
        cover = new HoleBoxCover();
        cover.position.set(geometry.position);
    }
    @Override
    public void update() {
        cover.position.set(geometry.position);
        if (hasReachedGoal) {
            coverTimer.advanceFrame();
            cover.position.add(0, 0, (float)geometry.displacement.z*coverTimer.getFrame()/coverTimer.getTotalFrames());
//            cover.color1.x = 1 - (float)coverTimer.getFrame()/coverTimer.getTotalFrames();
        }
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
