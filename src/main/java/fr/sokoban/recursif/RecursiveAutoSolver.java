package fr.sokoban.recursif;

import java.util.*;

public class RecursiveAutoSolver {

    private static final int MAX_LOCAL_VISITED = 500_000;

    public static List<RecursiveMove> solve(World rootWorld, World currentWorld) {
        World root = rootWorld.deepCopy();
        World current = root.findWorldByName(currentWorld.getName());

        if (current == null) {
            current = root;
        }

        List<RecursiveMove> solution = new ArrayList<>();

        if (!solveWorld(current, solution)) {
            return null;
        }

        while (current.getParentWorld() != null) {
            if (!exitWorld(current, solution)) {
                return null;
            }

            current = current.getParentWorld();

            if (!solveWorld(current, solution)) {
                return null;
            }
        }

        return root.isRecursiveVictory() ? solution : null;
    }

    private static boolean solveWorld(World world, List<RecursiveMove> solution) {
        List<AbstractBox> boxes = new ArrayList<>(world.getBoxes());

        for (AbstractBox box : boxes) {
            if (box instanceof WorldBox worldBox) {
                World inner = worldBox.getInnerWorld();

                if (inner != null && !inner.isRecursiveVictory()) {
                    if (!goToWorldBox(world, worldBox, solution)) {
                        return false;
                    }

                    if (!solveWorld(inner, solution)) {
                        return false;
                    }

                    if (!exitWorld(inner, solution)) {
                        return false;
                    }
                }
            }
        }

        List<RecursiveMove> local = solveLocal(world);

        if (local == null) {
            return false;
        }

        for (RecursiveMove move : local) {
            if (!applyMove(world, move)) {
                return false;
            }

            solution.add(move);
        }

        return world.isLocalVictory();
    }

    private static boolean goToWorldBox(World world, WorldBox worldBox, List<RecursiveMove> solution) {
        Map<Position, List<Direction>> reachable = computeReachable(world);

        Position boxPos = worldBox.getPosition();

        for (Direction dir : Direction.values()) {
            Position stand = boxPos.move(dir.opposite());

            if (reachable.containsKey(stand)) {
                List<Direction> path = reachable.get(stand);

                for (Direction d : path) {
                    RecursiveMove move = RecursiveMove.move(d);
                    applyMove(world, move);
                    solution.add(move);
                }

                RecursiveMove enter = RecursiveMove.enter(dir);
                applyEnter(world, dir);
                solution.add(enter);

                return true;
            }
        }

        return false;
    }

    private static boolean exitWorld(World inner, List<RecursiveMove> solution) {
        World parent = inner.getParentWorld();

        if (parent == null || inner.getContainerBox() == null) {
            return false;
        }

        Position boxPos = inner.getContainerBox().getPosition();

        for (Direction dir : Direction.values()) {
            Position p = boxPos.move(dir);

            if (parent.isFree(p)) {
                parent.setPlayerPosition(p);
                solution.add(RecursiveMove.exit());
                return true;
            }
        }

        return false;
    }

    private static List<RecursiveMove> solveLocal(World world) {
        if (world.isLocalVictory()) {
            return new ArrayList<>();
        }

        LocalState start = LocalState.fromWorld(world);

        Queue<LocalNode> queue = new LinkedList<>();
        Set<LocalState> visited = new HashSet<>();

        queue.add(new LocalNode(start, new ArrayList<>()));
        visited.add(start);

        while (!queue.isEmpty()) {
            LocalNode node = queue.poll();

            if (visited.size() > MAX_LOCAL_VISITED) {
                return null;
            }

            if (isGoal(world, node.state)) {
                return node.path;
            }

            Map<Position, List<Direction>> reachable = computeReachable(world, node.state);

            for (int i = 0; i < node.state.boxes.size(); i++) {
                Position boxPos = node.state.boxes.get(i);

                for (Direction dir : Direction.values()) {
                    Position stand = boxPos.move(dir.opposite());
                    Position dest = boxPos.move(dir);

                    if (!reachable.containsKey(stand)) {
                        continue;
                    }

                    if (!isFree(world, node.state, dest)) {
                        continue;
                    }

                    List<Position> newBoxes = new ArrayList<>(node.state.boxes);
                    newBoxes.set(i, dest);

                    LocalState next = new LocalState(boxPos, newBoxes);

                    if (visited.contains(next)) {
                        continue;
                    }

                    visited.add(next);

                    List<RecursiveMove> newPath = new ArrayList<>(node.path);

                    for (Direction d : reachable.get(stand)) {
                        newPath.add(RecursiveMove.move(d));
                    }

                    newPath.add(RecursiveMove.move(dir));

                    queue.add(new LocalNode(next, newPath));
                }
            }
        }

        return null;
    }

    private static boolean isGoal(World world, LocalState state) {
        for (Position box : state.boxes) {
            if (!world.isTarget(box)) {
                return false;
            }
        }

        return true;
    }

    private static Map<Position, List<Direction>> computeReachable(World world) {
        return computeReachable(world, LocalState.fromWorld(world));
    }

    private static Map<Position, List<Direction>> computeReachable(World world, LocalState state) {
        Map<Position, List<Direction>> map = new HashMap<>();
        Queue<Position> queue = new LinkedList<>();

        if (state.player == null) {
            return map;
        }

        queue.add(state.player);
        map.put(state.player, new ArrayList<>());

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            List<Direction> path = map.get(current);

            for (Direction dir : Direction.values()) {
                Position next = current.move(dir);

                if (!isFree(world, state, next)) {
                    continue;
                }

                if (map.containsKey(next)) {
                    continue;
                }

                List<Direction> newPath = new ArrayList<>(path);
                newPath.add(dir);

                map.put(next, newPath);
                queue.add(next);
            }
        }

        return map;
    }

    private static boolean isFree(World world, LocalState state, Position pos) {
        if (!world.isInside(pos) || world.isWall(pos)) {
            return false;
        }

        for (Position box : state.boxes) {
            if (box.equals(pos)) {
                return false;
            }
        }

        for (AbstractBox box : world.getBoxes()) {
            if (box instanceof WorldBox && box.getPosition().equals(pos)) {
                return false;
            }
        }

        return true;
    }

    private static boolean applyMove(World world, RecursiveMove move) {
        if (move.getType() == RecursiveMove.Type.MOVE) {
            Direction dir = move.getDirection();

            Position player = world.getPlayerPosition();
            Position next = player.move(dir);

            if (!world.isInside(next) || world.isWall(next)) {
                return false;
            }

            AbstractBox box = world.getBoxAt(next);

            if (box == null) {
                world.setPlayerPosition(next);
                return true;
            }

            if (box instanceof WorldBox) {
                return false;
            }

            Position after = next.move(dir);

            if (!world.isInside(after)
                    || world.isWall(after)
                    || world.hasBoxAt(after)) {
                return false;
            }

            box.setPosition(after);
            world.setPlayerPosition(next);
            return true;
        }

        if (move.getType() == RecursiveMove.Type.ENTER) {
            return applyEnter(world, move.getDirection());
        }

        return false;
    }

    private static boolean applyEnter(World world, Direction dir) {
        Position next = world.getPlayerPosition().move(dir);
        AbstractBox box = world.getBoxAt(next);

        if (!(box instanceof WorldBox worldBox)) {
            return false;
        }

        World inner = worldBox.getInnerWorld();

        if (inner == null) {
            return false;
        }

        Position entry = findEntry(inner, dir);
        inner.setPlayerPosition(entry);

        return true;
    }

    private static Position findEntry(World world, Direction dir) {
        int size = world.getSize();

        if (dir == Direction.UP) {
            for (int c = 1; c < size - 1; c++) {
                Position p = new Position(size - 2, c);
                if (world.isFree(p)) return p;
            }
        }

        if (dir == Direction.DOWN) {
            for (int c = 1; c < size - 1; c++) {
                Position p = new Position(1, c);
                if (world.isFree(p)) return p;
            }
        }

        if (dir == Direction.LEFT) {
            for (int r = 1; r < size - 1; r++) {
                Position p = new Position(r, size - 2);
                if (world.isFree(p)) return p;
            }
        }

        if (dir == Direction.RIGHT) {
            for (int r = 1; r < size - 1; r++) {
                Position p = new Position(r, 1);
                if (world.isFree(p)) return p;
            }
        }

        for (int r = 1; r < size - 1; r++) {
            for (int c = 1; c < size - 1; c++) {
                Position p = new Position(r, c);
                if (world.isFree(p)) return p;
            }
        }

        return new Position(1, 1);
    }

    private static class LocalState {
        Position player;
        List<Position> boxes;

        LocalState(Position player, List<Position> boxes) {
            this.player = player;
            this.boxes = new ArrayList<>(boxes);
            this.boxes.sort(
                    Comparator.comparingInt(Position::row)
                            .thenComparingInt(Position::col)
            );
        }

        static LocalState fromWorld(World world) {
            List<Position> boxes = new ArrayList<>();

            for (AbstractBox box : world.getBoxes()) {
                if (box instanceof TargetBox) {
                    boxes.add(box.getPosition());
                }
            }

            return new LocalState(world.getPlayerPosition(), boxes);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof LocalState other)) {
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

    private static class LocalNode {
        LocalState state;
        List<RecursiveMove> path;

        LocalNode(LocalState state, List<RecursiveMove> path) {
            this.state = state;
            this.path = path;
        }
    }
}