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

public class TextureMesh extends Mesh {
    private final int vaoId;
    private final int vertexVboId;
    private final int textureCoordsVboId;
    private final int indexVboId;
    private final int vertexCount;
    private final Texture texture;
    public TextureMesh(float[] vertices, float[] textureCoords, int[] indices, Texture texture) {
        vertexCount = indices.length;
        this.texture = texture;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vertexVboId = glGenBuffers();
        FloatBuffer vertexBuffer = memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vertexVboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        memFree(vertexBuffer);

        textureCoordsVboId = glGenBuffers();
        FloatBuffer textureCoordsBuffer = memAllocFloat(textureCoords.length);
        textureCoordsBuffer.put(textureCoords).flip();
        glBindBuffer(GL_ARRAY_BUFFER, textureCoordsVboId);
        glBufferData(GL_ARRAY_BUFFER, textureCoordsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        memFree(textureCoordsBuffer);

//        textureCoordsVboId = glGenBuffers();
//        FloatBuffer coordsBuffer = memAllocFloat(textureCoords.length);
//        coordsBuffer.put(textureCoords).flip();
//        glBindBuffer(GL_ARRAY_BUFFER, textureCoordsVboId);
//        glBufferData(GL_ARRAY_BUFFER, coordsBuffer, GL_STATIC_DRAW);
//        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        indexVboId = glGenBuffers();
        IntBuffer indexBuffer = memAllocInt(indices.length);
        indexBuffer.put(indices).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        memFree(indexBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    public void render() {
        glBindVertexArray(getVaoId());
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
    public void delete() {
        glDeleteBuffers(vertexVboId);
        glDeleteBuffers(textureCoordsVboId);
        glDeleteBuffers(indexVboId);

        glDeleteVertexArrays(vaoId);
    }
    public int getVertexCount() {
        return vertexCount;
    }
    public int getVaoId() {
        return vaoId;
    }
}
