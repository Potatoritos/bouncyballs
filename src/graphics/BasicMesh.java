package graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class BasicMesh extends Mesh {
    private final int vaoId;
    private final int vertexVboId;
    private final int indexVboId;
    private final int vertexCount;

    public BasicMesh(float[] vertices, int[] indices) {
        vertexCount = indices.length;

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

        indexVboId = glGenBuffers();
        IntBuffer indexBuffer = memAllocInt(indices.length);
        indexBuffer.put(indices).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        memFree(indexBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    public void delete() {
        glDeleteBuffers(vertexVboId);
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
