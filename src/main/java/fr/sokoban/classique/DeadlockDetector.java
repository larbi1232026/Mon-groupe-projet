package fr.sokoban.classique;

import java.util.Set;

public class DeadlockDetector {

    public static boolean isDeadlock(SolverState state, World world, Set<Position> aliveSquares) {
        for (Position box : state.getBoxes()) {
            if (world.isTarget(box)) {
                continue;
            }

            if (!aliveSquares.contains(box)) {
                return true;
            }

            if (isCornerDeadlock(world, state, box)) {
                return true;
            }

            if (isSquareDeadlock(world, state, box)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isCornerDeadlock(World world, SolverState state, Position box) {
        boolean up = isWallOnly(world, box.move(Direction.UP));
        boolean down = isWallOnly(world, box.move(Direction.DOWN));
        boolean left = isWallOnly(world, box.move(Direction.LEFT));
        boolean right = isWallOnly(world, box.move(Direction.RIGHT));

        return (up && left)
                || (up && right)
                || (down && left)
                || (down && right);
    }

    private static boolean isSquareDeadlock(World world, SolverState state, Position box) {
        Position right = box.move(Direction.RIGHT);
        Position down = box.move(Direction.DOWN);
        Position diag = down.move(Direction.RIGHT);

        if (isBlocked(world, state, right)
                && isBlocked(world, state, down)
                && isBlocked(world, state, diag)) {
            return true;
        }

        Position left = box.move(Direction.LEFT);
        diag = down.move(Direction.LEFT);

        if (isBlocked(world, state, left)
                && isBlocked(world, state, down)
                && isBlocked(world, state, diag)) {
            return true;
        }

        Position up = box.move(Direction.UP);
        diag = up.move(Direction.RIGHT);

        if (isBlocked(world, state, right)
                && isBlocked(world, state, up)
                && isBlocked(world, state, diag)) {
            return true;
        }

        diag = up.move(Direction.LEFT);

        return isBlocked(world, state, left)
                && isBlocked(world, state, up)
                && isBlocked(world, state, diag);
    }

    private static boolean isBlocked(World world, SolverState state, Position p) {
        return !world.isInside(p)
                || world.isWall(p)
                || state.getBoxes().contains(p);
    }

    private static boolean isWallOnly(World world, Position p) {
        return !world.isInside(p) || world.isWall(p);
    }
}
