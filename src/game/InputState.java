package game;

import org.joml.Vector2f;

public class InputState {
    Vector2f mousePosition;
    public InputState() {
        mousePosition = new Vector2f();
    }
    public Vector2f getMousePosition() {
        return mousePosition;
    }
}
