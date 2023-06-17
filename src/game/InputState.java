package game;

import org.joml.Vector2d;

import java.util.HashSet;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class InputState {
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
