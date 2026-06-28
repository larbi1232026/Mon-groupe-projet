package fr.sokoban.classique;

import java.util.*;

public class SolverPathFinder {

    public static List<Direction> findPath(
            World world,
            Position start,
            Position goal,
            Set<Position> boxes
    ) {
        if (start.equals(goal)) {
            return new ArrayList<>();
        }

        Queue<Position> queue = new LinkedList<>();
        Map<Position, Position> parent = new HashMap<>();
        Map<Position, Direction> usedMove = new HashMap<>();
        Set<Position> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            if (current.equals(goal)) {
                return rebuildPath(start, goal, parent, usedMove);
            }

            for (Direction dir : Direction.values()) {
                Position next = current.move(dir);

                if (!world.isInside(next)) continue;
                if (world.isWall(next)) continue;
                if (boxes.contains(next)) continue;
                if (visited.contains(next)) continue;

                visited.add(next);
                parent.put(next, current);
                usedMove.put(next, dir);
                queue.add(next);
            }
        }

        return null;
    }

    private static List<Direction> rebuildPath(
            Position start,
            Position goal,
            Map<Position, Position> parent,
            Map<Position, Direction> usedMove
    ) {
        LinkedList<Direction> path = new LinkedList<>();
        Position current = goal;

        while (!current.equals(start)) {
            Direction dir = usedMove.get(current);
            path.addFirst(dir);
            current = parent.get(current);
        }

        return path;
    }
}
