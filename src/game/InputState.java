package game;

import org.joml.Vector2d;

import java.util.HashSet;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class InputState {
    public static final int SCROLLWHEEL_UP = 727272727;
    public static final int SCROLLWHEEL_DOWN = 727272728;
    public static final int MOUSE_BUTTON_LEFT = 727272729;
    public final Vector2d mousePosition;
    private final HashSet<Integer> pressedKeys;
    private int resetKey = GLFW_KEY_R;
    public int getResetKey() {
        return resetKey;
    }
    public void clearPressedKeys() {
        pressedKeys.clear();
    }
    public void addPressedKey(int key) {
        pressedKeys.add(key);
    }
    public boolean isKeyPressed(int key) {
        return pressedKeys.contains(key);
    }
    public InputState() {
        mousePosition = new Vector2d();
        pressedKeys = new HashSet<>();
    }
}
