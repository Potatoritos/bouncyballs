package game;

import org.joml.Vector3d;

import java.io.*;

/**
 * Stores level data
 */
public class Level implements Comparable<Level> {
    private final int rows;
    private final int columns;
    private final FloorTile[][] floorState;
    private final WallTile[][] wallXState;
    private final WallTile[][] wallYState;
    private final int[] ballRow;
    private final int[] ballColumn;
    private final String name;
    private final int[] starTimeLimits;
    private boolean isMainMenu;
    private int color;
    private Level(String name, int rows, int columns, int numBalls) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.floorState = new FloorTile[rows][columns];
        this.wallXState = new WallTile[rows][columns+1];
        this.wallYState = new WallTile[rows+1][columns];
        ballRow = new int[numBalls];
        ballColumn = new int[numBalls];
        starTimeLimits = new int[3];
    }
    public boolean isMainMenu() {
        return isMainMenu;
    }
    public void setMainMenu(boolean value) {
        isMainMenu = value;
    }
    public String getName() {
        return name;
    }
    public int getRows() {
        return rows;
    }
    public int getColumns() {
        return columns;
    }
    public float getPosX(int column) {
        return column - columns/2f;
    }
    public float getPosY(int row) {
        return row - rows/2f;
    }
    public FloorTile getFloorState(int row, int column) {
        return floorState[row][column];
    }
    public void setFloorState(int row, int column, FloorTile value) {
        floorState[row][column] = value;
    }
    public WallTile getWallXState(int row, int column) {
        return wallXState[row][column];
    }
    public void setWallXState(int row, int column, WallTile value) {
        wallXState[row][column] = value;
    }
    public WallTile getWallYState(int row, int column) {
        return wallYState[row][column];
    }
    public void setWallYState(int row, int column, WallTile value) {
        wallYState[row][column] = value;
    }
    public int numberBalls() {
        return ballRow.length;
    }
    public void setBallRow(int ball, int value) {
        ballRow[ball] = value;
    }
    public int getBallRow(int ball) {
        return ballRow[ball];
    }
    public void setBallColumn(int ball, int value) {
        ballColumn[ball] = value;
    }
    public int getBallColumn(int ball) {
        return ballColumn[ball];
    }
    public void setStarTimeLimit(int starLevel, int frames) {
        starTimeLimits[starLevel] = frames;
    }
    public int getStarTimeLimit(int starLevel) {
        return starTimeLimits[starLevel];
    }
    public int getStarLevel(int framesElapsed) {
        int level = 0, sum = 0;
        while (level < 2 && framesElapsed > (sum += getStarTimeLimit(level))) {
            level++;
        }
        return level;
    }
    public void setColor(int value) {
        color = value;
    }
    public int  getColor() {
        return color;
    }

    /**
     * Creates a file that stores the level's data
     * (NOT IMPLEMENTED)
     * @param path the path of the file
     */
    public void exportToFile(String path) {
        try (FileWriter fw = new FileWriter("assets/levels/" + path);
            BufferedWriter bw = new BufferedWriter(fw)) {
        } catch (IOException e) {
            throw new RuntimeException("IOException");
        }
    }

    /**
     * Loads a level from a file
     * @param path the path of the file
     * @return the loaded level
     */
    public static Level fromFile(String path) {
        try (FileReader fr = new FileReader("assets/levels/" + path);
             BufferedReader br = new BufferedReader(fr)) {
            String name = br.readLine();
            int rows = Integer.parseInt(br.readLine());
            int columns = Integer.parseInt(br.readLine());
            int numBalls = Integer.parseInt(br.readLine());
            if (rows <= 0 || rows > 100 || columns <= 0 || columns > 100) {
                throw new RuntimeException("Invalid level dimensions");
            }
            if (numBalls == 0 || numBalls > 3) {
                throw new RuntimeException("Invalid level number of balls");
            }
            Level level = new Level(name, rows, columns, numBalls);
            if (name.equals("mainmenu")) {
                level.setMainMenu(true);
            }
            level.setStarTimeLimit(0, Integer.parseInt(br.readLine()));
            level.setStarTimeLimit(1, Integer.parseInt(br.readLine()));
            level.setStarTimeLimit(2, Integer.MAX_VALUE/2);

            level.setColor(Integer.parseInt(br.readLine()));

            // Process the spawn positions of each ball,
            // represented by a (rows) * (columns) grid
            for (int i = 0; i < rows; i++) {
                String row = br.readLine();
                if (row.length() != columns) new RuntimeException("Invalid level file - special tiles malformed");
                for (int j = 0; j < row.length(); j++) {
                    if ('1' <= row.charAt(j) && row.charAt(j) <= '1' + numBalls) {
                        int ball = row.charAt(j) - '1';
                        level.setBallRow(ball, rows-1-i);
                        level.setBallColumn(ball, j);
                    }
                }
            }

            // Process the floor tiles,
            // represented by a (rows) * (columns) grid
            for (int i = 0; i < rows; i++) {
                String row = br.readLine();
                if (row.length() != columns) new RuntimeException("Invalid level file - floor tiles malformed");
                for (int j = 0; j < row.length(); j++) {
                    switch(row.charAt(j)) {
                        case '.' -> level.setFloorState(rows-1-i, j, FloorTile.NONE);
                        case 'x' -> level.setFloorState(rows-1-i, j, FloorTile.FLOOR);
                        case '#' -> level.setFloorState(rows-1-i, j, FloorTile.TALL);
                        case '1' -> level.setFloorState(rows-1-i, j, FloorTile.GOAL1);
                        case '2' -> level.setFloorState(rows-1-i, j, FloorTile.GOAL2);
                        case '3' -> level.setFloorState(rows-1-i, j, FloorTile.GOAL3);
                    }
                }
            }
            // Process the vertically-oriented walls,
            // represented by a (rows) * (columns+1) grid
            for (int i = 0; i < rows; i++) {
                String row = br.readLine();
                if (row.length() != columns+1) throw new RuntimeException("Invalid level file - vertical walls malformed");
                for (int j = 0; j < row.length(); j++) {
                    switch (row.charAt(j)) {
                        case '.' -> level.setWallXState(rows-1-i, j, WallTile.NONE);
                        case '|' -> level.setWallXState(rows-1-i, j, WallTile.WALL);
                        case '1' -> level.setWallXState(rows-1-i, j, WallTile.WALL1);
                        case '2' -> level.setWallXState(rows-1-i, j, WallTile.WALL2);
                        case '3' -> level.setWallXState(rows-1-i, j, WallTile.WALL3);
                    }
                }
            }
            // Process the horizontally-oriented walls,
            // represented by a (rows+1) * (columns) grid
            for (int i = 0; i < rows+1; i++) {
                String row = br.readLine();
                if (row.length() != columns) throw new RuntimeException("Invalid level file - horizontal walls malformed");
                for (int j = 0; j < row.length(); j++) {
                    switch (row.charAt(j)) {
                        case '.' -> level.setWallYState(rows-i, j, WallTile.NONE);
                        case '_' -> level.setWallYState(rows-i, j, WallTile.WALL);
                        case '1' -> level.setWallYState(rows-i, j, WallTile.WALL1);
                        case '2' -> level.setWallYState(rows-i, j, WallTile.WALL2);
                        case '3' -> level.setWallYState(rows-i, j, WallTile.WALL3);
                    }
                }
            }

            return level;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid level file");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found");
        } catch (IOException e) {
            throw new RuntimeException("IOException");
        }
    }

    /**
     * Compares this level's name to another level's name
     * @param other the object to be compared.
     * @return -1, 0, or 1 if this.name < other.name, this.name == other.name, or this.name > other.name, respectively
     */
    public int compareTo(Level other) {
        return name.compareTo(other.name);
    }
}
