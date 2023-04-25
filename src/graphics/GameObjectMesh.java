package graphics;

import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;
import static util.Geometry.triangleNormal;

public class GameObjectMesh extends Mesh {

    public GameObjectMesh(float[] vertices, float[] normals, int[] indices) {
        super();
        vao.bind();

        vao.createFloatVBO(0, 3, vertices);
        vao.createFloatVBO(1, 3, normals);
        vao.createEBO(indices);

        vao.unbind();
    }
}
