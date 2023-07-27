package audio;

import graphics.PerspectiveCamera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class AudioListener {
    public AudioListener() {
        this(new Vector3f(0, 0, 0));
    }
    public AudioListener(Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
        alListener3f(AL_VELOCITY, 0, 0, 0);
    }
    public void setSpeed(Vector3f speed) {
        alListener3f(AL_VELOCITY, speed.x, speed.y, speed.z);
    }
    public void setPosition(Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
    }
    public void setOrientation(Vector3f at, Vector3f up) {
        float[] data = new float[6];
        data[0] = at.x;
        data[1] = at.y;
        data[2] = at.z;
        data[3] = up.x;
        data[4] = up.y;
        data[5] = up.z;
        alListenerfv(AL_ORIENTATION, data);
    }
    public void updatePosition(PerspectiveCamera camera) {
        Matrix4f viewMatrix = camera.getViewMatrix();
        setPosition(camera.position);
        Vector3f at = new Vector3f();
        viewMatrix.positiveZ(at).negate();
        Vector3f up = new Vector3f();
        viewMatrix.positiveY(up);
        setOrientation(at, up);
    }
    public void setGain(float gain) {
        alListenerf(AL_GAIN, gain);
    }
}
