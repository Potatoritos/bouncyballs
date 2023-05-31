package graphics;

import static org.lwjgl.opengl.GL11.*;

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
