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
    public final AudioBuffer splashSound;
    public final AudioBuffer snapSound;
    public final AudioBuffer explosionSound;
    public final AudioBuffer menuHover;
    public final AudioBuffer menuClick;
    public final AudioBuffer menuBack;
    public final AudioBuffer[] music;
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
        snapSound = new AudioBuffer("assets/sounds/snap.ogg");
        splashSound = new AudioBuffer("assets/sounds/snap3.ogg");
        explosionSound = new AudioBuffer("assets/sounds/snap3.ogg");

        menuHover = new AudioBuffer("assets/sounds/menuhover.ogg");
        menuClick = new AudioBuffer("assets/sounds/menuclick.ogg");
        menuBack = new AudioBuffer("assets/sounds/menuback.ogg");

        music = new AudioBuffer[] {
                new AudioBuffer("assets/sounds/chinese_toys_rap.ogg"),
                new AudioBuffer("assets/sounds/gangnam_style.ogg")
        };
    }
    public void delete() {
        clackSound.delete();
        splashSound.delete();
        snapSound.delete();
        explosionSound.delete();

        menuHover.delete();
        menuClick.delete();
        menuBack.delete();

        music[0].delete();
        music[1].delete();

        if (context != NULL) alcDestroyContext(context);
        if (device != NULL) alcCloseDevice(device);
    }
}
