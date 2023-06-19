package graphics;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Represents a mesh that has a texture mapped onto it
 */
public class TextureMesh extends Mesh {
    private final Texture texture;
    public TextureMesh(float[] vertices, float[] textureCoords, int[] indices, Texture texture) {
        super();
        this.texture = texture;

        vao.bind();

        vao.createFloatVBO(0, 3, vertices);
        vao.createFloatVBO(1, 2, textureCoords);
        vao.createEBO(indices);

        vao.unbind();
    }
    public void render() {
        vao.bind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        glDrawElements(GL_TRIANGLES,  vao.getEboSize(), GL_UNSIGNED_INT, 0);
        vao.unbind();
    }
}
