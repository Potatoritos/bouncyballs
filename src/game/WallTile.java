package game;

public enum WallTile {
    NONE,
    WALL,
    WALL1,
    WALL2,
    WALL3;
    public static int wallColor(WallTile tile) {
        if (tile == WALL1) return 1;
        if (tile == WALL2) return 2;
        if (tile == WALL3) return 3;
        return 0;
    }
}
