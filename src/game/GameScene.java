package game;

import graphics.NanoVGContext;
import org.joml.Vector2d;
import org.joml.Vector4f;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;

import static math.MathUtil.cubicInterpolation;
import static math.MathUtil.cutMaxMin;
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
    private boolean pendingAdvance;
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

    // Stores star levels in values
    private final HashMap<String, Integer> completedLevels;
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

        // levelClearTimer is not here intentionally
        timers = new FrameTimer[] {horizontalSwipeTimer, verticalSwipeTimer, levelClearDelayTimer, enterLevelSelectTimer, enterLevelTimer, levelResetTimer, advanceTimer, enterMainMenuTimer, enterAboutTimer, gameExitTimer, levelTitleTimer};

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

        completedLevels = new HashMap<>();

        loadLevels();
        loadCompletedLevels();

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

    /**
     * @return the currently selected level in the level select menu
     */
    public Level currentLevel() {
        return levels.get(selectedLevelIndex);
    }

    /**
     * Add a level to the completed levels file
     * @param name the name of the level
     */
    private void addCompletedLevel(String name, int starLevel) {
        if (completedLevels.containsKey(name)) {
            if (starLevel < completedLevels.get(name)) {
                completedLevels.put(name, starLevel);
            } else {
                return;
            }
            // Recreate completed levels file if name exists but star level is better
            HashMap<String, Integer> completed = new HashMap<>(completedLevels);
            completedLevels.clear();
            try (FileWriter fw = new FileWriter("assets/levels/completed_levels.txt", false);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                out.print("");
            } catch (IOException e) {
                throw new RuntimeException("Error while adding to completion list");
            }
            for (Map.Entry<String, Integer> entry : completed.entrySet()) {
                addCompletedLevel(entry.getKey(), entry.getValue());
            }
            return;
        }

        completedLevels.put(name, starLevel);
        try (FileWriter fw = new FileWriter("assets/levels/completed_levels.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)) {
            out.println(name + "," + starLevel);
        } catch (IOException e) {
            throw new RuntimeException("Error while adding to completion list");
        }
    }

    /**
     * Load the list of completed levels from the completed levels file
     */
    private void loadCompletedLevels() {
        try {
            try (FileReader fr = new FileReader("assets/levels/completed_levels.txt"); BufferedReader br = new BufferedReader(fr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    int delim = line.indexOf(',');
                    String name = line.substring(0, delim);
                    int starLevel = Integer.parseInt(line.substring(delim+1));
                    completedLevels.put(name, starLevel);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading from completion list");
        }
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
        Colors.resetBackground();
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
        updateLevelBackground();
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
        addCompletedLevel(currentLevel().getName(), levelScene.getStarLevel());
        levelClearTimer.start();
    }
    private void midLevelClearPending() {
        pendingAdvance = true;
    }
    private void midLevelClear() {
        pendingAdvance = false;
        advanceTimer.start();
        verticalSwipeTimer.start();
    }
    private void endLevelClear() {
        pendingAdvance = false;
        levelClearDelayTimer.end();
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
    private void updateLevelBackground() {
        Colors.resetBackground();
        if (completedLevels.containsKey(currentLevel().getName())) {
            switch(completedLevels.get(currentLevel().getName())) {
                case 0 -> Colors.background.set(Colors.red);
                case 1 -> Colors.background.set(Colors.pink);
                case 2 -> Colors.background.set(Colors.blue);
            }
        }
    }
    private void setLevelToSelected() {
        updateLevelBackground();
        levelScene.loadLevel(currentLevel());
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
            timer.advanceFrame();
        }
        if (!pendingAdvance) {
            levelClearTimer.advanceFrame();
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
            midLevelClearPending();
        }
        if (pendingAdvance) {
            if (input.isSelectLevelPressed()) {
                midLevelClear();
            } else if (input.isResetKeyPressed()) {
                endLevelClear();
                startLevelReset();
            } else if (input.isExitKeyPressed())  {
                endLevelClear();
                startEnterLevelSelect();
            }
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

            String name = currentLevel().getName();
            nvg.drawText(nvg.left(), nvg.bottom(), name);
            if (inLevelSelect && completedLevels.containsKey(name)) {
                nvg.drawImage(nvg.starImage, nvg.left(), nvg.bottom() - nvg.scaledWidthSize(160), 0.0625f);

                int starLevel = completedLevels.get(name);
                if (starLevel <= 1) nvg.drawImage(nvg.starImage, nvg.left() + nvg.scaledWidthSize(64), nvg.bottom() - nvg.scaledWidthSize(160), 0.0625f);
                if (starLevel <= 0) nvg.drawImage(nvg.starImage, nvg.left() + nvg.scaledWidthSize(128), nvg.bottom() - nvg.scaledWidthSize(160), 0.0625f);
            }
        }
        if (inLevel) {
            float width = nvg.scaledWidthSize(16);
            float x = nvg.getWidth() - width - nvg.scaledWidthSize(96);
            float starX = nvg.getWidth() - nvg.scaledWidthSize(88);
            float starScale = 0.03125f;
            float level2Height = 120;
            float verticalMargin = 60;
            int starLevel = levelScene.getStarLevel();

            Vector4f[] colors = new Vector4f[] {
                    Colors.red,
                    Colors.pink,
                    Colors.blue
            };

            UIRectangle[] rectangles = new UIRectangle[3];

            nvg.setFillColor(Colors.black);
            float border = nvg.scaledWidthSize(4);
            nvg.fillRect(x-border, verticalMargin-border, nvg.scaledWidthSize(16) + 2*border, nvg.getHeight() - 2*verticalMargin + 2*border);

            rectangles[2] = new UIRectangle(x, verticalMargin, width, nvg.scaledHeightSize(level2Height));
            nvg.drawImage(nvg.starImage, starX, verticalMargin + nvg.scaledWidthSize(88), starScale);

            float midpoint = (float)currentLevel().getStarTimeLimit(1) / (currentLevel().getStarTimeLimit(0) + currentLevel().getStarTimeLimit(1));
            midpoint *= nvg.getHeight() - nvg.scaledHeightSize(level2Height) - 2*verticalMargin;

            rectangles[1] = new UIRectangle(x, nvg.scaledHeightSize(level2Height) + verticalMargin, width, midpoint);
            float y = nvg.scaledHeightSize(level2Height) + midpoint;
            nvg.drawImage(nvg.starImage, starX, y-nvg.scaledHeightSize(32) + verticalMargin, starScale);
            nvg.drawImage(nvg.starImage, starX, y-nvg.scaledHeightSize(64) + verticalMargin, starScale);

            rectangles[0] = new UIRectangle(x, nvg.scaledHeightSize(level2Height)+midpoint + verticalMargin, width, nvg.getHeight()-y - 2*verticalMargin);
            nvg.drawImage(nvg.starImage, starX, nvg.getHeight()-nvg.scaledHeightSize(32) - verticalMargin, starScale);
            nvg.drawImage(nvg.starImage, starX, nvg.getHeight()-nvg.scaledHeightSize(64) - verticalMargin, starScale);
            nvg.drawImage(nvg.starImage, starX, nvg.getHeight()-nvg.scaledHeightSize(96) - verticalMargin, starScale);

            float percentage = (float)levelScene.stopwatch.getFrame() / (currentLevel().getStarTimeLimit(0) + currentLevel().getStarTimeLimit(1));
            float arrowY = nvg.getHeight() - percentage * (nvg.getHeight() - nvg.scaledHeightSize(level2Height) - 2*verticalMargin) - nvg.scaledWidthSize(16) - verticalMargin;
            nvg.drawImage(nvg.arrowImage, nvg.getWidth()-nvg.scaledWidthSize(144), Math.max(arrowY, verticalMargin), 0.25f);

            nvg.setFillColor(Colors.red);
            nvg.fillRect(rectangles[0]);
            nvg.setFillColor(Colors.pink);
            nvg.fillRect(rectangles[1]);
            nvg.setFillColor(Colors.blue);
            nvg.fillRect(rectangles[2]);

            nvg.setStrokeWidth(nvg.scaledWidthSize(4));
            nvg.drawLine(x, nvg.scaledHeightSize(level2Height)+verticalMargin, x+width, nvg.scaledHeightSize(level2Height)+verticalMargin);
            nvg.drawLine(x, y+verticalMargin, x+width, y+verticalMargin);

            rectangles[starLevel].setPadding(nvg.scaledWidthSize(2));
            nvg.fillRectOutline(rectangles[starLevel], nvg.scaledWidthSize(4), Colors.black, colors[starLevel]);

            if (levelScene.hasWon()) {
                Vector4f color = new Vector4f(colors[starLevel]);
                float expansionPercentage = Math.min(2 * levelClearTimer.fpercentage(), 1);
                color.w = 1 - expansionPercentage;
                float expansion = nvg.scaledWidthSize(600) * cubicInterpolation(expansionPercentage);
                rectangles[starLevel].setX(rectangles[starLevel].getX() - expansion);
                rectangles[starLevel].setWidth(rectangles[starLevel].getWidth() + expansion);
                nvg.setFillColor(color);
                nvg.fillRect(rectangles[starLevel]);
            }

//            nvg.drawLine(x+nvg.scaledWidthSize(16), verticalMargin, x+nvg.scaledWidthSize(16), nvg.getHeight() - verticalMargin);
        }

        if (levelClearTimer.isActive()) {
            // Render the LEVEL CLEAR animation
            float x = levelClearTimer.fpercentage()*1.4f;
            float y = nvg.scaledWidthSize(-200 * x * (x - 2));
//            nvg.fillRect(0, y, windowWidth, nvg.scaledWidthSize(300));

            Vector4f bg = new Vector4f(Colors.tile);
            bg.w = cubicInterpolation(0.8f * levelClearTimer.fpercentage());
            nvg.setFillColor(bg);
            nvg.fillRect(0, 0, nvg.getWidth(), nvg.getHeight());

            nvg.setFontFace("montserrat_bold");
            nvg.setFontSize(nvg.scaledWidthSize(120));
            nvg.setTextAlign(NVG_ALIGN_CENTER);

            Vector4f fg = new Vector4f(Colors.backgroundDarker);
            fg.w = 1.5f * levelClearTimer.fpercentage();
            fg.w *= fg.w;
            nvg.setFillColor(fg);
            nvg.drawText(windowWidth/2, y+nvg.scaledWidthSize(300), "LEVEL CLEAR");

            if (levelClearTimer.getFrame() >= 120) {
                switch(levelScene.getStarLevel()) {
                    case 0 -> {
                        nvg.drawImage(nvg.starImage, nvg.scaledWidthSize(1920 / 2 - 192), nvg.scaledWidthSize(530), 0.125f);
                        nvg.drawImage(nvg.starImage, nvg.scaledWidthSize(1920 / 2 - 64), nvg.scaledWidthSize(530), 0.125f);
                        nvg.drawImage(nvg.starImage, nvg.scaledWidthSize(1920 / 2 + 64), nvg.scaledWidthSize(530), 0.125f);
                    }
                    case 1 -> {
                        nvg.drawImage(nvg.starImage, nvg.scaledWidthSize(1920 / 2 - 128), nvg.scaledWidthSize(530), 0.125f);
                        nvg.drawImage(nvg.starImage, nvg.scaledWidthSize(1920 / 2), nvg.scaledWidthSize(530), 0.125f);
                    }
                    case 2 -> {
                        nvg.drawImage(nvg.starImage, nvg.scaledWidthSize(1920/2 - 64), nvg.scaledWidthSize(530), 0.125f);
                    }
                }

                nvg.setFillColor(fg);
                nvg.setFontSize(nvg.scaledWidthSize(80));
                nvg.setFontFace("montserrat");
                nvg.drawText(windowWidth/2, y+nvg.scaledWidthSize(550), levelScene.stopwatch.timeElapsedString(requestedFpsCap));

                nvg.setFontSize(nvg.scaledWidthSize(40));
                nvg.setTextAlign(NVG_ALIGN_LEFT);
                nvg.setFontFace("montserrat_bold");
                nvg.drawText(nvg.left() + nvg.scaledWidthSize(80), nvg.bottom() - nvg.scaledWidthSize(10), "next level");

                nvg.drawImage(nvg.mouse1Image, nvg.left(), nvg.bottom()-nvg.scaledWidthSize(nvg.mouse1Image.getHeight())*0.75f, 0.75f);
            }

        }
        if (inLevelSelect) {
            // Draw key indicators
//            nvg.drawImage(nvg.escapeImage, nvg.left(), nvg.top(), 0.75f);
            nvg.drawImage(nvg.mouse1Image, nvg.left()+nvg.getWidth()*0.29f, nvg.bottom()-nvg.scaledWidthSize(nvg.mouse1Image.getHeight())*0.75f, 0.75f);
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
            nvg.drawText(nvg.adjustedSceneX(100), nvg.scaledHeightSize(450), "Your objective is to manoeuvre all balls into their respective holes.");
            nvg.setFontFace("montserrat_bold");
            nvg.drawText(nvg.adjustedSceneX(100), nvg.scaledHeightSize(550), "Non-implied controls:");
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
