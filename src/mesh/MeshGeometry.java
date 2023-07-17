package mesh;

import graphics.GameObjectMesh;
import graphics.Mesh;
import graphics.Texture;
import graphics.TextureMesh;
import org.joml.Vector2f;
import org.joml.Vector3f;
import shape.Line3f;

import java.util.ArrayList;

import static java.lang.Math.sqrt;
import static math.MathUtil.insertVector;

/**
 * A collection of functions that create meshes
 */
public class MeshGeometry {
    private final static Vector3f u;
    private final static Vector3f v;
    static {
        u = new Vector3f();
        v = new Vector3f();
    }

    /**
     * Finds the face normal of a triangle defined by 3 points
     * @return the normal
     */
    public static Vector3f triangleNormal(Vector3f a, Vector3f b, Vector3f c) {
        b.sub(a, u);
        c.sub(a, v);
        return u.cross(v).normalize();
    }
    public static Vector3f triangleNormal(Triangle triangle) {
        return triangleNormal(triangle.a, triangle.b, triangle.c);
    }

    /**
     * Adds three Vector3fs to a list
     */
    private static void addTriangle(ArrayList<Vector3f> list, Vector3f a, Vector3f b, Vector3f c) {
        list.add(a);
        list.add(b);
        list.add(c);
    }

    /**
     * Generates the faces of the icosahedron with vertices that lie on the unit sphere
     * @return A list containing the vertices of every face (every three consecutive elements makes up one triangle)
     */
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

    /**
     * Finds the spherical midpoint of two points on the unit sphere
     * @return the midpoint
     */
    public static Vector3f sphericalMidpoint(Vector3f a, Vector3f b) {
        Vector3f midpoint = new Vector3f(a);
        midpoint.add(b);
        midpoint.normalize(1);
        return midpoint;
    }

    /**
     * Generates the faces of a geodesic polyhedron (polyhedron that approximates a sphere) by starting with an icosahedron, then incrementally dividing the faces of the polyhedron into 4 more faces
     * @param iterations the amount of times to subdivide faces
     * @return A list containing the vertices of every face (every three consecutive elements makes up one triangle)
     */
    public static ArrayList<Vector3f> generateGeodesicPolyhedronFaces(int iterations) {
        ArrayList<Vector3f> faces = generateIcosahedronFaces();
        ArrayList<Vector3f> newFaces = new ArrayList<>();

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
        }
        return faces;
    }

    /**
     * Creates a geodesic polyhedron mesh
     * @param iterations the amount of iterations done when creating the geodesic polyhedron
     * @param color the mesh's color
     * @return the mesh
     */
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

        for (int i = 0; i < faces.size(); i++) {
            insertVector(colors, i, color);
        }

        return new GameObjectMesh(vertices, normals, colors, indices);
    }

    /**
     * Creates the mesh of a hole tile
     * @param surfaceColor the color of the surface (non-hole) area
     * @param holeColor the color of the bottom of the hole
     * @param cylinderFaces a factor denoting how many faces the cylinder making up the hole should have
     * @param radius the radius of the hole
     * @return the mesh
     */
    public static GameObjectMesh holeTileMesh(Vector3f surfaceColor, Vector3f holeColor, int cylinderFaces, double radius) {
        MeshBuilder builder = new MeshBuilder();
        Quad pzNormals = new Quad(new Vector3f(0, 0, 1));
        Quad surfaceColors = new Quad(surfaceColor, surfaceColor, surfaceColor, surfaceColor);
        float height = 0.5f;

        // two rectangles at the top and bottom of the hole
        float circleTop = (float)(0.5-radius);
        builder.addQuad(
                new Quad(
                        new Vector3f(0, 0, height),
                        new Vector3f(1, 0, height),
                        new Vector3f(1, circleTop, height),
                        new Vector3f(0, circleTop, height)
                ),
                pzNormals,
                surfaceColors
        );
        builder.addQuad(
                new Quad(
                        new Vector3f(0, 1-circleTop, height),
                        new Vector3f(1, 1-circleTop, height),
                        new Vector3f(1, 1, height),
                        new Vector3f(0, 1, height)
                ),
                pzNormals,
                surfaceColors
        );

        // rectangle at the bottom of the tile
        builder.addQuad(
                new Quad(
                        new Vector3f(0, 0, 0),
                        new Vector3f(1, 0, 0),
                        new Vector3f(1, 1, 0),
                        new Vector3f(0, 1, 0)
                ),
                new Quad(new Vector3f(0, 0, -1)),
                new Quad(holeColor)
        );

        // side faces
        builder.addQuad(
                new Quad(
                        new Vector3f(0, 0, 0),
                        new Vector3f(1, 0, 0),
                        new Vector3f(1, 0, height),
                        new Vector3f(0, 0, height)
                ),
                new Quad(new Vector3f(0, -1, 0)),
                surfaceColors
        );
        builder.addQuad(
                new Quad(
                        new Vector3f(1, 1, 0),
                        new Vector3f(0, 1, 0),
                        new Vector3f(0, 1, height),
                        new Vector3f(1, 1, height)
                ),
                new Quad(new Vector3f(0, 1, 0)),
                surfaceColors
        );
        builder.addQuad(
                new Quad(
                        new Vector3f(0, 1, 0),
                        new Vector3f(0, 0, 0),
                        new Vector3f(0, 0, height),
                        new Vector3f(0, 1, height)
                ),
                new Quad(new Vector3f(-1, 0, 0)),
                surfaceColors
        );
        builder.addQuad(
                new Quad(
                        new Vector3f(1, 0, 0),
                        new Vector3f(1, 1, 0),
                        new Vector3f(1, 1, height),
                        new Vector3f(1, 0, height)
                ),
                new Quad(new Vector3f(1, 0, 0)),
                surfaceColors
        );

        // the hole and the rest of the polygons on the top face
        for (int i = 0; i < cylinderFaces; i++) {
            double angle1 = i*Math.PI/cylinderFaces;
            double angle2 = (i+1)*Math.PI/cylinderFaces;

            float x1 = (float)(radius*Math.sin(angle1)), x2 = (float)(radius*Math.sin(angle2));
            float y1 = (float)(radius*Math.cos(angle1)), y2 = (float)(radius*Math.cos(angle2));

            Vector3f t1 = new Vector3f(x1 + 0.5f, y1 + 0.5f, height);
            Vector3f t2 = new Vector3f(x2 + 0.5f, y2 + 0.5f, height);

            Vector3f n1 = new Vector3f(x1, y1, 0).negate();
            Vector3f n2 = new Vector3f(x2, y2, 0).negate();

            builder.addQuad(
                    new Quad(
                            new Vector3f(t1).sub(0, 0, height),
                            new Vector3f(t2).sub(0, 0, height),
                            t2,
                            t1
                    ),
                    new Quad(n1, n2, n2, n1),
                    new Quad(holeColor, holeColor, surfaceColor, surfaceColor)
            );
            builder.addQuad(
                    new Quad(
                            t1,
                            t2,
                            new Vector3f(1, y2+0.5f, height),
                            new Vector3f(1, y1+0.5f, height)),
                    pzNormals,
                    surfaceColors
            );

            t1.set(-x1 + 0.5f, y1 + 0.5f, height);
            t2.set(-x2 + 0.5f, y2 + 0.5f, height);

            n1.set(-x1, y1, 0).negate();
            n2.set(-x2, y2, 0).negate();
            builder.addQuad(
                    new Quad(
                            new Vector3f(t2).sub(0, 0, height),
                            new Vector3f(t1).sub(0, 0, height),
                            t1,
                            t2
                    ),
                    new Quad(n2, n1, n1, n2),
                    new Quad(holeColor, holeColor, surfaceColor, surfaceColor)
            );
            builder.addQuad(
                    new Quad(
                            t2,
                            t1,
                            new Vector3f(0, y1+0.5f, height),
                            new Vector3f(0, y2+0.5f, height)
                    ),
                    pzNormals,
                    surfaceColors
            );
        }
        return builder.createMesh();
    }

    /**
     * Creates the mesh of an axis-aligned box
     * @param box the position and dimensions of the rectangular prism
     * @param color the color of the prism
     * @return the mesh
     */
    public static GameObjectMesh axisAlignedBoxMesh(Line3f box, Vector3f color) {
        MeshBuilder builder = new MeshBuilder();
        builder.addAxisAlignedBox(box, color);
        return builder.createMesh();
    }

    public static GameObjectMesh spikeTileMesh(Line3f box, Vector3f color, float spikeHeight, int n) {
        MeshBuilder builder = new MeshBuilder();
        builder.addAxisAlignedBox(box, color);
        Vector3f top = new Vector3f();
        Triangle triangleColor = new Triangle(color);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                float i1 = (float)i / n, i2 = (float)(i+1) / n;
                float j1 = (float)j / n, j2 = (float)(j+1) / n;
                float x1 = box.x1() + i1*box.displacement.x;
                float x2 = box.x1() + i2*box.displacement.x;
                float y1 = box.y1() + j1*box.displacement.y;
                float y2 = box.y1() + j2*box.displacement.y;
                top.set((x1+x2)/2, (y1+y2)/2, box.z2() + spikeHeight);

                Triangle triangle = new Triangle(top, new Vector3f(x1, y1, box.z2()), new Vector3f(x2, y1, box.z2()));
                builder.addTriangle(triangle, new Triangle(triangleNormal(triangle)), triangleColor);

                triangle = new Triangle(top, new Vector3f(x2, y1, box.z2()), new Vector3f(x2, y2, box.z2()));
                builder.addTriangle(triangle, new Triangle(triangleNormal(triangle)), triangleColor);

                triangle = new Triangle(top, new Vector3f(x2, y2, box.z2()), new Vector3f(x1, y2, box.z2()));
                builder.addTriangle(triangle, new Triangle(triangleNormal(triangle)), triangleColor);

                triangle = new Triangle(top, new Vector3f(x1, y2, box.z2()), new Vector3f(x1, y1, box.z2()));
                builder.addTriangle(triangle, new Triangle(triangleNormal(triangle)), triangleColor);
            }
        }
        return builder.createMesh();
    }

    /**
     * Creates a quadrilateral mesh
     * @param quad the quadrilateral
     * @param color the color of the quadrilateral
     * @return the mesh
     */
    public static GameObjectMesh quadMesh(Quad quad, Vector3f color) {
        MeshBuilder builder = new MeshBuilder();
        builder.addQuad(quad, new Quad(triangleNormal(quad.a, quad.b, quad.c)), new Quad(color));
        return builder.createMesh();
    }

    /**
     * Creates a Z-axis-facing rectangular mesh that has a texture
     * @param position the position of the rectangle
     * @param dimensions the dimensions of the rectangle
     * @param texture the texture to map onto the rectangle
     * @return the mesh
     */
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
}
