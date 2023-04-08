package algorithm;

import graphics.Mesh;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.JarEntry;


public class GeodesicPolyhedra {
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
            faces.add(top);
            faces.add(topHalf[i]);
            faces.add(topHalf[(i+1)%5]);

            faces.add(topHalf[i]);
            faces.add(bottomHalf[i]);
            faces.add(bottomHalf[(i+1)%5]);

            faces.add(topHalf[i]);
            faces.add(topHalf[(i+1)%5]);
            faces.add(bottomHalf[(i+1)%5]);

            faces.add(bottomHalf[i]);
            faces.add(bottomHalf[(i+1)%5]);
            faces.add(bottom);
        }
        return faces;
    }

    private static Vector3f sphericalMidpoint(Vector3f a, Vector3f b) {
        Vector3f midpoint = new Vector3f(a);
        midpoint.add(b);
        midpoint.normalize(1);
        return midpoint;
    }

    public static Mesh generateIcosphereMesh(int iterations) {
        ArrayList<Vector3f> faces = generateIcosahedronFaces();
        ArrayList<Vector3f> newFaces = new ArrayList<>();

        int numberEdges = 30;
        int numberVertices = 12;

        for (int it = 0; it < iterations; it++) {
            newFaces.ensureCapacity(4 * faces.size());
            for (int i = 0; i < faces.size(); i += 3) {
                Vector3f v1 = faces.get(i), v2 = faces.get(i+1), v3 = faces.get(i+2);
                Vector3f m12 = sphericalMidpoint(v1, v2);
                Vector3f m23 = sphericalMidpoint(v2, v3);
                Vector3f m13 = sphericalMidpoint(v1, v3);

                newFaces.add(v1);
                newFaces.add(m12);
                newFaces.add(m13);

                newFaces.add(v2);
                newFaces.add(m12);
                newFaces.add(m23);

                newFaces.add(v3);
                newFaces.add(m23);
                newFaces.add(m13);

                newFaces.add(m12);
                newFaces.add(m23);
                newFaces.add(m13);
            }
            ArrayList<Vector3f> temp = faces;
            faces = newFaces;
            newFaces = temp;

            newFaces.clear();

            numberVertices += numberEdges;
            numberEdges = numberVertices + faces.size()/3 - 2;
        }

        HashMap<Vector3f, Integer> ids = new HashMap<>();
        float[] vertices = new float[3*numberVertices];
        int[] indices = new int[faces.size()];

        int idCounter = 0;

        for (int i = 0; i < faces.size(); i ++) {
            int id;
            Vector3f vertex = faces.get(i);
            if (ids.containsKey(vertex)) {
                id = ids.get(vertex);
            } else {
                id = idCounter;
                ids.put(vertex, id);
                vertices[3*idCounter] = vertex.x;
                vertices[3*idCounter + 1] = vertex.y;
                vertices[3*idCounter + 2] = vertex.z;
                idCounter++;
            }
            indices[i] = id;
        }

        return new Mesh(vertices, indices);
    }


}
