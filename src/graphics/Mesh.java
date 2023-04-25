package graphics;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {
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
    public void delete() {
        vao.delete();
    }
}
