package fr.sokoban.classique;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SolverState {
    private final Position player;
    private final Set<Position> boxes;

    public SolverState(Position player, Set<Position> boxes) {
        this.player = player;
        this.boxes = new HashSet<>(boxes);
    }

    public static SolverState fromWorld(World world) {
        return new SolverState(
                world.getPlayerPosition(),
                world.getBoxesPositions()
        );
    }

    public Position getPlayer() {
        return player;
    }

    public Set<Position> getBoxes() {
        return new HashSet<>(boxes);
    }

    public boolean isGoal(World world) {
        if (boxes.isEmpty()) return false;

        for (Position box : boxes) {
            if (!world.isTarget(box)) {
                return false;
            }
        }

        return true;
    }

    public boolean isFreeCell(World world, Position p) {
        return world.isInside(p)
                && !world.isWall(p)
                && !boxes.contains(p);
    }

    public SolverState pushBox(Position boxPos, Direction dir) {
        if (!boxes.contains(boxPos)) {
            return null;
        }

        Position destination = boxPos.move(dir);

        if (boxes.contains(destination)) {
            return null;
        }

        Set<Position> newBoxes = new HashSet<>(boxes);
        newBoxes.remove(boxPos);
        newBoxes.add(destination);

        return new SolverState(boxPos, newBoxes);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SolverState other)) {
            return false;
        }

        return Objects.equals(player, other.player)
                && Objects.equals(boxes, other.boxes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, boxes);
    }
}
