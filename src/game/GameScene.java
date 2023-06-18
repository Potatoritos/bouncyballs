package game;

import graphics.NanoVGContext;
import org.joml.Vector4f;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static math.MathUtil.cubicInterpolation;
import static math.MathUtil.cutMaxMin;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.nanovg.NanoVG.*;

public class GameScene extends Scene {
    private final LevelScene levelScene;
    private int windowWidth;
    private int windowHeight;
    private final FrameTimer swipeTimer;
    private final FrameTimer levelTitleTimer;
    private boolean isLevelResetting;
    private boolean isLevelAdvancing;
    private boolean isLevelSelected;
    private boolean inLevelSelect;
    private boolean hasEscaped;
    private final ArrayList<Level> levels;
    private int selectedLevelIndex;
    public GameScene(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        levelScene = new LevelScene(windowWidth, windowHeight);

        swipeTimer = new FrameTimer(120);
        levelTitleTimer = new FrameTimer(432);

        levels = new ArrayList<>();
        loadLevels();

        setInLevelSelect(true);
        isLevelSelected = false;
        levelScene.loadLevel(levels.get(selectedLevelIndex));
    }
    public void setInLevelSelect(boolean value) {
        inLevelSelect = value;
        levelScene.setPreviewMode(value);
    }
    public void loadLevels() {
        levels.clear();
        File levelsDirectory = new File("assets/levels");
        File[] levelFiles = levelsDirectory.listFiles();
        if (levelFiles == null || levelFiles.length == 0) {
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
        levelTitleTimer.update();
        if (!swipeTimer.isActive()) {
            if (inLevelSelect && !isLevelSelected) {
                if (input.isKeyPressed(InputState.SCROLLWHEEL_UP)) {
                    selectedLevelIndex = Math.max(selectedLevelIndex-1, 0);
                    levelScene.loadLevel(levels.get(selectedLevelIndex));
                }
                if (input.isKeyPressed(InputState.SCROLLWHEEL_DOWN)) {
                    selectedLevelIndex = Math.min(selectedLevelIndex+1, levels.size()-1);
                    levelScene.loadLevel(levels.get(selectedLevelIndex));
                }
                if (input.isKeyPressed(InputState.MOUSE_BUTTON_LEFT)) {
                    isLevelSelected = true;
                    swipeTimer.start();
                    levelTitleTimer.start();
                }
            }
            if (input.isKeyPressed(GLFW_KEY_ESCAPE)) {
                swipeTimer.start();
                hasEscaped = true;
            }
            if (input.isKeyPressed(GLFW_KEY_E)) {
                swipeTimer.start();
                isLevelAdvancing = true;
            }
            if (!inLevelSelect && input.isKeyPressed(input.getResetKey()) || levelScene.hasDied()) {
                isLevelResetting = true;
                if (!swipeTimer.isActive()) {
                    swipeTimer.start();
                }
            }
        }
        if (isLevelResetting || isLevelSelected || hasEscaped) {
            if (swipeTimer.getFrame() == 49) {
                levelScene.setPaused(true);
                if (isLevelResetting) {
                    levelScene.reset();
                }
                if (isLevelSelected) {
                    levelScene.loadLevel(levels.get(selectedLevelIndex));
                    setInLevelSelect(false);
                }
                if (hasEscaped) {
                    setInLevelSelect(true);
                    levelScene.reset();
                }
            }
            if (swipeTimer.isOnLastFrame()) {
                levelScene.setPaused(false);
                isLevelResetting = false;
                isLevelSelected = false;
                hasEscaped = false;
            }
        } else if (isLevelAdvancing) {
            if (swipeTimer.isOnLastFrame()) {
                isLevelAdvancing = false;
            }
        }
    }
    @Override
    public void render() {
        levelScene.render();
    }

    @Override
    public void nvgRender(NanoVGContext nvg) {
        if (inLevelSelect || levelTitleTimer.isActive()) {
            Vector4f color = new Vector4f(Colors.backgroundDarker);
            if (levelTitleTimer.isActive() && levelTitleTimer.getFrame() >= 288) {
                color.w = 1 - (levelTitleTimer.getFrame()-288f) / 144;
            }
            nvg.setFontFace("montserrat");
            nvg.setFontSize(120);
            nvg.setTextAlign(NVG_ALIGN_LEFT);
            nvg.setFillColor(color);
            nvg.drawText(nvg.left(), nvg.bottom(), String.format("level %02d", selectedLevelIndex+1));
        }
        if (inLevelSelect) {
//            nvg.drawImage(nvg.escapeImage, nvg.left(), nvg.top(), 1);
            nvg.drawImage(nvg.mouse1Image, nvg.left()+nvg.getWidth()*0.27f, nvg.bottom()-nvg.adjustedSize(nvg.mouse1Image.getHeight()), 1);
            nvg.drawImage(nvg.mousewheelImage, nvg.right()-nvg.adjustedSize(nvg.mousewheelImage.getWidth()), nvg.bottom()-nvg.adjustedSize(80), 1);

            float scrollPosition = (float)selectedLevelIndex / Math.max(1, levels.size()-1);
            if (levels.size() == 1) {
                scrollPosition = 1;
            }
            nvg.setStrokeWidth(nvg.adjustedSize(6));
            nvg.setStrokeColor(Colors.backgroundDarker);
            float scrollX = nvg.right() - nvg.adjustedSize(42);
            float scrollY = nvg.bottom() - nvg.adjustedSize(100);
            nvg.drawLine(scrollX, nvg.top(), scrollX, scrollY);
            scrollY -= nvg.top();

            nvg.setFillColor(Colors.backgroundDarker);
            nvg.fillCircle(scrollX, nvg.top() + scrollPosition*scrollY, nvg.adjustedSize(20));

//                nvgImagePattern(nvg, left + windowWidth*0.1f, bottom, windowWidth*83f/1920, windowWidth*68f/1920, 0, resources.getEscapeImage(), 1f, imagePaint);

        }
        if (swipeTimer.isActive()) {
            if (isLevelResetting || isLevelSelected || hasEscaped) {
                nvg.setFillColor(Colors.black);
                if (swipeTimer.getFrame() <= 48) {
                    nvg.fillRect(0, 0, windowWidth * cubicInterpolation((float)swipeTimer.getFrame() / 48), windowHeight);
                } else if (swipeTimer.getFrame() <= 72) {
                    nvg.fillRect(0, 0, windowWidth, windowHeight);
                } else {
                    nvg.fillRect(windowWidth * cubicInterpolation((float) (swipeTimer.getFrame()-72) / 48), 0, windowWidth, windowHeight);
                }
            } else if (isLevelAdvancing) {
                nvg.setFillColor(Colors.pink);
                if (swipeTimer.getFrame() <= 48) {
                    nvg.fillRect(0, 0, windowWidth, windowHeight * cubicInterpolation((float)swipeTimer.getFrame() / 48));
                } else if (swipeTimer.getFrame() <= 72) {
                    nvg.fillRect(0, 0, windowWidth, windowHeight);
                } else {
                    nvg.fillRect(0, windowHeight * cubicInterpolation((float)(swipeTimer.getFrame()-72) / 48), windowWidth, windowHeight);
                }
            }
        }
    }
    @Override
    public void delete() {
        levelScene.delete();
    }
}
