package game;

import graphics.NanoVGContext;
import org.joml.Vector4f;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static math.MathUtil.cubicInterpolation;
import static math.MathUtil.cutMaxMin;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.nanovg.NanoVG.*;

public class GameScene extends Scene {
    private final LevelScene levelScene;
    private int windowWidth;
    private int windowHeight;
    private final FrameTimer horizontalSwipeTimer;
    private final FrameTimer verticalSwipeTimer;
    private final FrameTimer levelClearTimer;
    private final FrameTimer levelClearDelayTimer;
    private final FrameTimer enterLevelSelectTimer;
    private final FrameTimer enterLevelTimer;
    private final FrameTimer levelResetTimer;
    private final FrameTimer advanceTimer;
    private final FrameTimer levelTitleTimer;
    private final FrameTimer[] timers;
    private boolean isLevelResetting;
    private boolean isLevelAdvancing;
    private boolean isLevelSelected;
    private boolean inLevelSelect;
    private boolean inLevel;
    private boolean hasEscaped;
    private boolean inTransition;
    private boolean atEndOfLevels;
    private final ArrayList<Level> levels;
    private int selectedLevelIndex;
    public GameScene(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        levelScene = new LevelScene(windowWidth, windowHeight);

        horizontalSwipeTimer = new FrameTimer(120);
        verticalSwipeTimer = new FrameTimer(240);
        levelClearTimer = new FrameTimer(180);
        levelClearDelayTimer = new FrameTimer(48);
        enterLevelSelectTimer = new FrameTimer(49);
        enterLevelTimer = new FrameTimer(49);
        levelResetTimer = new FrameTimer(120);
        advanceTimer = new FrameTimer(240);
        levelTitleTimer = new FrameTimer(432);
        timers = new FrameTimer[] {horizontalSwipeTimer, verticalSwipeTimer, levelClearTimer, levelClearDelayTimer, enterLevelSelectTimer, enterLevelTimer, levelResetTimer, advanceTimer, levelTitleTimer};

        levels = new ArrayList<>();
        loadLevels();

        startEnterLevelSelect();
        isLevelSelected = false;
        levelScene.loadLevel(levels.get(selectedLevelIndex));
    }
    private void startEnterLevelSelect() {
        enterLevelSelectTimer.start();
        horizontalSwipeTimer.start();
        inTransition = true;
    }
    private void endEnterLevelSelect() {
        inLevel = false;
        inLevelSelect = true;
        levelScene.reset();
        levelScene.enterPreviewMode();
        levelTitleTimer.end();
        inTransition = false;
    }
    private void startLevelReset() {
        levelResetTimer.start();
        horizontalSwipeTimer.start();
        inTransition = true;
    }
    private void midLevelReset() {
        levelScene.reset();
        levelScene.setPaused(true);
    }
    private void endLevelReset() {
        levelScene.setPaused(false);
        inTransition = false;
    }
    private void startEnterLevel() {
        enterLevelTimer.start();
        horizontalSwipeTimer.start();
        inTransition = true;
    }
    private void endEnterLevel() {
        inLevelSelect = false;
        levelScene.exitPreviewMode();

        levelTitleTimer.start();
        inTransition = false;
        inLevel = true;
    }
    private void startLevelClearDelay() {
        levelClearDelayTimer.start();
        inTransition = true;
    }
    private void startLevelClear() {
        levelClearTimer.start();
    }
    private void midLevelClear() {
        advanceTimer.start();
        verticalSwipeTimer.start();
    }
    private void midAdvanceLevel() {
        if (selectedLevelIndex >= levels.size()-1) {
            atEndOfLevels = true;
            endEnterLevelSelect();
        } else {
            changeSelectedLevelIndex(selectedLevelIndex+1);
            levelScene.setPaused(true);
            levelTitleTimer.start();
        }
    }
    private void endAdvanceLevel() {
        if (!atEndOfLevels) {
            levelScene.setPaused(false);
        }
        inTransition = false;
    }
    private void changeSelectedLevelIndex(int index) {
        selectedLevelIndex = index;
        levelScene.loadLevel(levels.get(selectedLevelIndex));
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
        for (FrameTimer timer : timers) {
            timer.update();
        }

        if (enterLevelSelectTimer.isOnLastFrame()) {
            endEnterLevelSelect();
        }
        if (enterLevelTimer.isOnLastFrame()) {
            endEnterLevel();
        }
        if (levelClearDelayTimer.isOnLastFrame()) {
            startLevelClear();
        }
        if (levelResetTimer.getFrame() == 49) {
            midLevelReset();
        } else if (levelResetTimer.isOnLastFrame()) {
            endLevelReset();
        }
        if (levelClearTimer.getFrame() == 120) {
            midLevelClear();
        }
        if (advanceTimer.getFrame() == 169) {
            midAdvanceLevel();
        } else if (advanceTimer.isOnLastFrame()) {
            endAdvanceLevel();
        }

        if (!inTransition && !horizontalSwipeTimer.isActive() && !verticalSwipeTimer.isActive()) {
            if (inLevelSelect) {
                if (input.isPreviousLevelPressed()) {
                    changeSelectedLevelIndex(Math.max(selectedLevelIndex-1, 0));
                } else if (input.isNextLevelPressed()) {
                    changeSelectedLevelIndex(Math.min(selectedLevelIndex+1, levels.size()-1));
                } else if (input.isSelectLevelPressed()) {
                    startEnterLevel();
                }
            } else if (inLevel) {
                if (levelScene.hasWon() || input.isKeyPressed(GLFW_KEY_W)) {
                    startLevelClearDelay();
                } else if (input.isExitKeyPressed()) {
                    startEnterLevelSelect();
                } else if (levelScene.hasDied() || input.isResetKeyPressed()) {
                    startLevelReset();
                }
            }
        }

        levelScene.update(input);

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
            nvg.drawImage(nvg.mouse1Image, nvg.left()+nvg.getWidth()*0.27f, nvg.bottom()-nvg.adjustedSize(nvg.mouse1Image.getHeight())*0.75f, 0.75f);
            nvg.drawImage(nvg.mousewheelImage, nvg.right()-nvg.adjustedSize(nvg.mousewheelImage.getWidth()-10), nvg.bottom()-nvg.adjustedSize(60), 0.75f);

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
        if (horizontalSwipeTimer.isActive()) {
            nvg.setFillColor(Colors.black);
            if (horizontalSwipeTimer.getFrame() <= 48) {
                nvg.fillRect(0, 0, windowWidth * cubicInterpolation((float) horizontalSwipeTimer.getFrame() / 48), windowHeight);
            } else if (horizontalSwipeTimer.getFrame() <= 72) {
                nvg.fillRect(0, 0, windowWidth, windowHeight);
            } else {
                nvg.fillRect(windowWidth * cubicInterpolation((float) (horizontalSwipeTimer.getFrame() - 72) / 48), 0, windowWidth, windowHeight);
            }
        }
        if (levelClearTimer.isActive()) {
            nvg.setFillColor(Colors.pink);
            float x = levelClearTimer.fpercentage()*1.4f;
            float y = nvg.adjustedSize(-400 * x * (x - 2));
            nvg.fillRect(0, y, windowWidth, nvg.adjustedSize(300));

            nvg.setFontFace("montserrat");
            nvg.setFontSize(120);
            nvg.setTextAlign(NVG_ALIGN_CENTER);
            nvg.setFillColor(Colors.black);
            nvg.drawText(windowWidth/2, y+nvg.adjustedSize(190), "LEVEL CLEAR");
        }
        if (verticalSwipeTimer.isActive()) {
            nvg.setFillColor(Colors.black);
            if (verticalSwipeTimer.getFrame() <= 48) {
                nvg.fillRect(0, 0, windowWidth, windowHeight * cubicInterpolation((float)verticalSwipeTimer.getFrame() / 48));
            } else if (verticalSwipeTimer.getFrame() <= 192) {
                nvg.fillRect(0, 0, windowWidth, windowHeight);
            } else {
                nvg.fillRect(0, windowHeight * cubicInterpolation((float)(verticalSwipeTimer.getFrame()-192) / 48), windowWidth, windowHeight);
            }
        }
    }
    @Override
    public void delete() {
        levelScene.delete();
    }
}
