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
        }

//        HashMap<Vector3f, Integer> ids = new HashMap<>();
//        ArrayList<Float> vertices = new ArrayList<>();
//        int[] indices = new int[faces.size()];
//
//        float[] colors = new float[faces.size()*3];
//
//        float[][] boing = new float[3][3];
//        boing[0][0] = 0.5f;
//        boing[0][1] = 0;
//        boing[0][2] = 0;
//        boing[1][0] = 0;
//        boing[1][1] = 0.5f;
//        boing[1][2] = 0;
//        boing[2][0] = 0;
//        boing[2][1] = 0;
//        boing[2][2] = 0.5f;
//
//        int cnt=0;
//
//        for (int i = 0; i < faces.size(); i ++) {
//            int id;
//            Vector3f vertex = faces.get(i);
////            if (ids.containsKey(vertex)) {
////                id = ids.get(vertex);
////            } else {
//            id = vertices.size()/3;
//            ids.put(vertex, id);
//            vertices.add(vertex.x);
//            vertices.add(vertex.y);
//            vertices.add(vertex.z);
//
//            colors[3*i] = boing[cnt][0];
//            colors[3*i+1] = boing[cnt][1];
//            colors[3*i+2] = boing[cnt][2];
//            cnt = (cnt+1)%3;
//
////            }
//            indices[i] = id;
//        }
//
//        float[] verticesArray = new float[vertices.size()];
//        for (int i = 0; i < vertices.size(); i++) {
//            verticesArray[i] = vertices.get(i);
//        }

        HashMap<Vector3f, Integer> ids = new HashMap<>();
        ArrayList<Float> vertices = new ArrayList<>();
        int[] indices = new int[faces.size()];

        for (int i = 0; i < faces.size(); i ++) {
            int id;
            Vector3f vertex = faces.get(i);
//            if (ids.containsKey(vertex)) {
//                id = ids.get(vertex);
//            } else {
                id = vertices.size()/3;
                ids.put(vertex, id);
                vertices.add(vertex.x);
                vertices.add(vertex.y);
                vertices.add(vertex.z);
//            }
            indices[i] = id;
        }

        float[] colors = new float[vertices.size()];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = (float)Math.random();
        }
        float[] verticesArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            verticesArray[i] = vertices.get(i);
        }

        return new Mesh(verticesArray, indices, colors);
    }


}
