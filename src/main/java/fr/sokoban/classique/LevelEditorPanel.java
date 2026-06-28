package fr.sokoban.classique;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class LevelEditorPanel extends JPanel {

    private static final int CELL_SIZE = 50;

    private final Map<Character, World> worlds;
    private char currentWorldName;
    private EditorTool currentTool;

    public LevelEditorPanel(Map<Character, World> worlds, char startWorld) {
        this.worlds = worlds;
        this.currentWorldName = startWorld;
        this.currentTool = EditorTool.WALL;

        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    public char getCurrentWorldName() {
        return currentWorldName;
    }

    public void setCurrentWorldName(char currentWorldName) {
        this.currentWorldName = currentWorldName;
        repaint();
    }

    public void setCurrentTool(EditorTool currentTool) {
        this.currentTool = currentTool;
    }

    private void handleClick(int mouseX, int mouseY) {
        World world = worlds.get(currentWorldName);
        if (world == null) return;

        int row = mouseY / CELL_SIZE;
        int col = mouseX / CELL_SIZE;
        Position pos = new Position(row, col);

        if (!world.isInside(pos)) return;

        applyTool(world, pos);
        repaint();
    }

    private void applyTool(World world, Position pos) {
        switch (currentTool) {
            case WALL:
                world.setCell(pos, CellType.WALL);
                world.removeBoxAt(pos);
                if (pos.equals(world.getPlayerPosition())) {
                    world.setPlayerPosition(null);
                }
                break;

            case BOX:
                if (!world.isWall(pos) && !pos.equals(world.getPlayerPosition())) {
                    world.removeBoxAt(pos);
                    world.addBox(new Box(pos));
                }
                break;

            case PLAYER:
                if (!world.isWall(pos) && !world.hasBoxAt(pos)) {
                    world.setPlayerPosition(pos);
                }
                break;

            case TARGET:
                if (!world.isWall(pos)) {
                    world.setCell(pos, CellType.TARGET);
                }
                break;

            case ERASE:
            case FLOOR:
                world.setCell(pos, CellType.EMPTY);
                world.removeBoxAt(pos);
                if (pos.equals(world.getPlayerPosition())) {
                    world.setPlayerPosition(null);
                }
                break;
        }
    }

    public int getRows() {
        return worlds.get(currentWorldName).getSize();
    }

    public int getCols() {
        return worlds.get(currentWorldName).getSize();
    }

    public char getCellChar(int row, int col) {
        World world = worlds.get(currentWorldName);
        Position pos = new Position(row, col);

        boolean player = world.getPlayerPosition() != null && world.getPlayerPosition().equals(pos);
        boolean box = world.hasBoxAt(pos);
        boolean target = world.isTarget(pos);

        if (player && target) return '+';
        if (box && target) return '*';
        if (player) return '@';
        if (box) return '$';
        if (target) return '.';
        if (world.isWall(pos)) return '#';
        return ' ';
    }

    public void setCellChar(int row, int col, char c) {
        World world = worlds.get(currentWorldName);
        Position pos = new Position(row, col);

        switch (c) {
            case '#':
                world.setCell(pos, CellType.WALL);
                world.removeBoxAt(pos);
                if (pos.equals(world.getPlayerPosition())) {
                    world.setPlayerPosition(null);
                }
                break;

            case '@':
                world.setCell(pos, CellType.EMPTY);
                world.removeBoxAt(pos);
                world.setPlayerPosition(pos);
                break;

            case '$':
                world.setCell(pos, CellType.EMPTY);
                world.removeBoxAt(pos);
                world.addBox(new Box(pos));
                break;

            case '.':
                world.setCell(pos, CellType.TARGET);
                world.removeBoxAt(pos);
                break;

            case '*':
                world.setCell(pos, CellType.TARGET);
                world.removeBoxAt(pos);
                world.addBox(new Box(pos));
                break;

            case '+':
                world.setCell(pos, CellType.TARGET);
                world.removeBoxAt(pos);
                world.setPlayerPosition(pos);
                break;

            default:
                world.setCell(pos, CellType.EMPTY);
                world.removeBoxAt(pos);
                if (pos.equals(world.getPlayerPosition())) {
                    world.setPlayerPosition(null);
                }
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        World world = worlds.get(currentWorldName);
        if (world == null) return;

        for (int row = 0; row < world.getSize(); row++) {
            for (int col = 0; col < world.getSize(); col++) {
                Position pos = new Position(row, col);
                int x = col * CELL_SIZE;
                int y = row * CELL_SIZE;
                drawCell(g, world, pos, x, y);
            }
        }
    }

    private void drawCell(Graphics g, World world, Position pos, int x, int y) {
        if (world.isWall(pos)) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
        }

        if (world.isTarget(pos)) {
            g.setColor(Color.YELLOW);
            g.fillOval(x + 15, y + 15, 20, 20);
        }

        if (world.hasBoxAt(pos)) {
            g.setColor(Color.ORANGE);
            g.fillRect(x + 10, y + 10, 30, 30);
        }

        if (world.getPlayerPosition() != null && world.getPlayerPosition().equals(pos)) {
            g.setColor(Color.BLUE);
            g.fillOval(x + 10, y + 10, 30, 30);
        }

        g.setColor(Color.BLACK);
        g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
    }
}