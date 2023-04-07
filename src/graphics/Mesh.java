package graphics;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Mesh {
    private final int vaoId;
    private final int vertexVboId;
    private final int indexVboId;
    private final int colorVboId;
    private final int vertexCount;

    public Mesh(float[] vertices, int[] indices, float[] colors) {
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

        colorVboId = glGenBuffers();
        FloatBuffer colorBuffer = memAllocFloat(colors.length);
        colorBuffer.put(colors).flip();
        glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
        glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        memFree(colorBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    public void render() {
        glBindVertexArray(vaoId);
//        glEnableVertexAttribArray(0);
//        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
//        glDisableVertexAttribArray(0);
//        glDisableVertexAttribArray(1);
    }

    public void delete() {
        glDeleteBuffers(vertexVboId);
        glDeleteBuffers(indexVboId);
        glDeleteBuffers(colorVboId);

        glDeleteVertexArrays(vaoId);
    }
    public int getVertexCount() {
        return vertexCount;
    }
    public int getVaoId() {
        return vaoId;
    }
}
