package fr.sokoban.classique;

import java.util.Stack;

public class GameState {
    private World currentWorld;
    private final Stack<GameSnapshot> history;

    public GameState(World currentWorld) {
        this.currentWorld = currentWorld;
        this.history = new Stack<>();
    }

    public World getCurrentWorld() {
        return currentWorld;
    }

    public void save() {
        history.push(new GameSnapshot(currentWorld));
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public void undo() {
        if (!history.isEmpty()) {
            currentWorld = history.pop().getWorld();
        }
    }
}