public abstract class AbstractBox {

    protected Position position;

    public AbstractBox(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void move(Direction direction) {
        position = position.move(direction);
    }
}
