package audio;

import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class AudioSource {
    private final int id;
    public AudioSource(boolean loop, boolean relative) {
        id = alGenSources();
        if (loop) alSourcei(id, AL_LOOPING, AL_TRUE);
        if (relative) alSourcei(id, AL_SOURCE_RELATIVE, AL_TRUE);
    }

    public AudioSource(AudioBuffer buffer, boolean loop, boolean relative) {
        this(loop, relative);
        setBuffer(buffer);
    }

    public void setBuffer(AudioBuffer buffer) {
        stop();
        alSourcei(id, AL_BUFFER, buffer.getId());
    }
    public void setPosition(Vector3f position) {
        alSource3f(id, AL_POSITION, position.x, position.y, position.z);
    }
    public void setVelocity(Vector3f velocity) {
        alSource3f(id, AL_VELOCITY, velocity.x, velocity.y, velocity.z);
    }
    public void setGain(float gain) {
        alSourcef(id, AL_GAIN, gain);
    }
    public void setProperty(int parameter, float value) {
        alSourcef(id, parameter, value);
    }
    public void play() {
        alSourcePlay(id);
    }
    public boolean isPlaying() {
        return alGetSourcei(id, AL_SOURCE_STATE) == AL_PLAYING;
    }
    public void pause() {
        alSourcePause(id);
    }
    public void stop() {
        alSourceStop(id);
    }
    public void delete() {
        stop();
        alDeleteSources(id);
    }
}
