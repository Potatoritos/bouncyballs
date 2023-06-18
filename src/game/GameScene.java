package game;

import org.lwjgl.nanovg.NVGColor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static math.MathUtil.cubicInterpolation;
import static math.MathUtil.cutMaxMin;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.nanovg.NanoVG.*;

public class GameScene extends Scene {
    private final LevelScene levelScene;
    private Level level;
    private int windowWidth;
    private int windowHeight;
    private NVGColor nvgColor;
    private final FrameTimer swipeTimer;
    private boolean isLevelResetting;
    private boolean isLevelAdvancing;
    private boolean inLevelSelect;
    private final ArrayList<Level> levels;
    public GameScene(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        levelScene = new LevelScene(windowWidth, windowHeight);
        level = Level.fromFile("level0.txt");
        levelScene.loadLevel(level);

        nvgColor = NVGColor.create();

        swipeTimer = new FrameTimer(120);

        levels = new ArrayList<>();
        loadLevels();

        setInLevelSelect(true);
    }
    public void setInLevelSelect(boolean value) {
        inLevelSelect = value;
        levelScene.setPreviewMode(value);
    }
    public void loadLevels() {
        levels.clear();
        File levelsDirectory = new File("assets/levels");
        File[] levelFiles = levelsDirectory.listFiles();
        if (levelFiles == null) {
            throw new RuntimeException("No level files found!");
        }
        Arrays.sort(levelFiles);
        for (File levelFile : levelFiles) {
            levels.add(Level.fromFile(levelFile.getName()));
        }
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
            if (swipeTimer.isOnLastFrame()) {
                levelScene.setPaused(false);
                isLevelResetting = false;
            }
        } else if (isLevelAdvancing) {
            if (swipeTimer.isOnLastFrame()) {
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
    }

    @Override
    public void nvgRender(long nvg) {
        if (inLevelSelect) {
            nvgFontFace(nvg, "montserrat");
            nvgFontSize(nvg, 144*windowWidth / 1920);
            nvgTextAlign(nvg, NVG_ALIGN_CENTER);
            nvgFillColor(nvg, nvgRGB((byte)0, (byte)0, (byte)0, nvgColor));
            nvgText(nvg, windowWidth/2f, windowHeight*0.93f, "level 01");
        }
        if (swipeTimer.isActive()) {
            nvgBeginFrame(nvg, windowWidth, windowHeight, 1);
            nvgBeginPath(nvg);
            if (isLevelResetting) {
                if (swipeTimer.getFrame() <= 48) {
                    nvgRect(nvg, 0, 0, windowWidth * cubicInterpolation((float)swipeTimer.getFrame() / 48), windowHeight);
                } else if (swipeTimer.getFrame() <= 72) {
                    nvgRect(nvg, 0, 0, windowWidth, windowHeight);
                } else {
                    nvgRect(nvg, windowWidth * cubicInterpolation((float) (swipeTimer.getFrame()-72) / 48), 0, windowWidth, windowHeight);
                }
                nvgFillColor(nvg, nvgRGB((byte)0, (byte)0, (byte)0, nvgColor));
            } else if (isLevelAdvancing) {
                if (swipeTimer.getFrame() <= 48) {
                    nvgRect(nvg, 0, 0, windowWidth, windowHeight * cubicInterpolation((float)swipeTimer.getFrame() / 48));
                } else if (swipeTimer.getFrame() <= 72) {
                    nvgRect(nvg, 0, 0, windowWidth, windowHeight);
                } else {
                    nvgRect(nvg, 0, windowHeight * cubicInterpolation((float)(swipeTimer.getFrame()-72) / 48), windowWidth, windowHeight);
                }
                nvgFillColor(nvg, nvgRGB((byte)0xE7, (byte)0x84, (byte)0xFF, nvgColor));
            }
            nvgFill(nvg);
        }
    }
    @Override
    public void delete() {
        levelScene.delete();
    }
}
