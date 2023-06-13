package mesh;

import graphics.GameObjectMesh;
import org.joml.Vector3f;

import java.util.ArrayList;

import static math.MathUtil.insertVector;

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
    public void addTriangle(Triangle vertices, Triangle normals, Triangle colors) {
        int i = this.vertices.size();
        indices.add(i);
        indices.add(i+1);
        indices.add(i+2);
        vertices.addToList(this.vertices);
        normals.addToList(this.normals);
        colors.addToList(this.colors);
    }
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
