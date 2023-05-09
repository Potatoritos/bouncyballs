package game;

import org.joml.Vector3f;

import java.util.ArrayList;

public class TileMap {
    private final int rows;
    private final int columns;
    private final Box[][] floorTileMap;
    private final ArrayList<Box> floorTiles;
    private final Box[][] wallTileMapX;
    private final ArrayList<Box> wallTilesX;
    private final Box[][] wallTileMapY;
    private final ArrayList<Box> wallTilesY;
    public TileMap(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        floorTileMap = new Box[rows][columns];
        floorTiles = new ArrayList<>();
        wallTileMapX = new Box[rows][columns+1];
        wallTilesX = new ArrayList<>();
        wallTileMapY = new Box[rows+1][columns];
        wallTilesY = new ArrayList<>();
    }
    public Box getFloorTile(int row, int column) {
        return floorTileMap[row][column];
    }
    private float getPosX(int column) {
        return column - columns/2f;
    }
    private float getPosY(int row) {
        return row - rows/2f;
    }
    public ArrayList<Box> getFloorTiles() {
        return floorTiles;
    }
    public ArrayList<Box> getWallTilesX() {
        return wallTilesX;
    }
    public ArrayList<Box> getWallTilesY() {
        return wallTilesY;
    }
    public void createFloorTile(int row, int column) {
        Box tile = new Box(
                new Vector3f(getPosX(column), getPosY(row), -0.25f),
                new Vector3f(1, 1, 0.25f)
        );
        floorTiles.add(tile);
        floorTileMap[row][column] = tile;
    }
    public void createWallTileX(int row, int column) {
        Box tile = new Box(
                new Vector3f(getPosX(column)-0.05f, getPosY(row), 0),
                new Vector3f(0.1f, 1, 0.5f)
        );
        wallTilesX.add(tile);
        wallTileMapX[row][column] = tile;
    }
    public void createWallTileY(int row, int column) {
        Box tile = new Box(
                new Vector3f(getPosX(column), getPosY(row) - 0.05f, 0),
                new Vector3f(1, 0.1f, 0.5f)
        );
        wallTilesY.add(tile);
        wallTileMapY[row][column] = tile;
    }
}
