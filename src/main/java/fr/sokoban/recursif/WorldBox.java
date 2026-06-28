package fr.sokoban.recursif;

public class WorldBox extends AbstractBox {
    private final World innerWorld;

    public WorldBox(Position position, World innerWorld) {
        super(position);
        this.innerWorld = innerWorld;
    }

    public World getInnerWorld() {
        return innerWorld;
    }

    @Override
    public WorldBox deepCopy() {
        return new WorldBox(
                new Position(getPosition().row(), getPosition().col()),
                innerWorld == null ? null : innerWorld.deepCopy()
        );
    }
}