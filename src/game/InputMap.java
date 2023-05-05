package game;

import org.joml.Vector2f;

public class InputMap {
    Vector2f mousePosition;
    public InputMap() {
        mousePosition = new Vector2f();
    }
    public Vector2f getMousePosition() {
        return mousePosition;
    }
}
