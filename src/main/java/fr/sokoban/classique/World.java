package fr.sokoban.classique;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class World {
    private final char name;
    private final int size;
    private final CellType[][] cells;
    private final Color color;

    private Position playerPosition;
    private final List<Box> boxes;

    public World(char name, int size, Color color) {
        this.name = name;
        this.size = size;
        this.color = color;
        this.cells = new CellType[size][size];
        this.boxes = new ArrayList<>();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                cells[r][c] = CellType.EMPTY;
            }
        }

        this.playerPosition = new Position(1, 1);
    }

    public char getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public Color getColor() {
        return color;
    }

    public Position getPlayerPosition() {
        return playerPosition;
    }

    public void setPlayerPosition(Position playerPosition) {
        this.playerPosition = playerPosition;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public void addBox(Box box) {
        boxes.add(box);
    }

    public void removeBox(Box box) {
        boxes.remove(box);
    }

    public void removeBoxAt(Position pos) {
        Box box = getBoxAt(pos);
        if (box != null) {
            boxes.remove(box);
        }
    }

    public Box getBoxAt(Position pos) {
        for (Box box : boxes) {
            if (box.getPosition().equals(pos)) {
                return box;
            }
        }
        return null;
    }

    public boolean hasBoxAt(Position pos) {
        return getBoxAt(pos) != null;
    }

    public Set<Position> getBoxesPositions() {
        Set<Position> positions = new HashSet<>();
        for (Box box : boxes) {
            positions.add(box.getPosition());
        }
        return positions;
    }

    public boolean isInside(Position p) {
        return p.row() >= 0 && p.row() < size && p.col() >= 0 && p.col() < size;
    }

    public CellType getCell(Position p) {
        return cells[p.row()][p.col()];
    }

    public void setCell(Position p, CellType type) {
        if (!isInside(p)) {
            return;
        }
        cells[p.row()][p.col()] = type;
    }

    public boolean isWall(Position p) {
        return !isInside(p) || getCell(p) == CellType.WALL;
    }

    public boolean isWall(int row, int col) {
        return !isInside(new Position(row, col)) || cells[row][col] == CellType.WALL;
    }

    public boolean isTarget(Position p) {
        return isInside(p) && getCell(p) == CellType.TARGET;
    }

    public boolean isFree(Position p) {
        return isInside(p) && !isWall(p) && !hasBoxAt(p);
    }

    public boolean isFree(Position p, Set<Position> blockedBoxes) {
        return isInside(p) && !isWall(p) && !blockedBoxes.contains(p);
    }

    public int countTargets() {
        int count = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (cells[r][c] == CellType.TARGET) {
                    count++;
                }
            }
        }
        return count;
    }

    public int countBoxesOnTargets() {
        int count = 0;
        for (Box box : boxes) {
            if (isTarget(box.getPosition())) {
                count++;
            }
        }
        return count;
    }

    public void clearAll() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                cells[r][c] = CellType.EMPTY;
            }
        }
        boxes.clear();
        playerPosition = new Position(1, 1);
    }

    public boolean isVictory() {
        if (boxes.isEmpty()) {
            return false;
        }

        int targets = countTargets();
        if (targets == 0) {
            return false;
        }

        for (Box box : boxes) {
            if (!isTarget(box.getPosition())) {
                return false;
            }
        }

        return true;
    }

    public World deepCopy() {
        World copy = new World(name, size, color);

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                copy.cells[r][c] = this.cells[r][c];
            }
        }

        copy.playerPosition = new Position(playerPosition.row(), playerPosition.col());

        for (Box box : boxes) {
            copy.boxes.add(box.copy());
        }

        return copy;
    }
}
