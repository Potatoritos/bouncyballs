package game;

import org.joml.Vector3d;

import java.io.*;

public class Level {
    private final int rows;
    private final int columns;
    private final FloorTile[][] floorState;
    private final boolean[][] wallXState;
    private final boolean[][] wallYState;
    private final double[] ballPosX;
    private final double[] ballPosY;
    private Level(int rows, int columns, int numBalls) {
        this.rows = rows;
        this.columns = columns;
        this.floorState = new FloorTile[rows][columns];
        this.wallXState = new boolean[rows][columns+1];
        this.wallYState = new boolean[rows+1][columns];
        ballPosX = new double[numBalls];
        ballPosY = new double[numBalls];
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
    public boolean getWallXState(int row, int column) {
        return wallXState[row][column];
    }
    public void setWallXState(int row, int column, boolean value) {
        wallXState[row][column] = value;
    }
    public boolean getWallYState(int row, int column) {
        return wallYState[row][column];
    }
    public void setWallYState(int row, int column, boolean value) {
        wallYState[row][column] = value;
    }
    public int numberBalls() {
        return ballPosX.length;
    }
    public void setBallPosX(int ball, double value) {
        ballPosX[ball] = value;
    }
    public double getBallPosX(int ball) {
        return ballPosX[ball];
    }
    public void setBallPosY(int ball, double value) {
        ballPosY[ball] = value;
    }
    public double getBallPosY(int ball) {
        return ballPosY[ball];
    }
    public void exportToFile(String path) {
        try (FileWriter fw = new FileWriter("assets/levels/" + path);
            BufferedWriter bw = new BufferedWriter(fw)) {

        } catch (IOException e) {
            throw new RuntimeException("IOException");
        }
    }
    public static Level fromFile(String path) {
        try (FileReader fr = new FileReader("assets/levels/" + path);
             BufferedReader br = new BufferedReader(fr)) {
            int rows = Integer.parseInt(br.readLine());
            int columns = Integer.parseInt(br.readLine());
            int numBalls = Integer.parseInt(br.readLine());
            if (rows <= 0 || rows > 100 || columns <= 0 || columns > 100) {
                throw new RuntimeException("Invalid level dimensions");
            }
            if (numBalls == 0 || numBalls > 2) {
                throw new RuntimeException("Invalid level number of balls");
            }
            Level level = new Level(rows, columns, numBalls);

            for (int i = 0; i < rows; i++) {
                String row = br.readLine();
                if (row.length() != columns) new RuntimeException("Invalid level file - floor tiles malformed");
                for (int j = 0; j < row.length(); j++) {
                    switch(row.charAt(j)) {
                        case '.' -> level.setFloorState(rows-1-i, j, FloorTile.NONE);
                        case '#' -> level.setFloorState(rows-1-i, j, FloorTile.FLOOR);
                        case '1' -> level.setFloorState(rows-1-i, j, FloorTile.GOAL1);
                        case '2' -> level.setFloorState(rows-1-i, j, FloorTile.GOAL2);
                    }
                }
            }
            for (int i = 0; i < rows; i++) {
                String row = br.readLine();
                if (row.length() != columns+1) throw new RuntimeException("Invalid level file - vertical walls malformed");
                for (int j = 0; j < row.length(); j++) {
                    if (row.charAt(j) == '|') level.setWallXState(rows-1-i, j, true);
                }
            }
            for (int i = 0; i < rows+1; i++) {
                String row = br.readLine();
                if (row.length() != columns) throw new RuntimeException("Invalid level file - horizontal walls malformed");
                for (int j = 0; j < row.length(); j++) {
                    if (row.charAt(j) == '_') level.setWallYState(rows-i, j, true);
                }
            }

            for (int i = 0; i < numBalls; i++) {
                level.setBallPosX(i, Double.parseDouble(br.readLine()));
                level.setBallPosY(i, Double.parseDouble(br.readLine()));
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
}
