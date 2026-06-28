package fr.sokoban.recursif;

public abstract class AbstractBox {
    private Position position;

    public AbstractBox(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public abstract AbstractBox deepCopy();
}
