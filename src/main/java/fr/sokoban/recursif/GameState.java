package fr.sokoban.recursif;

import java.util.Stack;

public class GameState {
    private World rootWorld;
    private World currentWorld;
    private final Stack<WorldSnapshot> history;

    public GameState(World rootWorld) {
        this.rootWorld = rootWorld;
        this.currentWorld = rootWorld;
        this.history = new Stack<>();
    }

    public World getRootWorld() {
        return rootWorld;
    }

    public World getCurrentWorld() {
        return currentWorld;
    }

    public void setCurrentWorld(World currentWorld) {
        this.currentWorld = currentWorld;
    }

    public void save() {
        history.push(new WorldSnapshot(rootWorld.deepCopy(), currentWorld.getName()));
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public void undo() {
        if (!history.isEmpty()) {
            WorldSnapshot snapshot = history.pop();

            this.rootWorld = snapshot.root;
            World restoredCurrent = rootWorld.findWorldByName(snapshot.currentWorldName);

            this.currentWorld = restoredCurrent != null ? restoredCurrent : rootWorld;
        }
    }

    private static class WorldSnapshot {
        World root;
        char currentWorldName;

        WorldSnapshot(World root, char currentWorldName) {
            this.root = root;
            this.currentWorldName = currentWorldName;
        }
    }
}