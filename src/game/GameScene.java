package game;

public class GameScene extends Scene {
    private final LevelScene levelScene;
    private Level level;
    public GameScene(int windowWidth, int windowHeight) {
        levelScene = new LevelScene(windowWidth, windowHeight);
        level = Level.fromFile("level0.txt");
        levelScene.loadLevel(level);
    }
    @Override
    public void handleWindowResize(int width, int height) {
        super.handleWindowResize(width, height);
        levelScene.handleWindowResize(width, height);
    }
    @Override
    public void update(InputState input) {
        levelScene.update(input);
        if (input.isKeyPressed(input.getResetKey()) || levelScene.hasDied()) {
            levelScene.reset();
        }
    }
    @Override
    public void render() {
        levelScene.render();
    }
    @Override
    public void delete() {
        levelScene.delete();
    }
}
