package game;

import graphics.GameObjectMesh;
import graphics.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;
import static util.Geometry.rectangularPrismMesh;

public class LevelScene extends Scene {
    private Level level;

    private final GameObjectMesh floorMesh;
    private final GameObjectMesh wallXMesh;
    private final GameObjectMesh wallYMesh;

    private final ShaderProgram colorNormalsInstanced;
    private final ShaderProgram outlineInstanced;
    private final Vector2f rotation;

    private final ArrayList<BlockTile> floorTiles;
    private final ArrayList<BlockTile> wallXTiles;
    private final ArrayList<BlockTile> wallYTiles;
    public LevelScene() {
        super();
        floorMesh = rectangularPrismMesh(
                new Vector3f(0, 0, -0.25f),
                new Vector3f(1, 1, 0.25f),
                new Vector3f(1f, 1f, 1f)
        );
        wallXMesh = rectangularPrismMesh(
                new Vector3f(-0.05f, 0, 0),
                new Vector3f(0.1f, 1, 0.5f),
                new Vector3f(1f, 1f, 1f)
        );
        wallYMesh = rectangularPrismMesh(
                new Vector3f(0, -0.05f, 0),
                new Vector3f(1, 0.1f, 0.5f),
                new Vector3f(1f, 1f, 1f)
        );
        colorNormalsInstanced = ShaderProgram.fromFile("color_normals_instanced.glsl");
        outlineInstanced = ShaderProgram.fromFile("outline_instanced.glsl");

        rotation = new Vector2f();

        floorTiles = new ArrayList<>();
        wallXTiles = new ArrayList<>();
        wallYTiles = new ArrayList<>();

        camera.getPosition().z = 6;
    }
    public void update() {

    }
    private void setViewMatrices(ShaderProgram shader, ArrayList<BlockTile> tiles) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(16*tiles.size());
        for (int i = 0; i < tiles.size(); i++) {
            camera.getViewMatrix().mul(tiles.get(i).getWorldMatrix(rotation)).get(i*16, buffer);
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

        colorNormalsInstanced.bind();
        glStencilFunc(GL_ALWAYS, 1, 0xFF);
        colorNormalsInstanced.setUniform("projectionMatrix", projectionMatrix);

        setViewMatrices(colorNormalsInstanced, floorTiles);
        floorMesh.renderInstanced(floorTiles.size());

        setViewMatrices(colorNormalsInstanced, wallXTiles);
        wallXMesh.renderInstanced(wallXTiles.size());

        setViewMatrices(colorNormalsInstanced, wallYTiles);
        wallYMesh.renderInstanced(wallYTiles.size());

        colorNormalsInstanced.unbind();
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
                    BlockTile tile = new BlockTile(
                            new Vector3f(level.getPosX(j), level.getPosY(i), -0.25f),
                            new Vector3f(1, 1, 0.25f)
                    );
                    floorTiles.add(tile);
                }
            }
            for (int j = 0; j < level.getColumns()+1; j++) {
                if (level.getWallXState(i, j)) {
                    BlockTile tile = new BlockTile(
                            new Vector3f(level.getPosX(j)-0.05f, level.getPosY(i), 0),
                            new Vector3f(0.1f, 1, 0.5f)
                    );
                    wallXTiles.add(tile);
                }
            }
        }
        for (int i = 0; i < level.getRows()+1; i++) {
            for (int j = 0; j < level.getColumns(); j++) {
                if (level.getWallYState(i, j)) {
                    BlockTile tile = new BlockTile(
                            new Vector3f(level.getPosX(j), level.getPosY(i) - 0.05f, 0),
                            new Vector3f(1, 0.1f, 0.5f)
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
