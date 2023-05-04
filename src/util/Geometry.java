package util;

import graphics.GameObjectMesh;
import graphics.Texture;
import graphics.TextureMesh;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;


public class Geometry {
    private final static Vector3f u;
    private final static Vector3f v;
    static {
        u = new Vector3f();
        v = new Vector3f();
    }
    public static Vector3f triangleNormal(Vector3f a, Vector3f b, Vector3f c) {
        b.sub(a, u);
        c.sub(a, v);
        return u.cross(v).normalize();
    }
    private static void addTriangle(ArrayList<Vector3f> list, Vector3f a, Vector3f b, Vector3f c) {
        list.add(a);
        list.add(b);
        list.add(c);
    }
    public static ArrayList<Vector3f> generateIcosahedronFaces() {
        ArrayList<Vector3f> faces = new ArrayList<>(60);

        float x = 1/(float)Math.sqrt(5);
        float y1 = (1-x)/2;
        float y2 = (1+x)/2;
        float z1 = (float)Math.sqrt(y1);
        float z2 = (float)Math.sqrt(y2);

        Vector3f top = new Vector3f(1, 0, 0);
        Vector3f bottom = new Vector3f(-1, 0, 0);

        Vector3f[] topHalf = new Vector3f[] {
                new Vector3f(x, 2*x, 0),
                new Vector3f(x, y1, z2),
                new Vector3f(x, -y2, z1),
                new Vector3f(x, -y2, -z1),
                new Vector3f(x, y1, -z2)
        };

        Vector3f[] bottomHalf = new Vector3f[5];
        for (int i = 0; i < 5; i++) {
            bottomHalf[i] = new Vector3f();
            topHalf[(i+2)%5].negate(bottomHalf[i]);
        }

        for (int i = 0; i < 5; i++) {
            addTriangle(faces, top, topHalf[i], topHalf[(i+1)%5]);
            addTriangle(faces, topHalf[i], bottomHalf[i], bottomHalf[(i+1)%5]);
            addTriangle(faces, topHalf[(i+1)%5], topHalf[i], bottomHalf[(i+1)%5]);
            addTriangle(faces, bottomHalf[(i+1)%5], bottomHalf[i], bottom);
        }
        return faces;
    }

    public static Vector3f sphericalMidpoint(Vector3f a, Vector3f b) {
        Vector3f midpoint = new Vector3f(a);
        midpoint.add(b);
        midpoint.normalize(1);
        return midpoint;
    }

    public static ArrayList<Vector3f> generateGeodesicPolyhedronFaces(int iterations) {
        ArrayList<Vector3f> faces = generateIcosahedronFaces();
        ArrayList<Vector3f> newFaces = new ArrayList<>();

//        int numberEdges = 30;
//        int numberVertices = 12;

        for (int it = 0; it < iterations; it++) {
            newFaces.ensureCapacity(4 * faces.size());
            for (int i = 0; i < faces.size(); i += 3) {
                Vector3f v1 = faces.get(i), v2 = faces.get(i + 1), v3 = faces.get(i + 2);
                Vector3f m12 = sphericalMidpoint(v1, v2);
                Vector3f m23 = sphericalMidpoint(v2, v3);
                Vector3f m13 = sphericalMidpoint(v1, v3);

                addTriangle(newFaces, v1, m12, m13);
                addTriangle(newFaces, v2, m23, m12);
                addTriangle(newFaces, v3, m13, m23);
                addTriangle(newFaces, m12, m23, m13);
            }
            ArrayList<Vector3f> temp = faces;
            faces = newFaces;
            newFaces = temp;

            newFaces.clear();

//            numberVertices += numberEdges;
//            numberEdges = numberVertices + faces.size()/3 - 2;
        }
        return faces;
    }
    private static void insertVector(float[] arr, int index, Vector3f vector) {
        arr[3*index] = vector.x;
        arr[3*index + 1] = vector.y;
        arr[3*index + 2] = vector.z;
    }

    public static GameObjectMesh generateGeodesicPolyhedronMesh(int iterations, Vector3f color) {
        ArrayList<Vector3f> faces = generateGeodesicPolyhedronFaces(iterations);
        float[] vertices = new float[3*faces.size()];
        float[] normals = new float[3*faces.size()];
        float[] colors = new float[3*faces.size()];
        int[] indices = new int[faces.size()];

        for (int i = 0; i < faces.size(); i += 3) {
            Vector3f v1 = faces.get(i), v2 = faces.get(i+1), v3 = faces.get(i+2);
            insertVector(vertices, i, v1);
            insertVector(vertices, i+1, v2);
            insertVector(vertices, i+2, v3);

            Vector3f normal = triangleNormal(v1, v2, v3);
//            Vector3f normal = new Vector3f(0, 0, 0);
            insertVector(normals, i, normal);
            insertVector(normals, i+1, normal);
            insertVector(normals, i+2, normal);

            indices[i] = i;
            indices[i+1] = i+1;
            indices[i+2] = i+2;
        }

//        computeFaceNormals(normals, vertices, indices);

        for (int i = 0; i < faces.size(); i++) {
            insertVector(colors, i, color);
        }

        return new GameObjectMesh(vertices, normals, colors, indices);
    }

//    public static Mesh generateIcosphereBasicMesh() {
//        ArrayList<Vector3f> faces = generateIcosphereFaces();
//
//        HashMap<Vector3f, Integer> ids = new HashMap<>();
//        float[] vertices = new float[3*numberVertices];
//        float[] normals = new float[3*numberVertices];
//        int[] indices = new int[faces.size()];
//
//        int idCounter = 0;
//
//        for (int i = 0; i < faces.size(); i ++) {
//            int id;
//            Vector3f vertex = faces.get(i);
//            if (ids.containsKey(vertex)) {
//                id = ids.get(vertex);
//            } else {
//                id = idCounter;
//                ids.put(vertex, id);
//                vertices[3*idCounter] = vertex.x;
//                vertices[3*idCounter + 1] = vertex.y;
//                vertices[3*idCounter + 2] = vertex.z;
//                idCounter++;
//            }
//            indices[i] = id;
//        }
//
//        return new BasicMesh(vertices, indices);
//    }
    public static GameObjectMesh rectangularPrismMesh(Vector3f position, Vector3f dimensions, Vector3f color) {
        Vector3f p = position, d = dimensions;
        Vector3f center = new Vector3f(p.x+d.x/2, p.y+d.y/2, p.z+d.z/2);
//        center.set(position);
//        p.half(d, center);
        float[] vertices = new float[] {
                p.x,        p.y,        p.z,
                p.x+d.x,    p.y,        p.z,
                p.x,        p.y+d.y,    p.z,
                p.x+d.x,    p.y+d.y,    p.z,

                p.x+d.x,    p.y,        p.z+d.z,
                p.x,        p.y,        p.z+d.z,
                p.x+d.x,    p.y+d.y,    p.z+d.z,
                p.x,        p.y+d.y,    p.z+d.z,

                p.x,        p.y,        p.z+d.z,
                p.x,        p.y,        p.z,
                p.x,        p.y+d.y,    p.z+d.z,
                p.x,        p.y+d.y,    p.z,

                p.x+d.x,    p.y,        p.z,
                p.x+d.x,    p.y,        p.z+d.z,
                p.x+d.x,    p.y+d.y,    p.z,
                p.x+d.x,    p.y+d.y,    p.z+d.z,

                p.x,        p.y,        p.z+d.z,
                p.x+d.x,    p.y,        p.z+d.z,
                p.x,        p.y,        p.z,
                p.x+d.x,    p.y,        p.z,

                p.x,        p.y+d.y,    p.z,
                p.x+d.x,    p.y+d.y,    p.z,
                p.x,        p.y+d.y,    p.z+d.z,
                p.x+d.x,    p.y+d.y,    p.z+d.z
        };
        int[] indices = new int[] {
                0, 1, 2, 1, 3, 2,
                4, 5, 6, 5, 7, 6,
                8, 9, 10, 9, 11, 10,
                12, 13, 14, 13, 15, 14,
                16, 17, 18, 17, 19, 18,
                20, 21, 22, 21, 23, 22
        };
        float[] normals = new float[vertices.length];
        for (int i = 0; i < vertices.length/3; i++) {
            u.set(vertices[3*i], vertices[3*i+1], vertices[3*i+2]).sub(center).normalize();
            insertVector(normals, i, u);
        }

//        float[] normals = new float[] {
//                0, 0, -1,   0, 0, -1,   0, 0, -1,   0, 0, -1,
//                0, 0, 1,    0, 0, 1,    0, 0, 1,    0, 0, 1,
//                -1, 0, 0,   -1, 0, 0,   -1, 0, 0,   -1, 0, 0,
//                1, 0, 0,    1, 0, 0,    1, 0, 0,    1, 0, 0,
//                0, -1, 0,   0, -1, 0,   0, -1, 0,   0, -1, 0,
//                0, 1, 0,    0, 1, 0,    0, 1, 0,    0, 1, 0
//        };
//        float[] normals = new float[] {
//                0, 0, 0,   0, 0, 0,   0, 0, 0,   0, 0, 0,
//                0, 0, 0,    0, 0, 0,    0, 0, 0,    0, 0, 0,
//                0, 0, 0,   0, 0, 0,   0, 0, 0,   0, 0, 0,
//                0, 0, 0,    0, 0, 0,    0, 0, 0,    0, 0, 0,
//                0, 0, 0,   0, 0, 0,   0, 0, 0,   0, 0, 0,
//                0, 0, 0,    0, 0, 0,    0, 0, 0,    0, 0, 0
//        };
//        float[] normals = new float[3*indices.length];
//        computeFaceNormals(normals, vertices, indices);
        float[] colors = new float[vertices.length];
        for (int i = 0; i < vertices.length/3; i++) {
            insertVector(colors, i, color);
        }
        return new GameObjectMesh(vertices, normals, colors, indices);
    }
    public static TextureMesh texturedRectangle(Vector2f position, Vector2f dimensions, Texture texture) {
        float[] vertices = new float[] {
                position.x, position.y, 0,
                position.x + dimensions.x, position.y, 0,
                position.x, position.y + dimensions.y, 0,
                position.x + dimensions.x, position.y + dimensions.y, 0
        };
        int[] indices = new int[] {0, 1, 2, 1, 3, 2};
        float[] textureCoords = new float[] {
                0, 0,
                1, 0,
                0, 1,
                1, 1
        };
        return new TextureMesh(vertices, textureCoords, indices, texture);
    }

    // Wrong and I don't know why
    public static void computeFaceNormals(float[] normals, float[] vertices, int[] indices) {
        Vector3f a = new Vector3f(), b = new Vector3f(), c = new Vector3f();
        for (int i = 0; i < indices.length; i += 3) {
            a.set(vertices[3*indices[i]], vertices[3*indices[i]+1], vertices[3*indices[i+2]+2]);
            b.set(vertices[3*indices[i+1]], vertices[3*indices[i+1]+1], vertices[3*indices[i+1]+2]);
            c.set(vertices[3*indices[i+2]], vertices[3*indices[i+2]+1], vertices[3*indices[i+2]+2]);
            Vector3f normal = triangleNormal(a, b, c);
            normals[3*indices[i]] = normals[3*indices[i+1]] = normals[3*indices[i+2]] = normal.x;
            normals[3*indices[i]+1] = normals[3*indices[i+1]+1] = normals[3*indices[i+2]+1] = normal.y;
            normals[3*indices[i]+2] = normals[3*indices[i+1]+2] = normals[3*indices[i+2]+2] = normal.z;
        }
    }
}
