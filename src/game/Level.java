package game;

import java.io.*;
import java.util.ArrayList;

public class Level {
    private final int rows;
    private final int columns;
    private final boolean[][] floorState;
    private final boolean[][] wallXState;
    private final boolean[][] wallYState;
    private Level(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.floorState = new boolean[rows][columns];
        this.wallXState = new boolean[rows][columns+1];
        this.wallYState = new boolean[rows+1][columns];
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
    public boolean getFloorState(int row, int column) {
        return floorState[row][column];
    }
    public void setFloorState(int row, int column, boolean value) {
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
            if (rows <= 0 || rows > 100 || columns <= 0 || columns > 100) {
                throw new RuntimeException("Invalid level file");
            }
            Level level = new Level(rows, columns);

            for (int i = 0; i < rows; i++) {
                String row = br.readLine();
                if (row.length() != columns) new RuntimeException("Invalid level file");
                for (int j = 0; j < row.length(); j++) {
                    if (row.charAt(j) == '#') level.setFloorState(rows-1-i, j, true);
                }
            }
            for (int i = 0; i < rows; i++) {
                String row = br.readLine();
                if (row.length() != columns+1) throw new RuntimeException("Invalid level file");
                for (int j = 0; j < row.length(); j++) {
                    if (row.charAt(j) == '|') level.setWallXState(rows-1-i, j, true);
                }
            }
            for (int i = 0; i < rows+1; i++) {
                String row = br.readLine();
                if (row.length() != columns) throw new RuntimeException("Invalid level file");
                for (int j = 0; j < row.length(); j++) {
                    if (row.charAt(j) == '_') level.setWallYState(rows-i, j, true);
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
}
