package fr.sokoban.recursif;

import java.util.Objects;

public class Position {
    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    public Position move(Direction dir) {
        return new Position(row + dir.dr(), col + dir.dc());
    }

    public Position translate(int dr, int dc) {
        return new Position(row + dr, col + dc);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position other)) return false;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
