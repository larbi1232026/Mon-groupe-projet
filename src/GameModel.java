public class GameModel {

    private World world;
    private Position playerPosition;

    public GameModel(World world, Position playerPosition) {
        this.world = world;
        this.playerPosition = playerPosition;
    }

    public Position getPlayerPosition() {
        return playerPosition;
    }

    public void move(Direction direction) {
        Position newPos = playerPosition.move(direction);

        // Vérifie si la case est libre
        if (world.isFree(newPos)) {
            playerPosition = newPos;
        }
    }
}
