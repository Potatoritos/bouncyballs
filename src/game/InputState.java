package game;

import org.joml.Vector2d;

import java.util.HashSet;

public class InputState {
    public final Vector2d mousePosition;
    public final HashSet<Integer> pressedKeys;
    public InputState() {
        mousePosition = new Vector2d();
        pressedKeys = new HashSet<>();
    }
}
