package game;

import org.joml.Vector2d;

import java.util.HashSet;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

/**
 * Stores the current state of user input
 */
public class InputState {
    public static final int SCROLLWHEEL_UP = 727272727;
    public static final int SCROLLWHEEL_DOWN = 727272728;
    public static final int MOUSE_BUTTON_LEFT = 727272729;

    /**
     * The mouse position, normalized to [-1, 1]
     */
    public final Vector2d mousePosition;

    /**
     * The actual window coordinates of the mouse
     */
    public final Vector2d actualMousePosition;
    private final HashSet<Integer> pressedKeys;

    /**
     * Marks all keys as not pressed. Should be called at the start of every frame
     */
    public void clearPressedKeys() {
        pressedKeys.clear();
    }

    /**
     * Marks a key as pressed for the current frame
     * @param key the key
     */
    public void addPressedKey(int key) {
        pressedKeys.add(key);
    }
    public boolean isKeyPressed(int key) {
        return pressedKeys.contains(key);
    }
    public InputState() {
        mousePosition = new Vector2d();
        actualMousePosition = new Vector2d();
        pressedKeys = new HashSet<>();
    }
    public boolean isNextLevelPressed() {
        return isKeyPressed(SCROLLWHEEL_DOWN);
    }
    public boolean isPreviousLevelPressed() {
        return isKeyPressed(SCROLLWHEEL_UP);
    }
    public boolean isSelectLevelPressed() {
        return isKeyPressed(MOUSE_BUTTON_LEFT);
    }
    public boolean isExitKeyPressed() {
        return isKeyPressed(GLFW_KEY_ESCAPE);
    }
    public boolean isResetKeyPressed() {
        return isKeyPressed(GLFW_KEY_R);
    }
}
