package scene;

import collision.CollisionHandler3;
import game.*;
import geometry.Line3;
import geometry.Sphere;
import graphics.EmptyFbo;
import graphics.FrameBufferObject;
import graphics.GameObjectMesh;
import graphics.ShaderProgram;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import math.MathUtil;
import util.Deletable;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static mesh.MeshGeometry.*;
import static org.lwjgl.opengl.GL30.*;

public class LevelScene extends Scene {
    private Level level;

    private final GameObjectMesh floorMesh;
    private final GameObjectMesh holeMesh;
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
    private final ArrayList<HoleBox> holeTiles;
    private final ArrayList<Box> wallXTiles;
    private final ArrayList<Box> wallYTiles;
    private final ArrayList<Ball> balls;
//    private final Ball ball;
    private final CollisionHandler3 collisionHandler;
    private long timer;
    private final double floorTileHeight = 0.5;
    private final double wallHeight = 0.75;

    private final Vector3f tileColor = new Vector3f(219f/255, 215f/255, 184f/255);
    private final Vector3f bgColor = new Vector3f(143f/255, 133f/255, 17f/255);
    private final Vector3f yellow = new Vector3f(219f/255, 203f/255, 15f/255);
    private final Vector3f cyan = new Vector3f(38f/255, 181f/255, 219f/255);
    private final Vector3f magenta = new Vector3f(219f/255, 59f/255, 111f/255);
    public LevelScene(int windowWidth, int windowHeight) {
        super();
        tileColor.set(yellow);
        bgColor.set(new Vector3f(0f, 0f, 0f));
        floorMesh = rectangularPrismMesh(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 1, (float)floorTileHeight),
                new Vector3f(0f, 0f, 0f)
        );
        holeMesh = holeTileMesh(
                new Vector3f(0f, 0f, 0f),
                new Vector3f(1, 0, 0),
                20,
                0.4
        );
        wallXMesh = rectangularPrismMesh(
                new Vector3f(0, 0, 0),
                new Vector3f(0.1f, 1.1f, (float)(floorTileHeight+wallHeight)),
                new Vector3f(0f, 0f, 0f)
        );
        wallYMesh = rectangularPrismMesh(
                new Vector3f(0, -0, 0),
                new Vector3f(1.1f, 0.1f, (float)(floorTileHeight+wallHeight)),
                new Vector3f(0f, 0f, 0f)
        );
        ballMesh = generateGeodesicPolyhedronMesh(3, new Vector3f(0f, 0f, 0f));
        colorNormals = ShaderProgram.fromFile("color_normals.glsl");
        colorNormalsInstanced = ShaderProgram.fromFile("color_normals_instanced.glsl");
        outline = ShaderProgram.fromFile("outline.glsl");
        outlineInstanced = ShaderProgram.fromFile("outline_instanced.glsl");
        sobelFilterInstanced = ShaderProgram.fromFile("sobel_filter_instanced.glsl");

        rotation = new Vector3d();

        floorTiles = new ArrayList<>();
        holeTiles = new ArrayList<>();
        wallXTiles = new ArrayList<>();
        wallYTiles = new ArrayList<>();
        balls = new ArrayList<>();

        camera.position.z = 6;

//        ball = new Ball(new Sphere(new Vector3d(1.5, -0.5, 0.5), 0.35));
//        ball.color1.set(magenta);
//        ball.color2.set(magenta);
//
//        balls.add(ball);

        edgeSourceFbo = new EmptyFbo(windowWidth, windowHeight);
        handleWindowResize(windowWidth, windowHeight);

        collisionHandler = new CollisionHandler3();
    }
    @Override
    public void handleWindowResize(int width, int height) {
        super.handleWindowResize(width, height);
        edgeSourceFbo.resize(width, height);
    }
    public void update(InputState input) {
//        timer++;
        rotation.x = (MathUtil.cutMaxMin(input.getMousePosition().y, 0, 1)-0.5) * Math.PI/3;
        rotation.y = (MathUtil.cutMaxMin(input.getMousePosition().x, 0, 1)-0.5) * Math.PI/3;

//        if (timer == 10) {
//            ball.velocity.x = -0.04;
//            ball.velocity.y = 0.04;
//        }

        for (Ball ball : balls) {
            if (ball.geometry.position.z <= -1) {
                ball.geometry.position.set(1.5, -0.5, 0.5);
                ball.velocity.set(0,0,0);
            }

            ball.velocity.x += Math.sin(rotation.y * 0.002);
            ball.velocity.x = MathUtil.cutMaxMin(ball.velocity.x, -0.2f, 0.2f);
            ball.velocity.y += -Math.sin(rotation.x * 0.002);
            ball.velocity.y = MathUtil.cutMaxMin(ball.velocity.y, -0.2f, 0.2f);

            ball.velocity.z -= 0.002;

            if (ball.velocity.length() > 0.1f) {
                ball.velocity.normalize(0.1f);
            }

            collisionHandler.reset();
            collisionHandler.setBall(ball);
            for (Box box : wallXTiles) {
                collisionHandler.addFloorBox(box);
            }
            for (Box box : wallYTiles) {
                collisionHandler.addFloorBox(box);
            }
            for (Box box : floorTiles) {
                collisionHandler.addFloorBox(box);
            }
            for (HoleBox box : holeTiles) {
                collisionHandler.addHoleBox(box);
            }
            collisionHandler.processCollisions();

            ball.update();
        }
    }

    private void renderGameObjects(ShaderProgram shader) {
        setViewMatrices(shader, wallXTiles);
        wallXMesh.renderInstanced(wallXTiles.size());

        setViewMatrices(shader, holeTiles);
        holeMesh.renderInstanced(holeTiles.size());

        setViewMatrices(shader, wallYTiles);
        wallYMesh.renderInstanced(wallYTiles.size());

        setViewMatrices(shader, floorTiles);
        floorMesh.renderInstanced(floorTiles.size());

        setViewMatrices(shader, balls);
        ballMesh.renderInstanced(balls.size());
    }
    private void renderGameObjectsColor(ShaderProgram shader) {
        setViewMatrices(shader, wallXTiles);
        setColors(shader, wallXTiles);
        wallXMesh.renderInstanced(wallXTiles.size());

        setViewMatrices(shader, holeTiles);
        setColors(shader, holeTiles);
        holeMesh.renderInstanced(holeTiles.size());

        setViewMatrices(shader, wallYTiles);
        setColors(shader, wallYTiles);
        wallYMesh.renderInstanced(wallYTiles.size());

        setViewMatrices(shader, floorTiles);
        setColors(shader, floorTiles);
        floorMesh.renderInstanced(floorTiles.size());

        setViewMatrices(shader, balls);
        setColors(shader, balls);
        ballMesh.renderInstanced(balls.size());
    }
    private void setViewMatrices(ShaderProgram shader, ArrayList<? extends GameObject> objects) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(16*objects.size());
        for (int i = 0; i < objects.size(); i++) {
            camera.getViewMatrix(objects.get(i).getWorldMatrix(rotation)).get(i*16, buffer);
        }
        shader.setUniformMatrix4fv("viewMatrices", buffer);
        MemoryUtil.memFree(buffer);
    }
    private void setColors(ShaderProgram shader, ArrayList<? extends GameObject> objects) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(3*objects.size());
        for (int i = 0; i < objects.size(); i++) {
            objects.get(i).color1.get(i*3, buffer);
        }
        shader.setUniform3fv("color1", buffer);
        MemoryUtil.memFree(buffer);

        buffer = MemoryUtil.memAllocFloat(3*objects.size());
        for (int i = 0; i < objects.size(); i++) {
            objects.get(i).color2.get(i*3, buffer);
        }
        shader.setUniform3fv("color2", buffer);
        MemoryUtil.memFree(buffer);
    }
    public void render() {
        if (level == null) return;

        glClearColor(bgColor.x, bgColor.y, bgColor.z, 1);
//        glEnable(GL_STENCIL_TEST);
//        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
//        glStencilMask(0xFF);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glEnable(GL_CULL_FACE);

//        glStencilFunc(GL_ALWAYS, 1, 0xFF);
//        glStencilMask(0xFF);

        edgeSourceFbo.bind();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

//        colorNormals.bind();
//        colorNormals.setUniform("projectionMatrix", camera.getProjectionMatrix());
//        colorNormals.setUniform("viewMatrix", camera.getViewMatrix(ball.getWorldMatrix(rotation)));
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

        renderGameObjectsColor(sobelFilterInstanced);


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
        holeTiles.clear();
        wallXTiles.clear();
        wallYTiles.clear();
        balls.clear();

        for (int i = 0; i < level.getRows(); i++) {
            for (int j = 0; j < level.getColumns(); j++) {
                switch (level.getFloorState(i, j)) {
                    case FLOOR -> {
                        Box tile = new Box(new Line3(
                                new Vector3d(level.getPosX(j), level.getPosY(i), -floorTileHeight),
                                new Vector3d(1, 1, floorTileHeight)
                        ));
                        tile.color1.set(tileColor);
                        tile.color2.set(tileColor);
                        floorTiles.add(tile);
                    }
                    case GOAL1 -> {
                        HoleBox tile = new HoleBox(new Line3(
                                new Vector3d(level.getPosX(j), level.getPosY(i), -floorTileHeight),
                                new Vector3d(1, 1, floorTileHeight)
                        ), 0.4);
                        tile.color1.set(tileColor);
                        tile.color2.set(magenta);
                        holeTiles.add(tile);
                    }
                    case GOAL2 -> {
                        HoleBox tile = new HoleBox(new Line3(
                                new Vector3d(level.getPosX(j), level.getPosY(i), -floorTileHeight),
                                new Vector3d(1, 1, floorTileHeight)
                        ), 0.4);
                        tile.color1.set(tileColor);
                        tile.color2.set(cyan);
                        holeTiles.add(tile);
                    }
                }
            }
            for (int j = 0; j < level.getColumns()+1; j++) {
                if (level.getWallXState(i, j)) {
                    Box tile = new Box(new Line3(
                            new Vector3d(level.getPosX(j)-0.05, level.getPosY(i)-0.05, -floorTileHeight),
                            new Vector3d(0.1, 1.1, floorTileHeight+wallHeight)
                    ));
                    tile.color1.set(tileColor);
                    tile.color2.set(tileColor);
                    wallXTiles.add(tile);
                }
            }
        }
        for (int i = 0; i < level.getRows()+1; i++) {
            for (int j = 0; j < level.getColumns(); j++) {
                if (level.getWallYState(i, j)) {
                    Box tile = new Box(new Line3(
                            new Vector3d(level.getPosX(j)-0.05, level.getPosY(i)-0.05, -floorTileHeight),
                            new Vector3d(1.1, 0.1, floorTileHeight+wallHeight)
                    ));
                    tile.color1.set(tileColor);
                    tile.color2.set(tileColor);
                    wallYTiles.add(tile);
                }
            }
        }

        Vector3f[] ballColors = new Vector3f[] { magenta, cyan };
        for (int i = 0; i < level.numberBalls(); i++) {
            Ball ball = new Ball(
                    new Sphere(new Vector3d(level.getBallPosX(i), level.getBallPosY(i), 0.5), 0.35)
            );
            ball.color1.set(ballColors[i]);
            ball.color2.set(ballColors[i]);
            balls.add(ball);
        }
    }
    public void delete() {
        for (Deletable obj : new Deletable[] {floorMesh, wallXMesh, wallYMesh, colorNormalsInstanced, outlineInstanced}) {
            obj.delete();
        }
    }
}
