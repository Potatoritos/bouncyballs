package graphics;

import org.lwjgl.system.MemoryUtil;
import util.Deletable;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

public class VertexArrayObject implements Deletable {
    private final int id;
    private int eboSize;
    ArrayList<Integer> vbos;
    VertexArrayObject() {
        id = glGenVertexArrays();
        vbos = new ArrayList<>();
    }
    public int getEboSize() {
        return eboSize;
    }

    public void bind() {
        glBindVertexArray(id);
    }
    public void unbind() {
        glBindVertexArray(0);
    }

    public void createFloatVBO(int index, int size, float[] array) {
        int vbo = glGenBuffers();
        vbos.add(vbo);

        FloatBuffer buffer = MemoryUtil.memAllocFloat(array.length);
        buffer.put(array).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(index);
        glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0);
        MemoryUtil.memFree(buffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void createEBO(int[] array) {
        int vbo = glGenBuffers();
        eboSize = array.length;
        IntBuffer buffer = MemoryUtil.memAllocInt(array.length);
        buffer.put(array).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(buffer);
    }

    public int getId() {
        return id;
    }
    public void delete() {
        for (int vbo : vbos) {
            glDeleteBuffers(vbo);
            glDeleteVertexArrays(id);
        }
    }
}
