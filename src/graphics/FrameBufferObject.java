package graphics;

import org.lwjgl.opengl.GL30;
import util.Deletable;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30C.*;

public class FrameBufferObject implements Deletable {
    private final int id;
    public FrameBufferObject() {
        id = glGenFramebuffers();
    }
    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id);
    }
    public static void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    public void attachColorTexture(Texture texture) {
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.getId(), 0);
    }
    public void attachDepthTexture(Texture texture) {
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture.getId(), 0);
    }
    public boolean isComplete() {
        return glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
    }
    public void delete() {
        glDeleteFramebuffers(id);
    }
}
