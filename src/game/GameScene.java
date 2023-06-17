package game;

import org.lwjgl.nanovg.NVGColor;

import static math.MathUtil.cubicInterpolation;
import static math.MathUtil.cutMaxMin;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;

public class GameScene extends Scene {
    private final LevelScene levelScene;
    private Level level;
    private int windowWidth;
    private int windowHeight;
    private long nvg;
    private NVGColor nvgColor;
    private final FrameTimer swipeTimer;
    private boolean isLevelResetting;
    private boolean isLevelAdvancing;
    public GameScene(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        levelScene = new LevelScene(windowWidth, windowHeight);
        level = Level.fromFile("level0.txt");
        levelScene.loadLevel(level);

        nvg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        nvgColor = NVGColor.create();

        swipeTimer = new FrameTimer(120);
    }
    @Override
    public void handleWindowResize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        super.handleWindowResize(width, height);
        levelScene.handleWindowResize(width, height);
    }
    @Override
    public void update(InputState input) {
        levelScene.update(input);
        swipeTimer.update();
        if (input.isKeyPressed(input.getResetKey()) || levelScene.hasDied()) {
            isLevelResetting = true;
            if (!swipeTimer.isActive()) {
                swipeTimer.start();
            }
        }
        if (isLevelResetting) {
            if (swipeTimer.getFrame() == 49) {
                levelScene.setPaused(true);
                levelScene.reset();
            }
            if (swipeTimer.getFrame() == 119) {
                levelScene.setPaused(false);
                isLevelResetting = false;
            }
        } else if (isLevelAdvancing) {
            if (swipeTimer.getFrame() == 119) {
                isLevelAdvancing = false;
            }
        }
        if (input.isKeyPressed(GLFW_KEY_E)) {
            swipeTimer.start();
            isLevelAdvancing = true;
        }
    }
    @Override
    public void render() {
        levelScene.render();

        if (swipeTimer.isActive()) {
            nvgBeginFrame(nvg, windowWidth, windowHeight, 1);
            nvgBeginPath(nvg);
            if (isLevelResetting) {
                if (swipeTimer.getFrame() <= 48) {
                    nvgRect(nvg, 0, 0, windowWidth * cubicInterpolation((float) swipeTimer.getFrame() / 48), windowHeight);
                } else if (swipeTimer.getFrame() <= 72) {
                    nvgRect(nvg, 0, 0, windowWidth, windowHeight);
                } else {
                    nvgRect(nvg, windowWidth * cubicInterpolation((float) (swipeTimer.getFrame()-72) / 48), 0, windowWidth, windowHeight);
                }
                nvgFillColor(nvg, nvgRGB((byte)0, (byte)0, (byte)0, nvgColor));
            } else if (isLevelAdvancing) {
                if (swipeTimer.getFrame() <= 48) {
                    nvgRect(nvg, 0, 0, windowWidth, windowHeight * cubicInterpolation((float) swipeTimer.getFrame() / 48));
                } else if (swipeTimer.getFrame() <= 72) {
                    nvgRect(nvg, 0, 0, windowWidth, windowHeight);
                } else {
                    nvgRect(nvg, 0, windowHeight * cubicInterpolation((float) (swipeTimer.getFrame()-72) / 48), windowWidth, windowHeight);
                }
                nvgFillColor(nvg, nvgRGB((byte)0xE7, (byte)0x84, (byte)0xFF, nvgColor));
            }
            nvgFill(nvg);
            nvgEndFrame(nvg);
        }

    }
    @Override
    public void delete() {
        levelScene.delete();
    }
}
