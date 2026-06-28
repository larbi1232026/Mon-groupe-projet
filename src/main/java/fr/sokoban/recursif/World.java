package fr.sokoban.recursif;

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
    private final List<AbstractBox> boxes;

    private World parentWorld;
    private WorldBox containerBox;

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

    public List<AbstractBox> getBoxes() {
        return boxes;
    }

    public World getParentWorld() {
        return parentWorld;
    }

    public void setParentWorld(World parentWorld) {
        this.parentWorld = parentWorld;
    }

    public WorldBox getContainerBox() {
        return containerBox;
    }

    public void setContainerBox(WorldBox containerBox) {
        this.containerBox = containerBox;
    }

    public void addBox(AbstractBox box) {
        boxes.add(box);

        if (box instanceof WorldBox worldBox) {
            World inner = worldBox.getInnerWorld();
            if (inner != null) {
                inner.setParentWorld(this);
                inner.setContainerBox(worldBox);
            }
        }
    }

    public void removeBox(AbstractBox box) {
        boxes.remove(box);
    }

    public AbstractBox getBoxAt(Position pos) {
        for (AbstractBox box : boxes) {
            if (box.getPosition().equals(pos)) {
                return box;
            }
        }
        return null;
    }

    public boolean hasBoxAt(Position pos) {
        return getBoxAt(pos) != null;
    }

    public boolean isInside(Position p) {
        return p != null
                && p.row() >= 0 && p.row() < size
                && p.col() >= 0 && p.col() < size;
    }

    public CellType getCell(Position p) {
        return cells[p.row()][p.col()];
    }

    public void setCell(Position p, CellType type) {
        if (!isInside(p)) return;
        cells[p.row()][p.col()] = type;
    }

    public boolean isWall(Position p) {
        return !isInside(p) || getCell(p) == CellType.WALL;
    }

    public boolean isTarget(Position p) {
        return isInside(p) && getCell(p) == CellType.TARGET;
    }

    public boolean isFree(Position p) {
        return isInside(p) && !isWall(p) && !hasBoxAt(p);
    }

    public Set<Position> getBoxesPositions() {
        Set<Position> positions = new HashSet<>();
        for (AbstractBox box : boxes) {
            positions.add(box.getPosition());
        }
        return positions;
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

    public int countTargetBoxes() {
        int count = 0;

        for (AbstractBox box : boxes) {
            if (box instanceof TargetBox) {
                count++;
            }
        }

        return count;
    }

    public int countBoxesOnTargets() {
        int count = 0;

        for (AbstractBox box : boxes) {
            if (box instanceof TargetBox && isTarget(box.getPosition())) {
                count++;
            }
        }

        return count;
    }

    public boolean isLocalVictory() {
        for (AbstractBox box : boxes) {
            if (box instanceof TargetBox && !isTarget(box.getPosition())) {
                return false;
            }
        }

        return true;
    }

    public boolean isRecursiveVictory() {
        if (!isLocalVictory()) {
            return false;
        }

        for (AbstractBox box : boxes) {
            if (box instanceof WorldBox worldBox) {
                World inner = worldBox.getInnerWorld();

                if (inner != null && !inner.isRecursiveVictory()) {
                    return false;
                }
            }
        }

        return true;
    }

    public World findWorldByName(char worldName) {
        if (this.name == worldName) {
            return this;
        }

        for (AbstractBox box : boxes) {
            if (box instanceof WorldBox worldBox && worldBox.getInnerWorld() != null) {
                World found = worldBox.getInnerWorld().findWorldByName(worldName);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    public World deepCopy() {
        World copy = new World(name, size, color);

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                copy.cells[r][c] = this.cells[r][c];
            }
        }

        if (playerPosition != null) {
            copy.playerPosition = new Position(playerPosition.row(), playerPosition.col());
        } else {
            copy.playerPosition = null;
        }

        for (AbstractBox box : boxes) {
            Position copiedPos = new Position(
                    box.getPosition().row(),
                    box.getPosition().col()
            );

            if (box instanceof TargetBox) {
                copy.addBox(new TargetBox(copiedPos));
            } else if (box instanceof WorldBox worldBox) {
                World innerCopy = worldBox.getInnerWorld() == null
                        ? null
                        : worldBox.getInnerWorld().deepCopy();

                WorldBox copiedBox = new WorldBox(copiedPos, innerCopy);
                copy.addBox(copiedBox);
            }
        }

        return copy;
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();

        sb.append("WORLD=").append(name).append(";");

        if (playerPosition != null) {
            sb.append("P=")
                    .append(playerPosition.row())
                    .append(",")
                    .append(playerPosition.col())
                    .append(";");
        } else {
            sb.append("P=null;");
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (cells[r][c] == CellType.WALL) {
                    sb.append("W(").append(r).append(",").append(c).append(");");
                } else if (cells[r][c] == CellType.TARGET) {
                    sb.append("T(").append(r).append(",").append(c).append(");");
                }
            }
        }

        for (AbstractBox box : boxes) {
            if (box instanceof TargetBox) {
                sb.append("B(")
                        .append(box.getPosition().row())
                        .append(",")
                        .append(box.getPosition().col())
                        .append(");");
            } else if (box instanceof WorldBox worldBox) {
                sb.append("WB(")
                        .append(box.getPosition().row())
                        .append(",")
                        .append(box.getPosition().col())
                        .append(")->");

                if (worldBox.getInnerWorld() != null) {
                    sb.append("{")
                            .append(worldBox.getInnerWorld().serialize())
                            .append("}");
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return serialize();
    }
}