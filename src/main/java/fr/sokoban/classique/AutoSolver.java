package fr.sokoban.classique;

import java.util.*;

public class AutoSolver {

    private static final int MAX_VISITED = 3_000_000;
    private static List<Position> targets;
    private static Set<Position> aliveSquares;

    public static List<Direction> solve(World world) {
        targets = getTargets(world);
        aliveSquares = computeAliveSquares(world, targets);

        SolverState initial = SolverState.fromWorld(world);

        PriorityQueue<SolverNode> open = new PriorityQueue<>(
                Comparator.comparingInt(n -> n.priority)
        );

        Map<SolverState, Integer> bestCost = new HashMap<>();

        int h = heuristic(initial, world);
        open.add(new SolverNode(initial, new ArrayList<>(), 0, h));
        bestCost.put(initial, 0);

        int visited = 0;

        while (!open.isEmpty()) {
            SolverNode node = open.poll();

            visited++;
            if (visited > MAX_VISITED) {
                System.out.println("Solveur arrêté : trop d'états explorés.");
                return null;
            }

            if (node.state.isGoal(world)) {
                return node.path;
            }

            List<PushCandidate> pushes = generatePushes(world, node.state);

            for (PushCandidate push : pushes) {
                SolverState next = node.state.pushBox(push.boxPos, push.direction);

                if (next == null) continue;

                if (DeadlockDetector.isDeadlock(next, world, aliveSquares)) {
                    continue;
                }

                int newCost = node.cost + push.pathToStand.size() + 1;

                if (bestCost.containsKey(next) && bestCost.get(next) <= newCost) {
                    continue;
                }

                bestCost.put(next, newCost);

                List<Direction> newPath = new ArrayList<>(node.path);
                newPath.addAll(push.pathToStand);
                newPath.add(push.direction);

                int priority = newCost + heuristic(next, world) * 10;

                open.add(new SolverNode(next, newPath, newCost, priority));
            }
        }

        return null;
    }

    private static List<PushCandidate> generatePushes(World world, SolverState state) {
        List<PushCandidate> result = new ArrayList<>();

        for (Position boxPos : state.getBoxes()) {
            for (Direction dir : Direction.values()) {
                Position stand = boxPos.move(dir.opposite());
                Position destination = boxPos.move(dir);

                if (!state.isFreeCell(world, stand)) continue;
                if (!state.isFreeCell(world, destination)) continue;

                if (!world.isTarget(destination) && !aliveSquares.contains(destination)) {
                    continue;
                }

                List<Direction> pathToStand = SolverPathFinder.findPath(
                        world,
                        state.getPlayer(),
                        stand,
                        state.getBoxes()
                );

                if (pathToStand == null) continue;

                result.add(new PushCandidate(boxPos, dir, pathToStand));
            }
        }

        result.sort(Comparator.comparingInt(p ->
                distanceToNearestTarget(p.boxPos.move(p.direction))
        ));

        return result;
    }

    private static int heuristic(SolverState state, World world) {
        List<Position> boxes = new ArrayList<>(state.getBoxes());

        if (boxes.size() > targets.size()) {
            return 1_000_000;
        }

        boolean[] usedTargets = new boolean[targets.size()];
        return matchingDistance(boxes, 0, usedTargets);
    }

    private static int matchingDistance(List<Position> boxes, int index, boolean[] usedTargets) {
        if (index == boxes.size()) {
            return 0;
        }

        Position box = boxes.get(index);
        int best = Integer.MAX_VALUE / 4;

        for (int i = 0; i < targets.size(); i++) {
            if (usedTargets[i]) continue;

            Position target = targets.get(i);

            usedTargets[i] = true;

            int distance = manhattan(box, target);
            int total = distance + matchingDistance(boxes, index + 1, usedTargets);

            best = Math.min(best, total);

            usedTargets[i] = false;
        }

        return best;
    }

    private static int distanceToNearestTarget(Position box) {
        int best = Integer.MAX_VALUE;

        for (Position target : targets) {
            best = Math.min(best, manhattan(box, target));
        }

        return best;
    }

    private static int manhattan(Position a, Position b) {
        return Math.abs(a.row() - b.row()) + Math.abs(a.col() - b.col());
    }

    private static List<Position> getTargets(World world) {
        List<Position> result = new ArrayList<>();

        for (int r = 0; r < world.getSize(); r++) {
            for (int c = 0; c < world.getSize(); c++) {
                Position p = new Position(r, c);

                if (world.isTarget(p)) {
                    result.add(p);
                }
            }
        }

        return result;
    }

    private static Set<Position> computeAliveSquares(World world, List<Position> targets) {
        Set<Position> alive = new HashSet<>();
        Queue<Position> queue = new LinkedList<>();

        for (Position target : targets) {
            alive.add(target);
            queue.add(target);
        }

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            for (Direction dir : Direction.values()) {
                Position previousBox = current.move(dir.opposite());
                Position playerStand = previousBox.move(dir.opposite());

                if (!world.isInside(previousBox) || world.isWall(previousBox)) continue;
                if (!world.isInside(playerStand) || world.isWall(playerStand)) continue;

                if (alive.contains(previousBox)) continue;

                alive.add(previousBox);
                queue.add(previousBox);
            }
        }

        return alive;
    }

    private static class PushCandidate {
        Position boxPos;
        Direction direction;
        List<Direction> pathToStand;

        PushCandidate(Position boxPos, Direction direction, List<Direction> pathToStand) {
            this.boxPos = boxPos;
            this.direction = direction;
            this.pathToStand = pathToStand;
        }
    }
}