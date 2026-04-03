public class World {

    private CellType[][] grid;

    public World(CellType[][] grid) {
        this.grid = grid;
    }

    public CellType getCell(Position pos) {
        return grid[pos.getX()][pos.getY()];
    }

    public boolean isFree(Position pos) {
        int x = pos.getX();
        int y = pos.getY();

        if (x < 0 || x >= grid.length || y < 0 || y >= grid[0].length) {
            return false;
        }

        return grid[x][y] != CellType.WALL;
    }
}
