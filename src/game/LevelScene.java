package game;

import graphics.EmptyFbo;
import graphics.FrameBufferObject;
import geometry.CollisionHandler2;
import graphics.GameObjectMesh;
import graphics.ShaderProgram;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import math.MathUtil;
import util.Deletable;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static geometry.MeshGeometry.generateGeodesicPolyhedronMesh;
import static geometry.MeshGeometry.rectangularPrismMesh;
import static org.lwjgl.opengl.GL30.*;

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
    private final ShaderProgram sobelFilterInstanced;
    private final EmptyFbo edgeSourceFbo;
    private final Vector3d rotation;

    private final ArrayList<Box> floorTiles;
    private final ArrayList<Box> wallXTiles;
    private final ArrayList<Box> wallYTiles;
    private final ArrayList<Ball> balls;
    private final Ball ball;
    private final CollisionHandler2 collisionHandler;
    private long timer;
    public LevelScene(int windowWidth, int windowHeight) {
        super();
        floorMesh = rectangularPrismMesh(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 1, 0.25f),
                new Vector3f(0.8f, 0.8f, 0.8f)
        );
        wallXMesh = rectangularPrismMesh(
                new Vector3f(0, 0, 0),
                new Vector3f(0.1f, 1, 0.5f),
                new Vector3f(0.8f, 0.8f, 0.8f)
        );
        wallYMesh = rectangularPrismMesh(
                new Vector3f(0, -0, 0),
                new Vector3f(1, 0.1f, 0.5f),
                new Vector3f(0.8f, 0.8f, 0.8f)
        );
        ballMesh = generateGeodesicPolyhedronMesh(3, new Vector3f(0.6f, 0.6f, 0.6f));
        colorNormals = ShaderProgram.fromFile("color_normals.glsl");
        colorNormalsInstanced = ShaderProgram.fromFile("color_normals_instanced.glsl");
        outline = ShaderProgram.fromFile("outline.glsl");
        outlineInstanced = ShaderProgram.fromFile("outline_instanced.glsl");
        sobelFilterInstanced = ShaderProgram.fromFile("sobel_filter_instanced.glsl");

        rotation = new Vector3d();

        floorTiles = new ArrayList<>();
        wallXTiles = new ArrayList<>();
        wallYTiles = new ArrayList<>();
        balls = new ArrayList<>();

        camera.position.z = 6;
        ball = new Ball(new Vector3d(0.95, 0.2, 0.4), 0.4);
        balls.add(ball);
        edgeSourceFbo = new EmptyFbo(windowWidth, windowHeight);
        handleWindowResize(windowWidth, windowHeight);

        collisionHandler = new CollisionHandler2();
    }
    @Override
    public void handleWindowResize(int width, int height) {
        super.handleWindowResize(width, height);
        edgeSourceFbo.resize(width, height);
    }
    public void update(InputState input) {
        timer++;
        rotation.x = (MathUtil.cutMaxMin(input.getMousePosition().y, 0, 1)-0.5) * Math.PI/3;
        rotation.y = (MathUtil.cutMaxMin(input.getMousePosition().x, 0, 1)-0.5) * Math.PI/3;

//        if (timer >= 60) {
//            ball.velocity.x = -0.01;
//            ball.velocity.y = 0.01;
//        }

        ball.velocity.x += Math.sin(rotation.y * 0.002);
        ball.velocity.x = MathUtil.cutMaxMin(ball.velocity.x, -0.2f, 0.2f);
        ball.velocity.y += -Math.sin(rotation.x * 0.002);
        ball.velocity.y = MathUtil.cutMaxMin(ball.velocity.y, -0.2f, 0.2f);

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

        ball.update();
    }

    private void renderGameObjects(ShaderProgram shader) {
        setViewMatrices(shader, wallXTiles);
        wallXMesh.renderInstanced(wallXTiles.size());

        setViewMatrices(shader, wallYTiles);
        wallYMesh.renderInstanced(wallYTiles.size());

        setViewMatrices(shader, floorTiles);
        floorMesh.renderInstanced(floorTiles.size());

        setViewMatrices(shader, balls);
        ballMesh.renderInstanced(balls.size());
    }
    private void setViewMatrices(ShaderProgram shader, ArrayList<? extends GameObject> tiles) {
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
//        glEnable(GL_STENCIL_TEST);
//        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
//        glStencilMask(0xFF);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

//        glStencilFunc(GL_ALWAYS, 1, 0xFF);
//        glStencilMask(0xFF);

        edgeSourceFbo.bind();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        colorNormals.bind();
        colorNormals.setUniform("projectionMatrix", camera.getProjectionMatrix());
        colorNormals.setUniform("viewMatrix", camera.getViewMatrix(ball.getWorldMatrix(rotation)));
//        ballMesh.render();

        colorNormalsInstanced.bind();
        colorNormalsInstanced.setUniform("projectionMatrix", camera.getProjectionMatrix());

        renderGameObjects(colorNormalsInstanced);

        FrameBufferObject.unbind();
        sobelFilterInstanced.bind();
        sobelFilterInstanced.setUniform("normalTexture", 0);
        sobelFilterInstanced.setUniform("depthTexture", 1);
        sobelFilterInstanced.setUniform("projectionMatrix", camera.getProjectionMatrix());

        glActiveTexture(GL_TEXTURE0);
        edgeSourceFbo.getColorTexture().bind();
        glActiveTexture(GL_TEXTURE1);
        edgeSourceFbo.getDepthTexture().bind();

        renderGameObjects(sobelFilterInstanced);


//        glStencilFunc(GL_NOTEQUAL, 1, 0xFF);
//        glStencilMask(0x00);

//        outline.bind();
//        outline.setUniform("projectionMatrix", camera.getProjectionMatrix());
//        outline.setUniform("viewMatrix", camera.getViewMatrix(ball.getWorldMatrix(rotation)));
//        outline.setUniform("expand", 0.05f);
//        ballMesh.render();

//        outlineInstanced.bind();
//        outlineInstanced.setUniform("projectionMatrix", camera.getProjectionMatrix());
//        outlineInstanced.setUniform("expand", 0.03f);
//
//        setViewMatrices(outlineInstanced, floorTiles);
//        floorMesh.renderInstanced(floorTiles.size());
//
//        setViewMatrices(outlineInstanced, wallXTiles);
//        wallXMesh.renderInstanced(wallXTiles.size());
//
//        setViewMatrices(outlineInstanced, wallYTiles);
//        wallYMesh.renderInstanced(wallYTiles.size());
//
//        outlineInstanced.unbind();
//
//        glDisable(GL_STENCIL_TEST);
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
        for (Deletable obj : new Deletable[] {floorMesh, wallXMesh, wallYMesh, colorNormalsInstanced, outlineInstanced}) {
            obj.delete();
        }
    }
}
