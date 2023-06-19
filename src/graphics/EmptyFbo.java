package graphics;

import static org.lwjgl.opengl.GL11.*;

/**
 * An FBO that is empty.
 * This is meant to be written to for use in shaders
 */
public class EmptyFbo extends FrameBufferObject {
    private final Texture colorTexture;
    private final Texture depthTexture;
    public EmptyFbo(int width, int height) {
        super();
        bind();
        colorTexture = new Texture();
        depthTexture = new Texture();
        resize(width, height);
    }

    public Texture getColorTexture() {
        return colorTexture;
    }
    public Texture getDepthTexture() {
        return depthTexture;
    }

    /**
     * Changes the dimensions of the texture
     * @param width the new width
     * @param height the new height
     */
    public void resize(int width, int height) {
        colorTexture.bind();
        colorTexture.setEmptyImage(width, height, GL_RGBA, GL_RGBA, GL_FLOAT);
        depthTexture.bind();
        depthTexture.setEmptyImage(width, height, GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE);
        Texture.unbind();

        attachColorTexture(colorTexture);
        attachDepthTexture(depthTexture);
    }

}
