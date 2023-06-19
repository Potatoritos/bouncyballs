package game;

import graphics.NanoVGContext;
import org.joml.Vector2d;
import org.joml.Vector4f;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static math.MathUtil.cubicInterpolation;
import static math.MathUtil.cutMaxMin;
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
    private final FrameTimer enterMainMenuTimer;
    private final FrameTimer levelTitleTimer;
    private final FrameTimer[] timers;
    private boolean inLevelSelect;
    private boolean inLevel;
    private boolean inMainMenu;
    private boolean inTransition;
    private boolean atEndOfLevels;
    private final ArrayList<Level> levels;
    private int selectedLevelIndex;
    private final UIButton levelSelectButton;
    private final UIButton aboutButton;
    private final UIButton fpsCapButton;
    private final UIButton gameSpeedButton;
    private final UIButton[] buttons;
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
        enterMainMenuTimer = new FrameTimer(49);
        levelTitleTimer = new FrameTimer(432);

        timers = new FrameTimer[] {horizontalSwipeTimer, verticalSwipeTimer, levelClearTimer, levelClearDelayTimer, enterLevelSelectTimer, enterLevelTimer, levelResetTimer, advanceTimer, enterMainMenuTimer, levelTitleTimer};

        levels = new ArrayList<>();

        levelSelectButton = new UIButton();
        levelSelectButton.setText("play");
        aboutButton = new UIButton();
        aboutButton.setText("about");
        fpsCapButton = new UIButton();
        fpsCapButton.setText("fps cap");
        fpsCapButton.setSecondaryText("144");
        gameSpeedButton = new UIButton();
        gameSpeedButton.setText("speed");
        gameSpeedButton.setSecondaryText("1.00x");
        buttons = new UIButton[] {levelSelectButton, aboutButton, fpsCapButton, gameSpeedButton};

        loadLevels();

//        startEnterLevelSelect();
        endEnterMainMenu();
//        levelScene.loadLevel(levels.get(selectedLevelIndex));
    }
    private void startEnterMainMenu() {
        enterMainMenuTimer.start();
        horizontalSwipeTimer.start();
        inTransition = true;
    }
    private void endEnterMainMenu() {
        levelScene.loadLevel(Level.fromFile("level_mainmenu.txt"));
        inLevelSelect = false;
        inMainMenu = true;
        levelScene.enterMainMenuMode();
        inTransition = false;
    }
    private void startEnterLevelSelect() {
        enterLevelSelectTimer.start();
        horizontalSwipeTimer.start();
        inTransition = true;
    }
    private void endEnterLevelSelect() {
        inLevel = false;
        inMainMenu = false;
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
        levelScene.enterLevelMode();

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
        if (inLevelSelect) {
            levelScene.updatePreviewCameraDistance();
        }
    }
    public void loadLevels() {
        levels.clear();
        File levelsDirectory = new File("assets/levels/main");
        File[] levelFiles = levelsDirectory.listFiles();
        if (levelFiles == null || levelFiles.length == 0) {
            throw new RuntimeException("No level files found!");
        }
        for (File levelFile : levelFiles) {
            levels.add(Level.fromFile("main/" + levelFile.getName()));
        }
        Collections.sort(levels);
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
        if (enterMainMenuTimer.isOnLastFrame()) {
            endEnterMainMenu();
        }

        for (UIButton button : buttons) {
            button.update(input);
        }

        if (!inTransition && !horizontalSwipeTimer.isActive() && !verticalSwipeTimer.isActive()) {
            if (inLevelSelect) {
                if (input.isPreviousLevelPressed()) {
                    changeSelectedLevelIndex(Math.max(selectedLevelIndex-1, 0));
                } else if (input.isNextLevelPressed()) {
                    changeSelectedLevelIndex(Math.min(selectedLevelIndex+1, levels.size()-1));
                } else if (input.isSelectLevelPressed()) {
                    startEnterLevel();
                } else if (input.isExitKeyPressed()) {
                    startEnterMainMenu();
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
            nvg.setFontFace("montserrat_bold");
            nvg.setFontSize(nvg.scaledWidthSize(120));
            nvg.setTextAlign(NVG_ALIGN_LEFT);
            nvg.setFillColor(color);
            nvg.drawText(nvg.left(), nvg.bottom(), String.format("level %02d", selectedLevelIndex+1));
        }
        if (inLevelSelect) {
            nvg.drawImage(nvg.escapeImage, nvg.left(), nvg.top(), 0.75f);
            nvg.drawImage(nvg.mouse1Image, nvg.left()+nvg.getWidth()*0.27f, nvg.bottom()-nvg.scaledWidthSize(nvg.mouse1Image.getHeight())*0.75f, 0.75f);
            nvg.drawImage(nvg.mousewheelImage, nvg.right()-nvg.scaledWidthSize(nvg.mousewheelImage.getWidth()-10), nvg.bottom()-nvg.scaledWidthSize(60), 0.75f);

            float scrollPosition = (float)selectedLevelIndex / Math.max(1, levels.size()-1);
            if (levels.size() == 1) {
                scrollPosition = 1;
            }
            nvg.setStrokeWidth(nvg.scaledWidthSize(6));
            nvg.setStrokeColor(Colors.backgroundDarker);
            float scrollX = nvg.right() - nvg.scaledWidthSize(42);
            float scrollY = nvg.bottom() - nvg.scaledWidthSize(100);
            nvg.drawLine(scrollX, nvg.top(), scrollX, scrollY);
            scrollY -= nvg.top();

            nvg.setFillColor(Colors.backgroundDarker);
            nvg.fillCircle(scrollX, nvg.top() + scrollPosition*scrollY, nvg.scaledWidthSize(20));

//                nvgImagePattern(nvg, left + windowWidth*0.1f, bottom, windowWidth*83f/1920, windowWidth*68f/1920, 0, resources.getEscapeImage(), 1f, imagePaint);

        } else if (inMainMenu) {
            nvg.setFontFace("montserrat_bold");
            nvg.setTextAlign(NVG_ALIGN_LEFT);
            nvg.setFillColor(Colors.backgroundDarker);

            nvg.setFontSize(nvg.scaledHeightSize(110));
            nvg.drawText(nvg.adjustedSceneX(920), nvg.scaledHeightSize(180), "bouncy balls");

            float buttonX = 970;
            float buttonY = 300;
            float gap = 30;
            float length = 300;
            float hoverPadding = 5;
            Vector2d buttonSize1 = new Vector2d(nvg.adjustedSceneX(buttonX+length) - nvg.adjustedSceneX(buttonX), nvg.scaledHeightSize(length));
            Vector2d buttonSize2 = new Vector2d(nvg.adjustedSceneX(buttonX+gap+2*length) - nvg.adjustedSceneX(buttonX+gap+length), nvg.scaledHeightSize(length));

            float x1 = nvg.adjustedSceneX(buttonX);
            float x2 = nvg.adjustedSceneX(buttonX+gap+length);
            levelSelectButton.geometry.set(new Vector2d(x1, nvg.scaledHeightSize(buttonY)), buttonSize1);
            aboutButton.geometry.set(new Vector2d(x2, nvg.scaledHeightSize(buttonY)), buttonSize1);
            fpsCapButton.geometry.set(new Vector2d(x1, nvg.scaledHeightSize(buttonY+gap+length)), buttonSize2);
            gameSpeedButton.geometry.set(new Vector2d(x2, nvg.scaledHeightSize(buttonY+gap+length)), buttonSize2);
            for (UIButton button : buttons) {
                if (button.isHoveredOver()) {
                    nvg.setFillColor(Colors.black);
                    nvg.fillRect((float)button.geometry.position.x-hoverPadding, (float)button.geometry.position.y-hoverPadding, (float)button.geometry.displacement.x+2*hoverPadding, (float)button.geometry.displacement.y+2*hoverPadding);
                    nvg.setFillColor(Colors.tile);
                    nvg.setFontFace("montserrat_bold");
                    nvg.setFontSize(nvg.scaledHeightSize(50));
                    nvg.drawText((float)button.geometry.x1() + nvg.adjustedSceneX(20), (float)button.geometry.y1() + nvg.scaledHeightSize(60), button.getText());

                    nvg.setFontFace("montserrat");
                    nvg.setFontSize(nvg.scaledHeightSize(100));
                    nvg.drawText((float)button.geometry.x1() + nvg.adjustedSceneX(20), (float)button.geometry.y2() - nvg.scaledHeightSize(30), button.getSecondaryText());
                } else {
                    nvg.setStrokeColor(Colors.black);
                    nvg.setStrokeWidth(nvg.scaledHeightSize(4));
                    nvg.drawRect((float)button.geometry.position.x, (float)button.geometry.position.y, (float)button.geometry.displacement.x, (float)button.geometry.displacement.y);
                    nvg.setFillColor(Colors.backgroundDarker);
                    nvg.setFontFace("montserrat_bold");
                    nvg.setFontSize(nvg.scaledHeightSize(50));
                    nvg.drawText((float)button.geometry.x1() + nvg.adjustedSceneX(20), (float)button.geometry.y1() + nvg.scaledHeightSize(60), button.getText());

                    nvg.setFontFace("montserrat");
                    nvg.setFontSize(nvg.scaledHeightSize(100));
                    nvg.drawText((float)button.geometry.x1() + nvg.adjustedSceneX(20), (float)button.geometry.y2() - nvg.scaledHeightSize(30), button.getSecondaryText());
                }
            }
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
            nvg.setFillColor(Colors.green);
            float x = levelClearTimer.fpercentage()*1.4f;
            float y = nvg.scaledWidthSize(-400 * x * (x - 2));
            nvg.fillRect(0, y, windowWidth, nvg.scaledWidthSize(300));

            nvg.setFontFace("montserrat_bold");
            nvg.setFontSize(nvg.scaledWidthSize(120));
            nvg.setTextAlign(NVG_ALIGN_CENTER);
            nvg.setFillColor(Colors.backgroundDarker);
            nvg.drawText(windowWidth/2, y+nvg.scaledWidthSize(190), "LEVEL CLEAR");
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
