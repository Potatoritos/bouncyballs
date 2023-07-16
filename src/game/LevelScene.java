package game;

import audio.AudioHandler;
import collision.CollisionHandler;
import graphics.*;
import mesh.Quad;
import org.joml.Matrix3f;
import shape.Line3;
import shape.Sphere;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import util.Deletable;

import java.nio.FloatBuffer;
import java.time.Duration;
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
    private final GameObjectMesh tallTileMesh;
    private final GameObjectMesh ballMesh;
    private final GameObjectMesh[] gameObjectMeshes;

    private final ShaderProgram colorNormalsInstanced;
    private final ShaderProgram outlineInstanced;
    private final ShaderProgram outInstanced;
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
    private final ArrayList<Box> tallTiles;
    private final ArrayList<Ball> balls;
    private final ArrayList<? extends GameObject>[] gameObjects;
    private final CollisionHandler collisionHandler;
    private final double floorTileHeight = 0.5;
    private final double wallHeight = 0.75;

    private boolean hasDied;
    private boolean hasWon;
    private boolean isPaused;
    private boolean inPreviewMode;
    private boolean inMainMenuMode;
    private final ContinuousFrameTimer previewRotation;
    private final ContinuousFrameTimer mainMenuVelocity;

    public final FrameTimer stopwatch;

    private int windowWidth;
    private int windowHeight;

    private final AudioHandler audioHandler;
    private final Matrix3f rotationMatrix;

    public LevelScene(int windowWidth, int windowHeight, AudioHandler audioHandler) {
        super();
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.audioHandler = audioHandler;
        rotationMatrix = new Matrix3f();
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
                new Vector3f(0, 0, 0),
                new Vector3f(1.1f, 0.1f, (float)(floorTileHeight+wallHeight)),
                new Vector3f(0f, 0f, 0f)
        );
        tallTileMesh = rectangularPrismMesh(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 1, (float)(floorTileHeight+wallHeight)),
                new Vector3f(0, 0, 0)
        );
        ballMesh = generateGeodesicPolyhedronMesh(3, new Vector3f(0f, 1f, 0f));
        gameObjectMeshes = new GameObjectMesh[] {floorMesh, holeMesh, holeCoverMesh, wallXMesh, wallYMesh, tallTileMesh, ballMesh};

        colorNormalsInstanced = ShaderProgram.fromFile("color_normals_instanced.glsl");
        outlineInstanced = ShaderProgram.fromFile("outline_instanced.glsl");
        outInstanced = ShaderProgram.fromFile("shadows_sobelfilter.glsl");
        depthInstanced = ShaderProgram.fromFile("depth_instanced.glsl");
        textureShader = ShaderProgram.fromFile("texture.glsl");

        rotation = new Vector3d();

        floorTiles = new ArrayList<>();
        holeTiles = new ArrayList<>();
        coverTiles = new ArrayList<>();
        wallXTiles = new ArrayList<>();
        wallYTiles = new ArrayList<>();
        tallTiles = new ArrayList<>();
        balls = new ArrayList<>();
        gameObjects = new ArrayList[] {floorTiles, holeTiles, coverTiles, wallXTiles, wallYTiles, tallTiles, balls};

        camera.position.z = 6;

        edgeSourceFbo = new EmptyFbo(windowWidth, windowHeight);
        handleWindowResize(windowWidth, windowHeight);
        shadowMap = new ShadowMap(2048, 2048, 3.5f, 0.1f, 10f);

        hasDied = false;
        hasWon = false;
        collisionHandler = new CollisionHandler();

        previewRotation = new ContinuousFrameTimer(576);
        mainMenuVelocity = new ContinuousFrameTimer(576);

        stopwatch = new FrameTimer(Integer.MAX_VALUE-1);

    }

    /**
     * Updates the position of the camera based on the amount of tiles in a level.
     * Should be called every time you switch levels in the level select menu
     */
    public void updatePreviewCameraDistance() {
        float factor = cameraDistanceFactor();
        camera.position.set(0, -factor*11/12, factor*2/3);
    }

    /**
     * Enters the mode used for the level select menu.
     * Updates the camera and light position
     */
    public void enterPreviewMode() {
        previewRotation.start();
        inMainMenuMode = false;
        inPreviewMode = true;
        camera.rotation.x = -(float)Math.PI/4f;
        updatePreviewCameraDistance();
        shadowMap.setSourcePosition(new Vector3f(-2, 2, 4));
        shadowMap.updateLightSpaceMatrix();
        rotation.set(0, 0, 0);
    }

    /**
     * Undoes everything done by enterPreviewMode() and enterMainMenuMode()
     */
    public void enterLevelMode() {
        previewRotation.stop();
        inPreviewMode = false;
        inMainMenuMode = false;
        camera.position.set(0, 0, cameraDistanceFactor());
        camera.rotation.x = 0;
        shadowMap.setSourcePosition(new Vector3f(0, 0, 4));
        shadowMap.updateLightSpaceMatrix();
        rotation.z = 0;
    }

    /**
     * Sets the camera and light position to that used in the main menu screen
     */
    public void enterMainMenuMode() {
        previewRotation.stop();
        rotation.z = 0;
        inPreviewMode = false;
        inMainMenuMode = true;
        camera.rotation.x = 0;
        camera.position.set(0.5, 0, 5);
        shadowMap.setSourcePosition(new Vector3f(2, 2, 4));
        shadowMap.updateLightSpaceMatrix();
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

    public int getStarLevel() {
        return level.getStarLevel(stopwatch.getFrame());
    }

    /**
     * Update balls and handle their collisions
     */
    public void updateBalls(AudioHandler audioHandler) {
        int ballsWon = 0;

        for (Ball ball : balls) {
            if (ball.isDead()) {
                hasDied = true;
                continue;
            }
            if (ball.hasReachedGoal()) {
                ballsWon++;
                // Hide the ball if it has reached its goal
                ball.geometry.position.set(0, 0, 10000);
                continue;
            }
            if (ball.isInExplosionAnimation()) {
                ball.update(rotationMatrix);
                continue;
            }
            // Fade the ball out when it falls
            ball.getColor(0).w = (float)cutMaxMin(1.25 + ball.geometry.position.z, 0, 1);

            // Accelerate the ball based on the level's rotation
            ball.velocity.x += Math.sin(rotation.y * 0.0004);
            ball.velocity.x = cutMaxMin(ball.velocity.x, -0.04f, 0.04f);
            ball.velocity.y += -Math.sin(rotation.x * 0.0004);
            ball.velocity.y = cutMaxMin(ball.velocity.y, -0.04f, 0.04f);

            // Apply gravity
            ball.velocity.z -= 0.00045;

            // Handle collisions
            collisionHandler.reset();
            collisionHandler.setBall(ball);
            for (Box box : wallXTiles) collisionHandler.addBoxFloorColliders(box);
            for (Box box : wallYTiles) collisionHandler.addBoxFloorColliders(box);
            for (Box box : floorTiles) collisionHandler.addBoxFloorColliders(box);
            for (Box box : tallTiles) collisionHandler.addBoxFloorColliders(box);
            for (HoleBox box : holeTiles) collisionHandler.addHoleBoxColliders(box);

            for (Ball collisionBall : balls) {
                if (ball == collisionBall) continue;
                collisionHandler.addBallColliders(collisionBall);
            }
            collisionHandler.addFallDeathTrigger();

            collisionHandler.processCollisions();

            ball.update(rotationMatrix);
        }

        if (ballsWon == balls.size()) {
            hasWon = true;
        }
    }
    @Override
    public void update(InputState input) {
        audioHandler.listener.updatePosition(camera);
        if (inMainMenuMode) {
            // Continuously spawn balls that move downwards
            mainMenuVelocity.update();
            mainMenuVelocity.start();
            for (Ball ball : balls) {
                if (ball.isDead()) {
                    ball.setIsDead(false);
                    ball.velocity.set(Math.random()*0.01, Math.random()*0.01, 0);
                    ball.geometry.position.set(-0.5 - Math.random(), 3.35, 0.35);
                }

                ball.velocity.x -= 0.00004 * Math.sin(2*Math.PI*mainMenuVelocity.percentage());
                ball.velocity.y -= 0.00005;
            }
            updateBalls(audioHandler);
            return;
        }
        if (inPreviewMode) {
            // Continuously rotate the level
            rotation.z = previewRotation.percentage() * 2 * Math.PI;
            previewRotation.update();
            return;
        }

        // Calculate rotation based on mouse position
        rotation.x = (cutMaxMin(input.mousePosition.y*1.2 - 0.1, 0, 1)-0.5) * Math.PI/3;
        rotation.y = (cutMaxMin(input.mousePosition.x*1.2 - 0.1, 0, 1)-0.5) * Math.PI/3;
        if (rotation.length() > Math.PI/6) {
            rotation.normalize(Math.PI/6);
        }

        rotationMatrix.identity()
                .rotateX((float)rotation.x)
                .rotateY((float)rotation.y)
                .rotateZ((float)rotation.z);

        if (isPaused) {
            return;
        }
        if (!hasWon) {
            stopwatch.advanceFrame();
        }

        updateBalls(audioHandler);

        for (HoleBox hole : holeTiles) {
            hole.update(audioHandler);
        }
    }

    /**
     * Render all game objects. Meant for use with the normal coloring shader
     */
    private void renderGameNormals(ShaderProgram shader) {
        for (int i = 0; i < gameObjects.length; i++) {
            setViewMatrices(shader, gameObjects[i]);
            setTransparencies(shader, gameObjects[i]);
            gameObjectMeshes[i].renderInstanced(gameObjects[i].size());
        }
    }

    /**
     * Render all game objects. Meant for use with the shadow (depth) map shader
     */
    private void renderDepths(ShaderProgram shader) {
        for (int i = 0; i < gameObjects.length; i++) {
            setWorldMatrices(shader, gameObjects[i]);
            gameObjectMeshes[i].renderInstanced(gameObjects[i].size());
        }
    }

    /**
     * Render all game objects. Meant for use with the shadows + sobel filter shader
     */
    private void renderGameObjects(ShaderProgram shader) {
        for (int i = 0; i < gameObjects.length; i++) {
            setViewMatrices(shader, gameObjects[i]);
            setColors(0, shader, gameObjects[i]);
            setColors(1, shader, gameObjects[i]);
            setWorldMatrices(shader, gameObjects[i]);
            gameObjectMeshes[i].renderInstanced(gameObjects[i].size());
        }
    }

    // The following functions set up variables for use in shaders
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
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
        glEnable(GL_BLEND);
        glDisable(GL_STENCIL_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Draw normals to edgeSourceFbo
        // These normals are used to draw edges (using sobel filter edge detection)
        edgeSourceFbo.bind();
        glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        colorNormalsInstanced.bind();
        colorNormalsInstanced.setUniform("projectionMatrix", camera.getProjectionMatrix());

        renderGameNormals(colorNormalsInstanced);

        // Compute a shadow map
        // This is used to determine whether a fragment is in a shadow
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
        outInstanced.bind();
        outInstanced.setUniform("inShadowColor", Colors.background);
        outInstanced.setUniform("normalTexture", 0);
        outInstanced.setUniform("depthTexture", 1);
        outInstanced.setUniform("shadowMap", 2);
        outInstanced.setUniform("projectionMatrix", camera.getProjectionMatrix());
        outInstanced.setUniform("lightSpaceMatrix", shadowMap.lightSpaceMatrix);

        glActiveTexture(GL_TEXTURE0);
        edgeSourceFbo.getColorTexture().bind();
        glActiveTexture(GL_TEXTURE1);
        edgeSourceFbo.getDepthTexture().bind();
        glActiveTexture(GL_TEXTURE2);
        shadowMap.depthMap.getDepthTexture().bind();

        renderGameObjects(outInstanced);
    }
    @Override
    public void nvgRender(NanoVGContext nvg) {

    }

    /**
     * @return a factor used to determine how high to position the camera so that all tiles are visible
     */
    private float cameraDistanceFactor() {
        return (float)(Math.max(level.getRows(), level.getColumns())/Math.tan(camera.getFov()/2)) * 0.7f;
    }

    /**
     * Loads all game objects in a level
     */
    public void loadLevel(Level level) {
        this.level = level;

        // Clear all game objects / level state
        floorTiles.clear();
        holeTiles.clear();
        coverTiles.clear();
        wallXTiles.clear();
        wallYTiles.clear();
        tallTiles.clear();
        for (Ball ball : balls) {
             ball.delete();
        }
        balls.clear();

        hasWon = false;
        hasDied = false;

        camera.position.z = cameraDistanceFactor();
        shadowMap.setRadius(Math.max(level.getRows(), level.getColumns())*0.7f);
//        shadowMap.setFarPlane(factor * 1.25f);
        shadowMap.updateLightSpaceMatrix();

        stopwatch.start();

        for (int i = 0; i < level.getRows(); i++) {
            // Load all floor tiles
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
                } else if (level.getFloorState(i, j) == FloorTile.TALL) {
                    Box tile = new Box(new Line3(
                            new Vector3d(level.getPosX(j), level.getPosY(i), -floorTileHeight),
                            new Vector3d(1, 1, floorTileHeight+wallHeight)
                    ));
                    tile.getColor(0).set(Colors.tile);
                    tallTiles.add(tile);
                }
            }
            // Load all vertically-oriented walls
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
        // Load all horizontally-oriented walls
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

        // Load all balls
        for (int i = 0; i < level.numberBalls(); i++) {
            Ball ball = new Ball(
                    new Sphere(new Vector3d(level.getPosX(level.getBallColumn(i))+0.5, level.getPosY(level.getBallRow(i))+0.5, 0.35), 0.35),
                    audioHandler
            );
            ball.getColor(0).set(Colors.base[i]);
            ball.setHoleColor(i+1);
            balls.add(ball);
        }
    }

    /**
     * Reset the current level
     */
    public void reset() {
        loadLevel(level);
    }
    public void setPaused(boolean value) {
        isPaused = value;
    }
    public void delete() {
        for (Deletable obj : new Deletable[] {floorMesh, holeMesh, holeCoverMesh, wallXMesh, wallYMesh, tallTileMesh, ballMesh, colorNormalsInstanced, outlineInstanced, depthInstanced, outInstanced, textureShader}) {
            obj.delete();
        }
        for (Ball ball : balls) {
            ball.delete();
        }
    }
}
