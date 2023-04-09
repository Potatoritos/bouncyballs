package util;

import graphics.BasicMesh;
import graphics.GameObjectMesh;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;


public class Geometry {
    public static Vector3f triangleNormal(Vector3f a, Vector3f b, Vector3f c) {
        Vector3f u = new Vector3f(), v = new Vector3f();
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
    private static void addVector(float[] arr, int index, Vector3f vector) {
        arr[3*index] = vector.x;
        arr[3*index + 1] = vector.y;
        arr[3*index + 2] = vector.z;
    }

    public static GameObjectMesh generateGeodesicPolyhedronMesh(int iterations) {
        ArrayList<Vector3f> faces = generateGeodesicPolyhedronFaces(iterations);
        float[] vertices = new float[3*faces.size()];
        float[] normals = new float[3*faces.size()];
        int[] indices = new int[faces.size()];

        for (int i = 0; i < faces.size(); i += 3) {
            Vector3f v1 = faces.get(i), v2 = faces.get(i+1), v3 = faces.get(i+2);
            addVector(vertices, i, v1);
            addVector(vertices, i+1, v2);
            addVector(vertices, i+2, v3);

            Vector3f normal = triangleNormal(v1, v2, v3);
            addVector(normals, i, normal);
            addVector(normals, i+1, normal);
            addVector(normals, i+2, normal);

            indices[i] = i;
            indices[i+1] = i+1;
            indices[i+2] = i+2;
        }

        return new GameObjectMesh(vertices, normals, indices);
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
    public static GameObjectMesh rectangularPrismMesh(Vector3f position, Vector3f dimensions) {
        Vector3f p = position, d = dimensions;
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
        float[] normals = new float[] {
                0, 0, -1,   0, 0, -1,   0, 0, -1,   0, 0, -1,
                0, 0, 1,    0, 0, 1,    0, 0, 1,    0, 0, 1,
                -1, 0, 0,   -1, 0, 0,   -1, 0, 0,   -1, 0, 0,
                1, 0, 0,    1, 0, 0,    1, 0, 0,    1, 0, 0,
                0, -1, 0,   0, -1, 0,   0, -1, 0,   0, -1, 0,
                0, 1, 0,    0, 1, 0,    0, 1, 0,    0, 1, 0
        };
        return new GameObjectMesh(vertices, normals, indices);
    }
}
