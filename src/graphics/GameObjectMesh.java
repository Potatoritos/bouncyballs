package graphics;

/**
 * Represents the mesh of a game object
 */
public class GameObjectMesh extends Mesh {

    public GameObjectMesh(float[] vertices, float[] normals, float[] colors, int[] indices) {
        super();
        vao.bind();

        vao.createFloatVBO(0, 3, vertices);
        vao.createFloatVBO(1, 3, normals);
        vao.createFloatVBO(2, 3, colors);
        vao.createEBO(indices);

        vao.unbind();
    }
}
