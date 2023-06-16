package game;

public class FrameTimer {
    private int frame;
    private final int totalFrames;
    public FrameTimer(int totalFrames) {
        this.totalFrames = totalFrames;
        frame = totalFrames;
    }
    public int getFrame() {
        return frame;
    }
    public int getTotalFrames() {
        return totalFrames;
    }
    public void start() {
        start(1);
    }
    public void start(int startFrame) {
        frame = startFrame;
    }
    public void update() {
        frame = Math.min(totalFrames, frame+1);
    }
    public boolean isActive() {
        return frame < totalFrames;
    }
}