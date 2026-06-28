package fr.sokoban.classique;

import java.util.List;

public class GameModel {
    private final GameState state;
    private int moveCount;

    public GameModel(GameState state) {
        this.state = state;
        this.moveCount = 0;
    }

    public GameState getState() {
        return state;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void undo() {
        if (state.canUndo()) {
            state.undo();
            if (moveCount > 0) {
                moveCount--;
            }
        }
    }

    public boolean move(Direction dir) {
        if (isWin()) {
            return false;
        }

        World world = state.getCurrentWorld();
        Position player = world.getPlayerPosition();
        Position next = player.move(dir);

        if (!world.isInside(next) || world.isWall(next)) {
            return false;
        }

        Box box = world.getBoxAt(next);

        if (box == null) {
            state.save();
            world.setPlayerPosition(next);
            moveCount++;
            return true;
        }

        Position afterBox = next.move(dir);

        if (!world.isInside(afterBox) || world.isWall(afterBox) || world.hasBoxAt(afterBox)) {
            return false;
        }

        state.save();
        box.setPosition(afterBox);
        world.setPlayerPosition(next);
        moveCount++;
        return true;
    }

    public boolean isWin() {
        return state.getCurrentWorld().isVictory();
    }

    public boolean isLose() {
        World world = state.getCurrentWorld();

        if (isWin()) {
            return false;
        }

        if (world.getBoxes().isEmpty()) {
            return false;
        }

        for (Box box : world.getBoxes()) {
            Position p = box.getPosition();

            if (world.isTarget(p)) {
                continue;
            }

            boolean up = isBlocked(world, p.translate(-1, 0));
            boolean down = isBlocked(world, p.translate(1, 0));
            boolean left = isBlocked(world, p.translate(0, -1));
            boolean right = isBlocked(world, p.translate(0, 1));

            boolean blockedInCorner =
                    (up && left) ||
                            (up && right) ||
                            (down && left) ||
                            (down && right);

            if (blockedInCorner) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlocked(World world, Position p) {
        return !world.isInside(p) || world.isWall(p) || world.hasBoxAt(p);
    }

    public List<Direction> solveAutomatically() {
        return AutoSolver.solve(state.getCurrentWorld());
    }

    public void applySolution(List<Direction> solution) {
        if (solution == null || solution.isEmpty()) {
            return;
        }

        for (Direction dir : solution) {
            if (isWin()) {
                break;
            }
            move(dir);
        }
    }

    public void resetMoveCount() {
        moveCount = 0;
    }
}