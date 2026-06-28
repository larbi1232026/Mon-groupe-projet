package fr.sokoban.recursif;

public class TargetBox extends AbstractBox {

    public TargetBox(Position position) {
        super(position);
    }

    @Override
    public AbstractBox deepCopy() {
        return new TargetBox(new Position(getPosition().row(), getPosition().col()));
    }
}
