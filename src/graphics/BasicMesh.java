package graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class BasicMesh extends Mesh {

    public BasicMesh(float[] vertices, int[] indices) {
        super();

        vao.bind();

        vao.createFloatVBO(0, 3, vertices);
        vao.createEBO(indices);

        vao.unbind();
    }
}
