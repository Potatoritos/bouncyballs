package game;

import org.joml.Vector3f;

public class Ball {
    private Vector3f velocity;
    private Vector3f position;
    public Ball() {
        velocity = new Vector3f();
        position = new Vector3f();
    }
    public void update() {
        position.add(velocity);

    }
}
