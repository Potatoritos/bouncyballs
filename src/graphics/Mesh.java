package graphics;

import static org.lwjgl.opengl.GL30.*;

public abstract class Mesh {
    public abstract int getVertexCount();
    public abstract int getVaoId();
    public abstract void delete();
    public void render() {
        glBindVertexArray(getVaoId());
        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
