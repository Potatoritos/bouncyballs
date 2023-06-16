package game;

import collision.CollisionHandler3;
import collision.DeathTrigger;
import mesh.Quad;
import shape.Line3;
import shape.Plane;
import shape.Sphere;
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
    private final GameObjectMesh holeCoverMesh;
    private final GameObjectMesh wallXMesh;
    private final GameObjectMesh wallYMesh;
    private final GameObjectMesh ballMesh;
    private final GameObjectMesh[] gameObjectMeshes;

    private final ShaderProgram colorNormalsInstanced;
    private final ShaderProgram outlineInstanced;
    private final ShaderProgram sobelFilterInstanced;
    private final EmptyFbo edgeSourceFbo;
    private final Vector3d rotation;

    private final ArrayList<Box> floorTiles;
    private final ArrayList<HoleBox> holeTiles;
    private final ArrayList<HoleBoxCover> coverTiles;
    private final ArrayList<Box> wallXTiles;
    private final ArrayList<Box> wallYTiles;
    private final ArrayList<Ball> balls;
    private final ArrayList<? extends GameObject>[] gameObjects;
    private final CollisionHandler3 collisionHandler;
    private final double floorTileHeight = 0.5;
    private final double wallHeight = 0.75;

    private final Vector3f tileColor = new Vector3f(238f/255, 240f/255, 242f/255);
    private final Vector3f bgColor = new Vector3f(180f/255, 180f/255, 190f/255);
    private final Vector3f red = new Vector3f(255f/255, 91f/255, 91f/255);
    private final Vector3f blue = new Vector3f(136f/255, 132f/255, 255f/255);
    private final Vector3f green = new Vector3f(106f/255, 181f/255, 71f/255);
    public LevelScene(int windowWidth, int windowHeight) {
        super();
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
        holeCoverMesh = quadMesh(
                new Quad(
                        new Vector3f(0, 0, 0),
                        new Vector3f(1, 0, 0),
                        new Vector3f(1, 1, 0),
                        new Vector3f(0, 1, 0)
                ),
                new Vector3f(0, 0, 0)
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
        gameObjectMeshes = new GameObjectMesh[] {floorMesh, holeMesh, holeCoverMesh, wallXMesh, wallYMesh, ballMesh};

        colorNormalsInstanced = ShaderProgram.fromFile("color_normals_instanced.glsl");
        outlineInstanced = ShaderProgram.fromFile("outline_instanced.glsl");
        sobelFilterInstanced = ShaderProgram.fromFile("sobel_filter_instanced.glsl");

        rotation = new Vector3d();

        floorTiles = new ArrayList<>();
        holeTiles = new ArrayList<>();
        coverTiles = new ArrayList<>();
        wallXTiles = new ArrayList<>();
        wallYTiles = new ArrayList<>();
        balls = new ArrayList<>();
        gameObjects = new ArrayList[] {floorTiles, holeTiles, coverTiles, wallXTiles, wallYTiles, balls, };


        camera.position.z = 6;

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
        rotation.x = (MathUtil.cutMaxMin(input.getMousePosition().y*1.2 - 0.1, 0, 1)-0.5) * Math.PI/3;
        rotation.y = (MathUtil.cutMaxMin(input.getMousePosition().x*1.2 - 0.1, 0, 1)-0.5) * Math.PI/3;
        if (rotation.length() > Math.PI/6) {
            rotation.normalize(Math.PI/6);
        }

        for (Ball ball : balls) {
            if (ball.isDead()) {
                ball.geometry.position.set(1.5, -0.5, 0.5);
                ball.velocity.set(0,0,0);
                ball.setIsDead(false);
            }

            ball.velocity.x += Math.sin(rotation.y * 0.0004);
            ball.velocity.x = MathUtil.cutMaxMin(ball.velocity.x, -0.04f, 0.04f);
            ball.velocity.y += -Math.sin(rotation.x * 0.0004);
            ball.velocity.y = MathUtil.cutMaxMin(ball.velocity.y, -0.04f, 0.04f);

            ball.velocity.z -= 0.00048;

            collisionHandler.reset();
            collisionHandler.setBall(ball);
            for (Box box : wallXTiles) collisionHandler.addFloorBox(box);
            for (Box box : wallYTiles) collisionHandler.addFloorBox(box);
            for (Box box : floorTiles) collisionHandler.addFloorBox(box);
            for (HoleBox box : holeTiles) collisionHandler.addHoleBox(box);

            for (Ball collisionBall : balls) {
                if (ball == collisionBall) continue;
                collisionHandler.addBallCollision(collisionBall);
            }
            collisionHandler.addFallDeathTrigger();

            collisionHandler.processCollisions();

            ball.update();
        }
    }

    private void renderGameObjects(ShaderProgram shader) {
        for (int i = 0; i < gameObjects.length; i++) {
            setViewMatrices(shader, gameObjects[i]);
            gameObjectMeshes[i].renderInstanced(gameObjects[i].size());
        }
    }
    private void renderGameObjectsColor(ShaderProgram shader) {
        for (int i = 0; i < gameObjects.length; i++) {
            setViewMatrices(shader, gameObjects[i]);
            setColors(shader, gameObjects[i]);
            gameObjectMeshes[i].renderInstanced(gameObjects[i].size());
        }
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
        coverTiles.clear();
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
                        floorTiles.add(tile);
                    }
                    case GOAL1 -> {
                        HoleBox tile = new HoleBox(new Line3(
                                new Vector3d(level.getPosX(j), level.getPosY(i), -floorTileHeight),
                                new Vector3d(1, 1, floorTileHeight)
                        ), 0.4);
                        tile.color1.set(tileColor);
                        tile.color2.set(red);
                        tile.setHoleColor(1);
                        holeTiles.add(tile);
                        coverTiles.add(tile.cover);
                    }
                    case GOAL2 -> {
                        HoleBox tile = new HoleBox(new Line3(
                                new Vector3d(level.getPosX(j), level.getPosY(i), -floorTileHeight),
                                new Vector3d(1, 1, floorTileHeight)
                        ), 0.4);
                        tile.color1.set(tileColor);
                        tile.color2.set(blue);
                        tile.setHoleColor(2);
                        holeTiles.add(tile);
                        coverTiles.add(tile.cover);
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
                    wallYTiles.add(tile);
                }
            }
        }

        Vector3f[] ballColors = new Vector3f[] { red, blue };
        for (int i = 0; i < level.numberBalls(); i++) {
            Ball ball = new Ball(
                    new Sphere(new Vector3d(level.getBallPosX(i), level.getBallPosY(i), 0.5), 0.35)
            );
            ball.color1.set(ballColors[i]);
            ball.color2.set(ballColors[i]);
            ball.setHoleColor(i+1);
            balls.add(ball);
        }
    }
    public void delete() {
        for (Deletable obj : new Deletable[] {floorMesh, holeMesh, holeCoverMesh, wallXMesh, wallYMesh, colorNormalsInstanced, outlineInstanced}) {
            obj.delete();
        }
    }
}
