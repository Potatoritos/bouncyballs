package graphics;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;

/**
 * An FBO that holds only a depth texture.
 * This is meant to be written to for use in shaders
 */
public class DepthMapFbo extends FrameBufferObject {
    private final Texture depthTexture;
    public Texture getDepthTexture() {
        return depthTexture;
    }
    public DepthMapFbo(int width, int height) {
        depthTexture = new Texture();

        depthTexture.bind();
        depthTexture.setEmptyImage(width, height, GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT, GL_FLOAT);
        Texture.unbind();

        bind();
        attachDepthTexture(depthTexture);

        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        unbind();
    }
}
