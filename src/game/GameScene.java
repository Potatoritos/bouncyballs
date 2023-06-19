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

/**
 * Handles the game's menus
 */
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
    private final FrameTimer enterAboutTimer;
    private final FrameTimer gameExitTimer;
    private final FrameTimer levelTitleTimer;
    private final FrameTimer[] timers;
    private boolean inLevelSelect;
    private boolean inLevel;
    private boolean inMainMenu;
    private boolean inAboutMenu;
    private boolean inTransition;
    private boolean atEndOfLevels;
    private final ArrayList<Level> levels;
    private int selectedLevelIndex;
    private final UIButton levelSelectButton;
    private final UIButton aboutButton;
    private final UIButton fpsCapButton;
    private final UIButton gameSpeedButton;
    private final UIButton[] buttons;
    private float buttonX;
    private float buttonY;
    private float buttonGap;
    private float buttonLength;
    private int requestedFpsCap;
    private int[] fpsCaps;
    private int fpsCapIndex;
    private double requestedGameSpeed;
    private double[] gameSpeeds;
    private int gameSpeedIndex;
    private boolean hasRequestedExit;
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
        enterAboutTimer = new FrameTimer(49);
        gameExitTimer = new FrameTimer(72);
        levelTitleTimer = new FrameTimer(432);

        timers = new FrameTimer[] {horizontalSwipeTimer, verticalSwipeTimer, levelClearTimer, levelClearDelayTimer, enterLevelSelectTimer, enterLevelTimer, levelResetTimer, advanceTimer, enterMainMenuTimer, enterAboutTimer, gameExitTimer, levelTitleTimer};

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

        buttonX = 970;
        buttonY = 300;
        buttonGap = 30;
        buttonLength = 300;

        loadLevels();

        requestedFpsCap = 144;
        fpsCaps = new int[] {144, 30, 60};
        requestedGameSpeed = 1;
        gameSpeeds = new double[] {0.5, 0.75, 1, 1.5, 2};
        gameSpeedIndex = 2;
        verticalSwipeTimer.start(160);

        endEnterMainMenu();
    }
    public int getRequestedFpsCap() {
        return requestedFpsCap;
    }
    public double getRequestedGameSpeed() {
        return requestedGameSpeed;
    }
    public boolean hasRequestedExit() {
        return hasRequestedExit;
    }
    // The functions below are for switching between states (e.g, in main menu, in level select menu)
    private void startRequestExit() {
        gameExitTimer.start();
        horizontalSwipeTimer.start();
        inTransition = true;
    }
    private void endRequestExit() {
        hasRequestedExit = true;
        inTransition = false;
    }
    private void startEnterAboutMenu() {
        enterAboutTimer.start();
        horizontalSwipeTimer.start();
        inTransition = true;
    }
    private void endEnterAboutMenu() {
        inLevelSelect = false;
        inMainMenu = false;
        inLevel = false;
        inAboutMenu = true;
        inTransition = false;
    }
    private void startEnterMainMenu() {
        enterMainMenuTimer.start();
        horizontalSwipeTimer.start();
        inTransition = true;
    }
    private void endEnterMainMenu() {
        levelScene.loadLevel(Level.fromFile("level_mainmenu.txt"));
        inLevel = false;
        inLevelSelect = false;
        inMainMenu = true;
        inAboutMenu = false;
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
        inAboutMenu = false;
        setLevelToSelected();
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
        setLevelToSelected();
    }
    private void setLevelToSelected() {
        levelScene.loadLevel(levels.get(selectedLevelIndex));
        if (inLevelSelect) {
            levelScene.updatePreviewCameraDistance();
        }
    }

    /**
     * Load levels from the level folder
     */
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

        // Handle state switching
        // TODO: create some sort of function callback class so i can avoid doing this
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
        if (enterAboutTimer.isOnLastFrame()) {
            endEnterAboutMenu();
        }
        if (gameExitTimer.isOnLastFrame()) {
            endRequestExit();
        }

        for (UIButton button : buttons) {
            button.update(input);
        }

        // Handle menu-related input
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
                if (levelScene.hasWon()) {
                    startLevelClearDelay();
                } else if (input.isExitKeyPressed()) {
                    startEnterLevelSelect();
                } else if (levelScene.hasDied() || input.isResetKeyPressed()) {
                    startLevelReset();
                }
            } else if (inMainMenu) {
                if (levelSelectButton.isClicked()) {
                    startEnterLevelSelect();
                } else if (fpsCapButton.isClicked()) {
                    fpsCapIndex = (fpsCapIndex+1) % fpsCaps.length;
                    requestedFpsCap = fpsCaps[fpsCapIndex];
                    fpsCapButton.setSecondaryText(Integer.toString(requestedFpsCap));
                } else if (gameSpeedButton.isClicked()) {
                    gameSpeedIndex = (gameSpeedIndex+1) % gameSpeeds.length;
                    requestedGameSpeed = gameSpeeds[gameSpeedIndex];
                    gameSpeedButton.setSecondaryText(String.format("%.2fx", requestedGameSpeed));
                } else if (aboutButton.isClicked()) {
                    startEnterAboutMenu();
                } else if (input.isExitKeyPressed()) {
                    startRequestExit();
                }
            } else if (inAboutMenu) {
                if (input.isExitKeyPressed()) {
                    startEnterMainMenu();
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
            // Draw the level title
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
            // Draw key indicators
//            nvg.drawImage(nvg.escapeImage, nvg.left(), nvg.top(), 0.75f);
            nvg.drawImage(nvg.mouse1Image, nvg.left()+nvg.getWidth()*0.27f, nvg.bottom()-nvg.scaledWidthSize(nvg.mouse1Image.getHeight())*0.75f, 0.75f);
            nvg.drawImage(nvg.mousewheelImage, nvg.right()-nvg.scaledWidthSize(nvg.mousewheelImage.getWidth()-10), nvg.bottom()-nvg.scaledWidthSize(60), 0.75f);

            // Draw the scrollbar
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

        } else if (inMainMenu) {
            // Render title
            nvg.setFontFace("montserrat_bold");
            nvg.setTextAlign(NVG_ALIGN_LEFT);
            nvg.setFillColor(Colors.backgroundDarker);
            nvg.setFontSize(nvg.scaledHeightSize(110));
            nvg.drawText(nvg.adjustedSceneX(920), nvg.scaledHeightSize(180), "bouncy balls");

            // Render buttons
            Vector2d buttonSize1 = new Vector2d(nvg.adjustedSceneX(buttonX+buttonLength) - nvg.adjustedSceneX(buttonX), nvg.scaledHeightSize(buttonLength));
            Vector2d buttonSize2 = new Vector2d(nvg.adjustedSceneX(buttonX+buttonGap+2*buttonLength) - nvg.adjustedSceneX(buttonX+buttonGap+buttonLength), nvg.scaledHeightSize(buttonLength));
            float nx1 = buttonX;
            float x1 = nvg.adjustedSceneX(nx1);
            float nx2 = buttonX + buttonGap + buttonLength;
            float x2 = nvg.adjustedSceneX(nx2);
            float ny1 = buttonY;
            float y1 = nvg.scaledHeightSize(ny1);
            float ny2 = buttonY + buttonGap + buttonLength;
            float y2 = nvg.scaledHeightSize(ny2);
            levelSelectButton.geometry.set(new Vector2d(x1, y1), buttonSize1);
            aboutButton.geometry.set(new Vector2d(x2, y1), buttonSize1);
            fpsCapButton.geometry.set(new Vector2d(x1, y2), buttonSize2);
            gameSpeedButton.geometry.set(new Vector2d(x2, y2), buttonSize2);
            nvg.renderButton(levelSelectButton, nx1);
            nvg.renderButton(aboutButton, nx2);
            nvg.renderButton(fpsCapButton, nx1);
            nvg.renderButton(gameSpeedButton, nx2);

        } else if (inAboutMenu) {
            // Render about menu text
            nvg.setFillColor(Colors.tile);
            nvg.fillRect(0, 0, windowWidth, windowHeight);
            nvg.setFillColor(Colors.backgroundDarker);
            nvg.setFontFace("montserrat_bold");
            nvg.setTextAlign(NVG_ALIGN_LEFT);
            nvg.setFillColor(Colors.backgroundDarker);
            nvg.setFontSize(nvg.scaledHeightSize(80));
            nvg.drawText(nvg.adjustedSceneX(100), nvg.scaledHeightSize(150), "About");

            nvg.setFontFace("montserrat");
            nvg.setFontSize(nvg.scaledHeightSize(40));
            nvg.drawText(nvg.adjustedSceneX(100), nvg.scaledHeightSize(250), "By Elliott Cheng. My first OpenGL project (also my last high school project)!");
            nvg.setFontFace("montserrat_bold");
            nvg.drawText(nvg.adjustedSceneX(100), nvg.scaledHeightSize(350), "Instructions:");
            nvg.setFontFace("montserrat");
            nvg.drawText(nvg.adjustedSceneX(100), nvg.scaledHeightSize(400), "Move your mouse to tilt the board.");
            nvg.drawText(nvg.adjustedSceneX(100), nvg.scaledHeightSize(450), "Your objective is to maneuver all balls into their respective holes.");
            nvg.setFontFace("montserrat_bold");
            nvg.drawText(nvg.adjustedSceneX(100), nvg.scaledHeightSize(550), "Non-explicitly-stated controls:");
            nvg.drawText(nvg.adjustedSceneX(150), nvg.scaledHeightSize(600), "Esc");
            nvg.drawText(nvg.adjustedSceneX(150), nvg.scaledHeightSize(650), "R");
            nvg.setFontFace("montserrat");
            nvg.drawText(nvg.adjustedSceneX(250), nvg.scaledHeightSize(600), "- back to previous menu / exit game");
            nvg.drawText(nvg.adjustedSceneX(250), nvg.scaledHeightSize(650), "- restart level");
        }

        if (horizontalSwipeTimer.isActive()) {
            // Render the horizontal swipe animation
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
            // Render the LEVEL CLEAR animation
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
            // Render the vertical swipe animation
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
