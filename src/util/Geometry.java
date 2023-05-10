package util;

import graphics.GameObjectMesh;
import graphics.Texture;
import graphics.TextureMesh;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static java.lang.Math.sqrt;


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

        float x = 1/(float) sqrt(5);
        float y1 = (1-x)/2;
        float y2 = (1+x)/2;
        float z1 = (float) sqrt(y1);
        float z2 = (float) sqrt(y2);

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

//            Vector3f normal = triangleNormal(v1, v2, v3);
//            Vector3f normal = new Vector3f(0, 0, 0);

            insertVector(normals, i, v1);
            insertVector(normals, i+1, v2);
            insertVector(normals, i+2, v3);

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

    // Finds the intersection (x,y) closest to (b,d) of the
    // ray and circle given by the parametric equations
    //  ⎡ x = at + b
    //  ⎣ y = ct + d
    // and
    //  ⎡ x = r sin(s) + u
    //  ⎣ y = r cos(s) + v
    // where 0 ≤ t ≤ 1, 0 ≤ s ≤ 2π
    public static boolean intersectionRayCircle(double a, double b, double c, double d, double r, double u, double v, Vector2f result) {
        double A = a*a + c*c;
        double B = 2*a*(b-u) + 2*c*(d-v);
        double C = (b-u)*(b-u) + (d-v)*(d-v) - r*r;
        double D = B*B - 4*A*C;
        if (D < 0) return false;

        double sqrtD = Math.sqrt(D);
        double t1 = (-B + sqrtD) / (2*A);
        double t2 = (-B - sqrtD) / (2*A);

        if ((t1 < 0 || t1 > 1) && (t2 < 0 || t2 > 1)) return false;

        double x1 = a*t1 + b;
        double y1 = c*t1 + d;
        double x2 = a*t2 + b;
        double y2 = c*t2 + d;
        if (Math.hypot(x1-b, y1-d) <= Math.hypot(x2-b, y2-d)) {
            result.set(x1, y1);
        } else {
            result.set(x2, y2);
        }
        return true;
    }

    // Finds the intersection (x,y) of the
    // rays given by the parametric equations
    //  ⎡ x = at + b
    //  ⎣ y = ct + d
    // and
    //  ⎡ x = e
    //  ⎣ y = gs + h
    // where 0 ≤ s,t ≤ 1
    public static boolean intersectionRayWallX(double a, double b, double c, double d, double e, double g, double h, Vector2f result) {
//        if ((a == 0 && b != e) || g == 0) {
//            return false;
//        }
        if (a == 0 || g == 0) {
            return false;
        }
        double t = (e-b)/a;
        double s = (c*t + d - h)/g;
        if (t < 0 || t > 1 || s < 0 || s > 1) {
            return false;
        }
        result.set(a*t+b, c*t+d);
        return true;
    }

    // Finds the intersection (x,y) of the
    // rays given by the parametric equations
    //  ⎡ x = at + b
    //  ⎣ y = ct + d
    // and
    //  ⎡ x = es + f
    //  ⎣ y = g
    // where 0 ≤ s,t ≤ 1
    private static boolean intersectionRayWallY(double a, double b, double c, double d, double e, double f, double g, Vector2f result) {
        boolean r = intersectionRayWallX(c, d, a, b, g, e, f, result);
        result.set(result.y, result.x);
        return r;
    }

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
        float l = (float)sqrt(1f/3);
        float[] normals = new float[] {
                -l, -l, -l,   l, -l, -l,   -l, l, -l,   l, l, -l,
                l, -l, l,    -l, -l, l,    l, l, l,    -l, l, l,
                -l, -l, l,   -l, -l, -l,   -l, l, l,   -l, l, -l,
                l, -l, -l,    l, -l, l,    l, l, -l,    l, l, l,
                -l, -l, l,   l, -l, l,   -l, -l, -l,   l, -l, -l,
                -l, l, -l,    l, l, -l,    -l, l, l,    l, l, l
        };
        int[] indices = new int[] {
                0, 1, 2, 1, 3, 2,
                4, 5, 6, 5, 7, 6,
                8, 9, 10, 9, 11, 10,
                12, 13, 14, 13, 15, 14,
                16, 17, 18, 17, 19, 18,
                20, 21, 22, 21, 23, 22
        };
//        float[] normals = new  float[vertices.length];
//        for (int i = 0; i < vertices.length/3; i++) {
//            u.set(vertices[3*i], vertices[3*i+1], vertices[3*i+2]).sub(center).normalize();
//            insertVector(normals, i, u);
//        }

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
