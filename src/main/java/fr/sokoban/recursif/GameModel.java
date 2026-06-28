package fr.sokoban.recursif;

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

    public boolean move(Direction dir) {

        World world = state.getCurrentWorld();
        Position player = world.getPlayerPosition();
        Position next = player.move(dir);

        if (!world.isInside(next) || world.isWall(next)) {
            return false;
        }

        AbstractBox box = world.getBoxAt(next);

        // === déplacement simple
        if (box == null) {
            state.save();
            world.setPlayerPosition(next);
            moveCount++;
            return true;
        }

        // === bloc monde interdit
        if (box instanceof WorldBox) {
            return false;
        }

        // ❌ SUPPRIMÉ : blocage sur cible

        Position after = next.move(dir);

        if (!world.isInside(after)
                || world.isWall(after)
                || world.hasBoxAt(after)) {
            return false;
        }

        state.save();
        box.setPosition(after);
        world.setPlayerPosition(next);
        moveCount++;

        return true;
    }

    public boolean enterWorldBox(Direction dir) {

        World world = state.getCurrentWorld();
        Position player = world.getPlayerPosition();
        Position next = player.move(dir);

        AbstractBox box = world.getBoxAt(next);

        if (!(box instanceof WorldBox wb)) return false;

        World inner = wb.getInnerWorld();
        if (inner == null) return false;

        state.save();

        inner.setParentWorld(world);
        inner.setContainerBox(wb);

        Position entry = findEntry(inner, dir);
        inner.setPlayerPosition(entry);

        state.setCurrentWorld(inner);
        moveCount++;

        return true;
    }

    public boolean exitCurrentWorld() {

        World current = state.getCurrentWorld();

        if (current.getParentWorld() == null) return false;

        World parent = current.getParentWorld();
        Position boxPos = current.getContainerBox().getPosition();

        for (Direction d : Direction.values()) {
            Position p = boxPos.move(d);
            if (parent.isFree(p)) {
                state.save();
                parent.setPlayerPosition(p);
                state.setCurrentWorld(parent);
                moveCount++;
                return true;
            }
        }

        return false;
    }

    private Position findEntry(World w, Direction dir) {

        int size = w.getSize();

        if (dir == Direction.UP)
            for (int c = 1; c < size - 1; c++)
                if (w.isFree(new Position(size - 2, c)))
                    return new Position(size - 2, c);

        if (dir == Direction.DOWN)
            for (int c = 1; c < size - 1; c++)
                if (w.isFree(new Position(1, c)))
                    return new Position(1, c);

        if (dir == Direction.LEFT)
            for (int r = 1; r < size - 1; r++)
                if (w.isFree(new Position(r, size - 2)))
                    return new Position(r, size - 2);

        if (dir == Direction.RIGHT)
            for (int r = 1; r < size - 1; r++)
                if (w.isFree(new Position(r, 1)))
                    return new Position(r, 1);

        return new Position(1, 1);
    }
    public boolean isLose() {
        return false;
    }
    public boolean isWin() {
        return state.getRootWorld().isRecursiveVictory();
    }
    public void setMoveCount(int count) {
        this.moveCount = count;
    }
    public void undo() {
        if (state.canUndo()) {
            state.undo();
            if (moveCount > 0) {
                moveCount--;
            }
        }
    }

    public List<RecursiveMove> solveAutomatically() {
        return RecursiveAutoSolver.solve(
                state.getRootWorld(),
                state.getCurrentWorld()
        );
    }
}
