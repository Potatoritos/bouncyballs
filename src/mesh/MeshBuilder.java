package mesh;

import graphics.GameObjectMesh;
import org.joml.Vector3f;
import shape.Line3d;
import shape.Line3f;

import java.util.ArrayList;

import static math.MathUtil.insertVector;

/**
 * Utility class for creating meshes
 */
public class MeshBuilder {
    private final ArrayList<Vector3f> vertices;
    private final ArrayList<Vector3f> normals;
    private final ArrayList<Vector3f> colors;
    private final ArrayList<Integer> indices;
    public MeshBuilder() {
        vertices = new ArrayList<>();
        normals = new ArrayList<>();
        colors = new ArrayList<>();
        indices = new ArrayList<>();
    }

    /**
     * Adds a quadrilateral to the mesh
     * @param vertices the quadrilateral's vertices
     * @param normals the normals of the vertices
     * @param colors the colors of the vertices
     */
    public void addQuad(Quad vertices, Quad normals, Quad colors) {
        int i = this.vertices.size();
        indices.add(i);
        indices.add(i+1);
        indices.add(i+2);
        indices.add(i+2);
        indices.add(i+3);
        indices.add(i);
        vertices.addToList(this.vertices);
        normals.addToList(this.normals);
        colors.addToList(this.colors);
    }

    /**
     * Adds a triangle to the mesh
     * @param vertices the triangle's vertices
     * @param normals the normals of the vertices
     * @param colors the colors of the vertices
     */
    public void addTriangle(Triangle vertices, Triangle normals, Triangle colors) {
        int i = this.vertices.size();
        indices.add(i);
        indices.add(i+1);
        indices.add(i+2);
        vertices.addToList(this.vertices);
        normals.addToList(this.normals);
        colors.addToList(this.colors);
    }

    public void addAxisAlignedBox(Line3f box, Vector3f color) {
        Quad colors = new Quad(color);
        addQuad(
                new Quad(
                        new Vector3f(box.x2(), box.y1(), box.z1()),
                        new Vector3f(box.x1(), box.y1(), box.z1()),
                        new Vector3f(box.x1(), box.y2(), box.z1()),
                        new Vector3f(box.x2(), box.y2(), box.z1())
                ),
                new Quad(new Vector3f(0, 0, -1)),
                colors
        );
        addQuad(
                new Quad(
                        new Vector3f(box.x1(), box.y1(), box.z2()),
                        new Vector3f(box.x2(), box.y1(), box.z2()),
                        new Vector3f(box.x2(), box.y2(), box.z2()),
                        new Vector3f(box.x1(), box.y2(), box.z2())
                ),
                new Quad(new Vector3f(0, 0, 1)),
                colors
        );
        addQuad(
                new Quad(
                        new Vector3f(box.x1(), box.y1(), box.z1()),
                        new Vector3f(box.x1(), box.y1(), box.z2()),
                        new Vector3f(box.x1(), box.y2(), box.z2()),
                        new Vector3f(box.x1(), box.y2(), box.z1())
                ),
                new Quad(new Vector3f(-1, 0, 0)),
                colors
        );
        addQuad(
                new Quad(
                        new Vector3f(box.x2(), box.y1(), box.z2()),
                        new Vector3f(box.x2(), box.y1(), box.z1()),
                        new Vector3f(box.x2(), box.y2(), box.z1()),
                        new Vector3f(box.x2(), box.y2(), box.z2())
                ),
                new Quad(new Vector3f(1, 0, 0)),
                colors
        );
        addQuad(
                new Quad(
                        new Vector3f(box.x1(), box.y1(), box.z1()),
                        new Vector3f(box.x2(), box.y1(), box.z1()),
                        new Vector3f(box.x2(), box.y1(), box.z2()),
                        new Vector3f(box.x1(), box.y1(), box.z2())
                ),
                new Quad(new Vector3f(0, -1, 0)),
                colors
        );
        addQuad(
                new Quad(
                        new Vector3f(box.x2(), box.y2(), box.z1()),
                        new Vector3f(box.x1(), box.y2(), box.z1()),
                        new Vector3f(box.x1(), box.y2(), box.z2()),
                        new Vector3f(box.x2(), box.y2(), box.z2())
                ),
                new Quad(new Vector3f(0, 1, 0)),
                colors
        );
    }

    /**
     * Creates a mesh from the added triangles and quadrilaterals
     * @return the mesh
     */
    GameObjectMesh createMesh() {
        int size = this.vertices.size();
        float[] vertices = new float[3*size];
        float[] normals = new float[3*size];
        float[] colors = new float[3*size];
        int[] indices = new int[this.indices.size()];
        for (int i = 0; i < this.vertices.size(); i++) {
            insertVector(vertices, i, this.vertices.get(i));
        }
        for (int i = 0; i < this.normals.size(); i++) {
            insertVector(normals, i, this.normals.get(i));
        }
        for (int i = 0; i < this.colors.size(); i++) {
            insertVector(colors, i, this.colors.get(i));
        }
        for (int i = 0; i < this.indices.size(); i++) {
            indices[i] = this.indices.get(i);
        }
        return new GameObjectMesh(vertices, normals, colors, indices);
    }
}
