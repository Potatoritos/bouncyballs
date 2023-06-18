package game;

public class ContinuousFrameTimer {
    private int frame;
    private final int totalFrames;
    private boolean isActive;
    public ContinuousFrameTimer(int totalFrames) {
        this.totalFrames = totalFrames;
    }
    public void update() {
        if (isActive) frame = (frame + 1) % totalFrames;
    }
    public void start() {
        isActive = true;
    }
    public void stop() {
        isActive = false;
    }
    public void setIsActive(boolean value) {
        this.isActive = value;
    }
    public boolean isActive() {
        return isActive;
    }
    public double percentage() {
        return (double)frame / totalFrames;
    }
    public float fpercentage() {
        return (float)frame / totalFrames;
    }
}
