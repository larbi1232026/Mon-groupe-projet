package fr.sokoban.recursif;

public class RecursiveMove {

    public enum Type {
        MOVE,
        ENTER,
        EXIT
    }

    private final Type type;
    private final Direction direction;

    private RecursiveMove(Type type, Direction direction) {
        this.type = type;
        this.direction = direction;
    }

    public static RecursiveMove move(Direction direction) {
        return new RecursiveMove(Type.MOVE, direction);
    }

    public static RecursiveMove enter(Direction direction) {
        return new RecursiveMove(Type.ENTER, direction);
    }

    public static RecursiveMove exit() {
        return new RecursiveMove(Type.EXIT, null);
    }

    public Type getType() {
        return type;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return switch (type) {
            case MOVE -> "MOVE " + direction;
            case ENTER -> "ENTER " + direction;
            case EXIT -> "EXIT";
        };
    }
}
