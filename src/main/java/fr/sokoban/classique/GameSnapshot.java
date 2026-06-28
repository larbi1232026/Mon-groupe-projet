package fr.sokoban.classique;

public class GameSnapshot {
    private final World world;

    public GameSnapshot(World world) {
        this.world = world.deepCopy();
    }

    public World getWorld() {
        return world.deepCopy();
    }
}
