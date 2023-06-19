package game;

import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * A square that covers a HoleBox once a ball has fallen into it
 */
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
