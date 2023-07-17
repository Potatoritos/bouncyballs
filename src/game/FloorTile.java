package game;

public enum FloorTile {
    NONE,
    FLOOR,
    TALL,
    SPIKE,
    GOAL1,
    GOAL2,
    GOAL3;
    public static int goalColor(FloorTile tile) {
        if (tile == GOAL1) return 1;
        if (tile == GOAL2) return 2;
        if (tile == GOAL3) return 3;
        return 0;
    }
}
