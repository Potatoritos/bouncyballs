package game;

import geometry.Ball;
import geometry.Box;
import geometry.CollisionHandler;
import geometry.CollisionHandlerOld;
import graphics.GameObjectMesh;
import graphics.ShaderProgram;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import util.Util;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static geometry.MeshGeometry.generateGeodesicPolyhedronMesh;
import static geometry.MeshGeometry.rectangularPrismMesh;
import static org.lwjgl.opengl.GL30.*;
import static geometry.Geometry.*;

public class LevelScene extends Scene {
    private Level level;

    private final GameObjectMesh floorMesh;
    private final GameObjectMesh wallXMesh;
    private final GameObjectMesh wallYMesh;
    private final GameObjectMesh ballMesh;

    private final ShaderProgram colorNormals;
    private final ShaderProgram colorNormalsInstanced;
    private final ShaderProgram outline;
    private final ShaderProgram outlineInstanced;
    private final Vector3d rotation;

    private final ArrayList<Box> floorTiles;
    private final ArrayList<Box> wallXTiles;
    private final ArrayList<Box> wallYTiles;
    private final Ball ball;
    private final CollisionHandler collisionHandler;
    private long timer;
    public LevelScene() {
        super();
        floorMesh = rectangularPrismMesh(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 1, 0.25f),
                new Vector3f(1f, 1f, 1f)
        );
        wallXMesh = rectangularPrismMesh(
                new Vector3f(0, 0, 0),
                new Vector3f(0.1f, 1, 0.5f),
                new Vector3f(1f, 1f, 1f)
        );
        wallYMesh = rectangularPrismMesh(
                new Vector3f(0, -0, 0),
                new Vector3f(1, 0.1f, 0.5f),
                new Vector3f(1f, 1f, 1f)
        );
        ballMesh = generateGeodesicPolyhedronMesh(3, new Vector3f(1f, 1f, 1f));
        colorNormals = ShaderProgram.fromFile("color_normals.glsl");
        colorNormalsInstanced = ShaderProgram.fromFile("color_normals_instanced.glsl");
        outline = ShaderProgram.fromFile("outline.glsl");
        outlineInstanced = ShaderProgram.fromFile("outline_instanced.glsl");

        rotation = new Vector3d();

        floorTiles = new ArrayList<>();
        wallXTiles = new ArrayList<>();
        wallYTiles = new ArrayList<>();

        camera.position.z = 6;
//        ball = new Ball(new Vector3d(0.049043, 0.471136, 0.4), 0.4);
        ball = new Ball(new Vector3d(0.95, 0.2, 0.4), 0.4);

        collisionHandler = new CollisionHandler();
    }
    public void update(InputState input) {
        timer++;
        rotation.x = (Util.cutMaxMin(input.getMousePosition().y, 0, 1)-0.5) * Math.PI/3;
        rotation.y = (Util.cutMaxMin(input.getMousePosition().x, 0, 1)-0.5) * Math.PI/3;

//        if (timer >= 60) {
//            ball.velocity.x = -0.01;
//            ball.velocity.y = 0.01;
//        }

        ball.velocity.x += Math.sin(rotation.y * 0.002);
        ball.velocity.x = Util.cutMaxMin(ball.velocity.x, -0.2f, 0.2f);
        ball.velocity.y += -Math.sin(rotation.x * 0.002);
        ball.velocity.y = Util.cutMaxMin(ball.velocity.y, -0.2f, 0.2f);

        if (ball.velocity.length() > 0.1f) {
            ball.velocity.normalize(0.1f);
        }

        float border = 3;

        if (ball.position.x > border) {
            ball.position.x = border;
            ball.velocity.x = 0;
        }
        if (ball.position.x < -border) {
            ball.position.x = -border;
            ball.velocity.x = 0;
        }
        if (ball.position.y > border) {
            ball.position.y = border;
            ball.velocity.y = 0;
        }
        if (ball.position.y < -border) {
            ball.position.y = -border;
            ball.velocity.y = 0;
        }

        collisionHandler.reset();
        collisionHandler.setBall(ball);
        for (Box box : wallXTiles) {
            collisionHandler.addBox(box);
        }
        for (Box box : wallYTiles) {
            collisionHandler.addBox(box);
        }
        collisionHandler.processCollisions();
//        if (!collisionHandler.hasCollidedX() && !collisionHandler.hasCollidedY()) {
//        System.out.printf("z %.16f %.16f | %.16f %.16f\n", ball.position.x, ball.position.y, ball.velocity.x, ball.velocity.y);
//        }

        ball.update();
    }

//    void processCollisions(Ball ball, Box box) {
//        Vector2f result = new Vector2f();
//        if ((ball.velocity.x > 0 && intersectionRayWallX(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, box.position.x-ball.getRadius(), box.dimensions.y, box.position.y, result))
//        || (ball.velocity.x < 0 && intersectionRayWallX(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, box.position.x+box.dimensions.x+ball.getRadius(), box.dimensions.y, box.position.y, result))) {
//            ball.position.x = result.x;
//            ball.position.y = result.y;
//            if (Math.abs(ball.velocity.x) < 0.01f) {
//                ball.velocity.x = 0;
//            } else {
//                ball.velocity.x = -ball.velocity.x/4;
//            }
//        }
//        if (ball.velocity.x < 0 && intersectionRayWallX(ball.velocity.x, ball.position.x, ball.velocity.y, ball.position.y, box.position.x+box.dimensions.x+ball.getRadius(), box.dimensions.y, box.position.y, result)) {
//            ball.position.x = result.x;
//            ball.position.y = result.y;
//            ball.velocity.x = 0;
//        }
//    }

    private void setViewMatrices(ShaderProgram shader, ArrayList<Box> tiles) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(16*tiles.size());
        for (int i = 0; i < tiles.size(); i++) {
            camera.getViewMatrix(tiles.get(i).getWorldMatrix(rotation)).get(i*16, buffer);
        }
        shader.setUniform("viewMatrices", buffer);
        MemoryUtil.memFree(buffer);
    }
    public void render() {
        if (level == null) return;

        glClearColor(1, 1, 1, 1);
        glEnable(GL_STENCIL_TEST);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        glStencilMask(0xFF);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        glStencilFunc(GL_ALWAYS, 1, 0xFF);
        glStencilMask(0xFF); //

        colorNormals.bind();
        colorNormals.setUniform("projectionMatrix", camera.getProjectionMatrix());
        colorNormals.setUniform("viewMatrix", camera.getViewMatrix(ball.getWorldMatrix(rotation)));
        ballMesh.render();

        colorNormalsInstanced.bind();
        colorNormalsInstanced.setUniform("projectionMatrix", camera.getProjectionMatrix());

        setViewMatrices(colorNormalsInstanced, wallXTiles);
        wallXMesh.renderInstanced(wallXTiles.size());

        setViewMatrices(colorNormalsInstanced, wallYTiles);
        wallYMesh.renderInstanced(wallYTiles.size());

        setViewMatrices(colorNormalsInstanced, floorTiles);
        floorMesh.renderInstanced(floorTiles.size());

        glStencilFunc(GL_NOTEQUAL, 1, 0xFF);
        glStencilMask(0x00);

        outline.bind();
        outline.setUniform("projectionMatrix", camera.getProjectionMatrix());
        outline.setUniform("viewMatrix", camera.getViewMatrix(ball.getWorldMatrix(rotation)));
        outline.setUniform("expand", 0.05f);
        ballMesh.render();

        outlineInstanced.bind();
        outlineInstanced.setUniform("projectionMatrix", camera.getProjectionMatrix());
        outlineInstanced.setUniform("expand", 0.03f);

        setViewMatrices(outlineInstanced, floorTiles);
        floorMesh.renderInstanced(floorTiles.size());

        setViewMatrices(outlineInstanced, wallXTiles);
        wallXMesh.renderInstanced(wallXTiles.size());

        setViewMatrices(outlineInstanced, wallYTiles);
        wallYMesh.renderInstanced(wallYTiles.size());

        outlineInstanced.unbind();

        glDisable(GL_STENCIL_TEST);
    }
    public void loadLevel(Level level) {
        this.level = level;

        floorTiles.clear();
        wallXTiles.clear();
        wallYTiles.clear();

        for (int i = 0; i < level.getRows(); i++) {
            for (int j = 0; j < level.getColumns(); j++) {
                if (level.getFloorState(i, j)) {
                    Box tile = new Box(
                            new Vector3d(level.getPosX(j), level.getPosY(i), -0.25),
                            new Vector3d(1, 1, 0.25)
                    );
                    floorTiles.add(tile);
                }
            }
            for (int j = 0; j < level.getColumns()+1; j++) {
                if (level.getWallXState(i, j)) {
                    Box tile = new Box(
                            new Vector3d(level.getPosX(j)-0.05, level.getPosY(i), 0),
                            new Vector3d(0.1, 1, 0.5)
                    );
                    wallXTiles.add(tile);
                }
            }
        }
        for (int i = 0; i < level.getRows()+1; i++) {
            for (int j = 0; j < level.getColumns(); j++) {
                if (level.getWallYState(i, j)) {
                    Box tile = new Box(
                            new Vector3d(level.getPosX(j), level.getPosY(i)-0.05, 0),
                            new Vector3d(1, 0.1, 0.5)
                    );
                    wallYTiles.add(tile);
                }
            }
        }
    }
    public void delete() {
        floorMesh.delete();
        wallXMesh.delete();
        wallYMesh.delete();
        colorNormalsInstanced.delete();
        outlineInstanced.delete();
    }
}
