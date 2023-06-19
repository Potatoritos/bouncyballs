package game;

/**
 * A timer that continuously increments its frame (modulo totalFrames)
 */
public class ContinuousFrameTimer {
    private int frame;
    private final int totalFrames;
    private boolean isActive;

    /**
     * @param totalFrames the number to modulo the frames
     */
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

    /**
     * @return the percentage denoting how close the timer is to ending
     */
    public double percentage() {
        return (double)frame / totalFrames;
    }

    /**
     * @return the percentage denoting how close the timer is to ending
     */
    public float fpercentage() {
        return (float)frame / totalFrames;
    }
}
