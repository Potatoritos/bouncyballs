package game;

import collision.CollisionHandler3;
import graphics.*;
import mesh.Quad;
import org.joml.Vector2f;
import shape.Line3;
import shape.Sphere;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import util.Deletable;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static math.MathUtil.cutMaxMin;
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
    private final ShaderProgram outShader;
    private final ShaderProgram depthInstanced;
    private final ShaderProgram textureShader;
    private final EmptyFbo edgeSourceFbo;
    private final ShadowMap shadowMap;
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

    private boolean hasDied;
    private boolean hasWon;
    private boolean isPaused;

    private int windowWidth;
    private int windowHeight;
//    private final TextureMesh view;
//    private final RenderEntity viewEntity;

    public LevelScene(int windowWidth, int windowHeight) {
        super();
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        floorMesh = rectangularPrismMesh(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 1, (float)floorTileHeight),
                new Vector3f(0f, 0f, 0f)
        );
        holeMesh = holeTileMesh(
                new Vector3f(0f, 0f, 0f),
                new Vector3f(1, 1, 0),
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
        ballMesh = generateGeodesicPolyhedronMesh(3, new Vector3f(0f, 1f, 0f));
        gameObjectMeshes = new GameObjectMesh[] {floorMesh, holeMesh, holeCoverMesh, wallXMesh, wallYMesh, ballMesh};

        colorNormalsInstanced = ShaderProgram.fromFile("color_normals_instanced.glsl");
        outlineInstanced = ShaderProgram.fromFile("outline_instanced.glsl");
        outShader = ShaderProgram.fromFile("shadows_sobelfilter.glsl");
        depthInstanced = ShaderProgram.fromFile("depth_instanced.glsl");
        textureShader = ShaderProgram.fromFile("texture.glsl");

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
        shadowMap = new ShadowMap(2048, 2048, 3.5f);

//        view = texturedRectangle(new Vector2f(0, 0), new Vector2f(1, 1), shadowMap.depthMap.getDepthTexture());
//        viewEntity = new RenderEntity(view, new Vector3f(1, 1, 2));

        hasDied = false;
        hasWon = false;
        collisionHandler = new CollisionHandler3();
    }
    public boolean hasDied() {
        return hasDied;
    }
    public boolean hasWon() {
        return hasWon;
    }
    @Override
    public void handleWindowResize(int width, int height) {
        super.handleWindowResize(width, height);
        edgeSourceFbo.resize(width, height);
        windowWidth = width;
        windowHeight = height;
    }
    public void update(InputState input) {
        rotation.x = (cutMaxMin(input.mousePosition.y*1.2 - 0.1, 0, 1)-0.5) * Math.PI/3;
        rotation.y = (cutMaxMin(input.mousePosition.x*1.2 - 0.1, 0, 1)-0.5) * Math.PI/3;
        if (rotation.length() > Math.PI/6) {
            rotation.normalize(Math.PI/6);
        }

        if (isPaused) {
            return;
        }

        int ballsWon = 0;

        for (Ball ball : balls) {
            if (ball.isDead()) {
                hasDied = true;
                continue;
            }
            if (ball.hasReachedGoal()) {
                ballsWon++;
                ball.geometry.position.set(0, 0, 10000);
                continue;
            }
            if (ball.isInExplosionAnimation()) {
                ball.update();
                continue;
            }
            ball.getColor(0).w = (float)cutMaxMin(1.25 + ball.geometry.position.z, 0, 1);

            ball.velocity.x += Math.sin(rotation.y * 0.0004);
            ball.velocity.x = cutMaxMin(ball.velocity.x, -0.04f, 0.04f);
            ball.velocity.y += -Math.sin(rotation.x * 0.0004);
            ball.velocity.y = cutMaxMin(ball.velocity.y, -0.04f, 0.04f);

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

        if (ballsWon == balls.size()) {
            hasWon = true;
        }

        for (HoleBox hole : holeTiles) {
            hole.update();
        }
    }

    private void renderGameNormals(ShaderProgram shader) {
        for (int i = 0; i < gameObjects.length; i++) {
            setViewMatrices(shader, gameObjects[i]);
            setTransparencies(shader, gameObjects[i]);
            gameObjectMeshes[i].renderInstanced(gameObjects[i].size());
        }
    }
    private void renderDepths(ShaderProgram shader) {
        for (int i = 0; i < gameObjects.length; i++) {
            setWorldMatrices(shader, gameObjects[i]);
            gameObjectMeshes[i].renderInstanced(gameObjects[i].size());
        }
    }
    private void renderGameObjects(ShaderProgram shader) {
        for (int i = 0; i < gameObjects.length; i++) {
            setViewMatrices(shader, gameObjects[i]);
            setColors(0, shader, gameObjects[i]);
            setColors(1, shader, gameObjects[i]);
            setWorldMatrices(shader, gameObjects[i]);
            gameObjectMeshes[i].renderInstanced(gameObjects[i].size());
        }
    }
    private void setTransparencies(ShaderProgram shader, ArrayList<? extends GameObject> objects) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(objects.size());
        for (int i = 0; i < objects.size(); i++) {
            buffer.put(i, objects.get(i).getColor(0).w);
        }
        shader.setUniform1fv("transparency", buffer);
        MemoryUtil.memFree(buffer);
    }
    private void setWorldMatrices(ShaderProgram shader, ArrayList<? extends GameObject> objects) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(16*objects.size());
        for (int i = 0; i < objects.size(); i++) {
            objects.get(i).getWorldMatrix(rotation).get(i*16, buffer);
        }
        shader.setUniformMatrix4fv("worldMatrices", buffer);
        MemoryUtil.memFree(buffer);
    }
    private void setViewMatrices(ShaderProgram shader, ArrayList<? extends GameObject> objects) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(16*objects.size());
        for (int i = 0; i < objects.size(); i++) {
            camera.getViewMatrix(objects.get(i).getWorldMatrix(rotation)).get(i*16, buffer);
        }
        shader.setUniformMatrix4fv("viewMatrices", buffer);
        MemoryUtil.memFree(buffer);
    }

    private void setColors(int index, ShaderProgram shader, ArrayList<? extends GameObject> objects) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(4*objects.size());
        for (int i = 0; i < objects.size(); i++) {
            objects.get(i).getColor(index).get(i*4, buffer);
        }
        shader.setUniform4fv("color" + index, buffer);
        MemoryUtil.memFree(buffer);
    }

    public void render() {
        if (level == null) return;

        glClearColor(Colors.background.x, Colors.background.y, Colors.background.z, 1);
//        glEnable(GL_STENCIL_TEST);
//        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
//        glStencilMask(0xFF);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
        glEnable(GL_BLEND);
        glDisable(GL_STENCIL_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

//        glStencilFunc(GL_ALWAYS, 1, 0xFF);
//        glStencilMask(0xFF);

        // Draw normals to edgeSourceFbo
        edgeSourceFbo.bind();
        glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        colorNormalsInstanced.bind();
        colorNormalsInstanced.setUniform("projectionMatrix", camera.getProjectionMatrix());

        renderGameNormals(colorNormalsInstanced);

        // Compute shadow map
        depthInstanced.bind();
        shadowMap.depthMap.bind();
        glViewport(0, 0, shadowMap.getWidth(), shadowMap.getHeight());
        glClear(GL_DEPTH_BUFFER_BIT);
//        glCullFace(GL_FRONT);
        depthInstanced.setUniform("lightSpaceMatrix", shadowMap.lightSpaceMatrix);
        renderDepths(depthInstanced);
        FrameBufferObject.unbind();
        glCullFace(GL_BACK);

        // Draw to screen
        glViewport(0, 0, windowWidth, windowHeight);
        outShader.bind();
        outShader.setUniform("normalTexture", 0);
        outShader.setUniform("depthTexture", 1);
        outShader.setUniform("shadowMap", 2);
        outShader.setUniform("projectionMatrix", camera.getProjectionMatrix());
        outShader.setUniform("lightSpaceMatrix", shadowMap.lightSpaceMatrix);


        glActiveTexture(GL_TEXTURE0);
        edgeSourceFbo.getColorTexture().bind();
        glActiveTexture(GL_TEXTURE1);
        edgeSourceFbo.getDepthTexture().bind();
        glActiveTexture(GL_TEXTURE2);
        shadowMap.depthMap.getDepthTexture().bind();

        renderGameObjects(outShader);

//        textureShader.bind();
//        textureShader.setUniform("viewMatrix", camera.getViewMatrix(viewEntity.getWorldMatrix()));
//        textureShader.setUniform("projectionMatrix", camera.getProjectionMatrix());
//        viewEntity.getMesh().render();
//

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

        hasWon = false;
        hasDied = false;

        for (int i = 0; i < level.getRows(); i++) {
            for (int j = 0; j < level.getColumns(); j++) {
                if (level.getFloorState(i, j) == FloorTile.FLOOR) {
                    Box tile = new Box(new Line3(
                            new Vector3d(level.getPosX(j), level.getPosY(i), -floorTileHeight),
                            new Vector3d(1, 1, floorTileHeight)
                    ));
                    tile.getColor(0).set(Colors.tile);
                    floorTiles.add(tile);

                } else if (level.getFloorState(i, j) == FloorTile.GOAL1 || level.getFloorState(i, j) == FloorTile.GOAL2 || level.getFloorState(i, j) == FloorTile.GOAL3) {
                    int holeColor = 1;
                    if (level.getFloorState(i, j) == FloorTile.GOAL2) {
                        holeColor = 2;
                    } else if (level.getFloorState(i, j) == FloorTile.GOAL3) {
                        holeColor = 3;
                    }
                    HoleBox tile = new HoleBox(new Line3(
                            new Vector3d(level.getPosX(j), level.getPosY(i), -floorTileHeight),
                            new Vector3d(1, 1, floorTileHeight)
                    ), 0.4);
                    tile.getColor(0).set(Colors.tile);
                    tile.getColor(1).set(Colors.base[holeColor-1]);
                    tile.setHoleColor(holeColor);
                    holeTiles.add(tile);
                    tile.cover.getColor(0).set(Colors.tile);
                    coverTiles.add(tile.cover);
                }
            }
            for (int j = 0; j < level.getColumns()+1; j++) {
                if (level.getWallXState(i, j)) {
                    Box tile = new Box(new Line3(
                            new Vector3d(level.getPosX(j)-0.05, level.getPosY(i)-0.05, -floorTileHeight),
                            new Vector3d(0.1, 1.1, floorTileHeight+wallHeight)
                    ));
                    tile.getColor(0).set(Colors.tile);
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
                    tile.getColor(0).set(Colors.tile);
                    wallYTiles.add(tile);
                }
            }
        }

        for (int i = 0; i < level.numberBalls(); i++) {
            Ball ball = new Ball(
                    new Sphere(new Vector3d(level.getPosX(level.getBallColumn(i))+0.5, level.getPosY(level.getBallRow(i))+0.5, 0.5), 0.35)
            );
            ball.getColor(0).set(Colors.base[i]);
            ball.setHoleColor(i+1);
            balls.add(ball);
        }
    }
    public void reset() {
        loadLevel(level);
    }
    public void setPaused(boolean value) {
        isPaused = value;
    }
    public void delete() {
        for (Deletable obj : new Deletable[] {floorMesh, holeMesh, holeCoverMesh, wallXMesh, wallYMesh, colorNormalsInstanced, outlineInstanced}) {
            obj.delete();
        }
    }
}
