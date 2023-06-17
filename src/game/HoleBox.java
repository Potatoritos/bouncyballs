package game;

import org.joml.Vector3f;
import shape.Line3;

public class HoleBox extends Box {
    private double radius;
    private int holeColor;
    private boolean hasReachedGoal;
    private final FrameTimer coverTimer;
    public final HoleBoxCover cover;
    public HoleBox(Line3 geometry, double radius) {
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
            coverTimer.update();
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
