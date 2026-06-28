package fr.sokoban.classique;

public enum Direction {
    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    private final int dr;
    private final int dc;

    Direction(int dr, int dc) {
        this.dr = dr;
        this.dc = dc;
    }

    public int dr() {
        return dr;
    }

    public int dc() {
        return dc;
    }

    public Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }

    public static Direction fromDelta(int dr, int dc) {
        for (Direction dir : values()) {
            if (dir.dr == dr && dir.dc == dc) {
                return dir;
            }
        }
        throw new IllegalArgumentException("Aucune direction pour delta : " + dr + ", " + dc);
    }
}