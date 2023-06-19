package game;

/**
 * A timer that counts frames
 */
public class FrameTimer {
    private int frame;
    private final int totalFrames;

    /**
     * @param totalFrames the total number of frames this timer will time before ending
     */
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
    public boolean isOnLastFrame() {
        return frame == totalFrames-1;
    }
    public void end() {
        frame = totalFrames;
    }
}
