package audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import util.Deletable;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class AudioHandler implements Deletable {
    private long context;
    private long device;
    public final AudioListener listener;
    public final AudioBuffer clackSound;
    public final AudioBuffer plingSound;
    public final AudioBuffer snapSound;
    public AudioHandler() {
        device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device");
        }
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        context = alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) {
            throw new IllegalStateException("Failed to create OpenAL context");
        }
        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        listener = new AudioListener();

        clackSound = new AudioBuffer("assets/sounds/clack.ogg");
        plingSound = new AudioBuffer("assets/sounds/pling.ogg");
        snapSound = new AudioBuffer("assets/sounds/snap.ogg");
    }
    public void delete() {
        clackSound.delete();
        plingSound.delete();
        snapSound.delete();

        if (context != NULL) alcDestroyContext(context);
        if (device != NULL) alcCloseDevice(device);
    }
}
