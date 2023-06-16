package game;

import org.joml.Vector3d;
import org.joml.Vector3f;

public class HoleBoxCover extends GameObject {
    public final Vector3d position;
    public HoleBoxCover() {
        super();
        position = new Vector3d();
    }
    public Vector3d getPosition() {
        return position;
    }
}
