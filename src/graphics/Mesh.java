package graphics;

import util.Deletable;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

/**
 * A set of vertices with user-defined properties
 */
public class Mesh implements Deletable {
    protected final VertexArrayObject vao;
    public Mesh() {
        vao = new VertexArrayObject();
    }
    public void render() {
        vao.bind();
        glBindVertexArray(vao.getId());
        glDrawElements(GL_TRIANGLES, vao.getEboSize(), GL_UNSIGNED_INT, 0);
        vao.unbind();
    }

    /**
     * Render this mesh using instancing
     */
    public void renderInstanced(int count) {
        vao.bind();
        glBindVertexArray(vao.getId());
        glDrawElementsInstanced(GL_TRIANGLES, vao.getEboSize(), GL_UNSIGNED_INT, 0, count);
        vao.unbind();
    }
    public void delete() {
        vao.delete();
    }
}





























































