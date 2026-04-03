import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LevelEditorPanel extends JPanel {

    private static final int CELL_SIZE = 40;
    private static final int ROWS = 10;
    private static final int COLS = 10;

    private EditorCellType[][] grid;
    private EditorTool currentTool;

    public LevelEditorPanel() {
        grid = new EditorCellType[ROWS][COLS];
        currentTool = EditorTool.WALL;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                grid[i][j] = EditorCellType.EMPTY;
            }
        }

        setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / CELL_SIZE;
                int row = e.getY() / CELL_SIZE;

                if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                    applyTool(row, col);
                    repaint();
                }
            }
        });
    }

    public void setCurrentTool(EditorTool tool) {
        this.currentTool = tool;
    }

    private void applyTool(int row, int col) {
        switch (currentTool) {
            case EMPTY:
                grid[row][col] = EditorCellType.EMPTY;
                break;
            case WALL:
                grid[row][col] = EditorCellType.WALL;
                break;
            case PLAYER:
                removeExistingPlayer();
                grid[row][col] = EditorCellType.PLAYER;
                break;
            case BOX:
                grid[row][col] = EditorCellType.BOX;
                break;
            case TARGET:
                grid[row][col] = EditorCellType.TARGET;
                break;
            case WORLD_BOX:
                grid[row][col] = EditorCellType.WORLD_BOX;
                break;
        }
    }

    private void removeExistingPlayer() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (grid[i][j] == EditorCellType.PLAYER) {
                    grid[i][j] = EditorCellType.EMPTY;
                }
            }
        }
    }

    public EditorCellType[][] getGrid() {
        return grid;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                drawCell(g, row, col);
            }
        }
    }

    private void drawCell(Graphics g, int row, int col) {
        int x = col * CELL_SIZE;
        int y = row * CELL_SIZE;

        switch (grid[row][col]) {
            case EMPTY:
                g.setColor(Color.WHITE);
                break;
            case WALL:
                g.setColor(Color.DARK_GRAY);
                break;
            case PLAYER:
                g.setColor(Color.BLUE);
                break;
            case BOX:
                g.setColor(Color.ORANGE);
                break;
            case TARGET:
                g.setColor(Color.RED);
                break;
            case WORLD_BOX:
                g.setColor(Color.MAGENTA);
                break;
        }

        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
    }
}
