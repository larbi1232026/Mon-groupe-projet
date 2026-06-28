package fr.sokoban.classique;

public class Box {
    private Position position;

    public Box(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Box copy() {
        return new Box(new Position(position.row(), position.col()));
    }
}
