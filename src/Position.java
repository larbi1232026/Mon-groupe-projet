public class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { 
        return x; 
    }

    public int getY() { 
        return y; 
    }

    public Position move(Direction direction) {
        switch (direction) {
            case UP: return new Position(x - 1, y);
            case DOWN: return new Position(x + 1, y);
            case LEFT: return new Position(x, y - 1);
            case RIGHT: return new Position(x, y + 1);
        }
        return this;
    }
}
