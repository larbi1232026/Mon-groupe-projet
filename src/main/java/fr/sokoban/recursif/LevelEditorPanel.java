package fr.sokoban.recursif;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LevelEditorPanel extends JPanel {
    private static final int CELL_SIZE = 60;
    private static final int ARC = 16;

    private static final Color BG_DARK = new Color(24, 31, 76);
    private static final Color BG_MID = new Color(56, 74, 160);

    private static final Color WALL_FRONT = new Color(47, 62, 123);
    private static final Color WALL_TOP = new Color(112, 150, 255);
    private static final Color WALL_SIDE = new Color(165, 202, 255);

    private static final Color PLAYER = new Color(222, 110, 210);
    private static final Color TARGET_BOX = new Color(194, 130, 59);
    private static final Color WORLD_BOX = new Color(130, 232, 118);
    private static final Color TARGET = new Color(255, 220, 95);

    private final World rootWorld;
    private World currentWorld;
    private EditorTool currentTool;
    private Runnable worldListChangedListener;

    private int wantedWorldCount = 1;

    public LevelEditorPanel(World rootWorld) {
        this.rootWorld = rootWorld;
        this.currentWorld = rootWorld;
        this.currentTool = EditorTool.WALL;

        setPreferredSize(new Dimension(1000, 850));
        setBackground(BG_DARK);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e.getX(), e.getY(), e.getButton());
            }
        });
    }

    public World getRootWorld() {
        return rootWorld;
    }

    public World getCurrentWorld() {
        return currentWorld;
    }

    public void setCurrentTool(EditorTool currentTool) {
        this.currentTool = currentTool;
        repaint();
    }

    public EditorTool getCurrentTool() {
        return currentTool;
    }

    public void setWorldListChangedListener(Runnable listener) {
        this.worldListChangedListener = listener;
    }

    public char getCurrentWorldName() {
        return currentWorld.getName();
    }

    public java.util.List<Character> getWorldNames() {
        java.util.List<Character> names = new java.util.ArrayList<>();
        collectWorldNames(rootWorld, names);
        return names;
    }

    private void collectWorldNames(World world, java.util.List<Character> names) {
        if (world == null || names.contains(world.getName())) {
            return;
        }

        names.add(world.getName());

        for (AbstractBox box : world.getBoxes()) {
            if (box instanceof WorldBox worldBox) {
                collectWorldNames(worldBox.getInnerWorld(), names);
            }
        }
    }

    public boolean selectWorld(char worldName) {
        World world = rootWorld.findWorldByName(worldName);
        if (world == null) {
            return false;
        }

        currentWorld = world;
        repaint();
        return true;
    }

    public void ensureRootWorldBoxes(int wantedCount) {
        if (wantedCount < 1) {
            wantedCount = 1;
        }

        this.wantedWorldCount = wantedCount;

        clearAllWorldBoxes(rootWorld);

        currentWorld = rootWorld;

        World parent = rootWorld;

        for (int i = 1; i < wantedCount; i++) {
            Position free = findFirstFreeCell(parent);

            if (free == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Impossible de créer tous les mondes : aucune case libre dans le monde " + parent.getName() + ".",
                        "Aucune case libre",
                        JOptionPane.WARNING_MESSAGE
                );
                break;
            }

            char newName = (char) ('A' + i);

            World innerWorld = new World(
                    newName,
                    parent.getSize(),
                    new Color(180, 120, 220)
            );

            buildBorderWalls(innerWorld);

            parent.addBox(new WorldBox(free, innerWorld));

            parent = innerWorld;
        }

        notifyWorldListChanged();
        repaint();
    }

    private void clearAllWorldBoxes(World world) {
        java.util.List<AbstractBox> toRemove = new java.util.ArrayList<>();

        for (AbstractBox box : world.getBoxes()) {
            if (box instanceof WorldBox worldBox) {
                clearAllWorldBoxes(worldBox.getInnerWorld());
                toRemove.add(box);
            }
        }

        for (AbstractBox box : toRemove) {
            world.removeBox(box);
        }
    }

    private void notifyWorldListChanged() {
        if (worldListChangedListener != null) {
            worldListChangedListener.run();
        }
    }

    private void handleClick(int mouseX, int mouseY, int button) {
        int boardSize = currentWorld.getSize() * CELL_SIZE;
        int startX = (getWidth() - boardSize) / 2;
        int startY = 90;

        int col = (mouseX - startX) / CELL_SIZE;
        int row = (mouseY - startY) / CELL_SIZE;

        Position pos = new Position(row, col);

        if (!currentWorld.isInside(pos)) {
            return;
        }

        if (button == MouseEvent.BUTTON3) {
            openWorldBoxAt(pos);
            return;
        }

        applyToolToCurrentWorld(pos);
        repaint();
    }

    private void applyToolToCurrentWorld(Position pos) {
        switch (currentTool) {
            case FLOOR -> {
                currentWorld.setCell(pos, CellType.EMPTY);

                AbstractBox box = currentWorld.getBoxAt(pos);
                if (box != null) {
                    currentWorld.removeBox(box);
                    notifyWorldListChanged();
                }
            }

            case WALL -> {
                currentWorld.setCell(pos, CellType.WALL);

                AbstractBox box = currentWorld.getBoxAt(pos);
                if (box != null) {
                    currentWorld.removeBox(box);
                    notifyWorldListChanged();
                }

                if (currentWorld.getPlayerPosition() != null
                        && currentWorld.getPlayerPosition().equals(pos)) {
                    movePlayerToFirstFreeCell(currentWorld);
                }
            }

            case TARGET -> {
                if (!currentWorld.isWall(pos)) {
                    currentWorld.setCell(pos, CellType.TARGET);
                }
            }

            case PLAYER -> {
                if (!currentWorld.isWall(pos) && !currentWorld.hasBoxAt(pos)) {
                    currentWorld.setPlayerPosition(pos);
                }
            }

            case TARGET_BOX -> {
                if (!currentWorld.isWall(pos)
                        && !pos.equals(currentWorld.getPlayerPosition())) {

                    AbstractBox existing = currentWorld.getBoxAt(pos);
                    if (existing != null) {
                        currentWorld.removeBox(existing);
                        notifyWorldListChanged();
                    }

                    currentWorld.addBox(new TargetBox(pos));
                }
            }

            case WORLD_BOX -> placeNextWorldBox(pos);

            case ERASE -> {
                currentWorld.setCell(pos, CellType.EMPTY);

                AbstractBox existing = currentWorld.getBoxAt(pos);
                if (existing != null) {
                    currentWorld.removeBox(existing);
                    notifyWorldListChanged();
                }

                if (currentWorld.getPlayerPosition() != null
                        && currentWorld.getPlayerPosition().equals(pos)) {
                    movePlayerToFirstFreeCell(currentWorld);
                }
            }
        }
    }

    private void placeNextWorldBox(Position pos) {
        if (currentWorld.isWall(pos) || pos.equals(currentWorld.getPlayerPosition())) {
            return;
        }

        if (hasWorldBox(currentWorld)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ce monde contient déjà une WorldBox.\nChaque monde ne doit contenir qu'une seule WorldBox vers le monde suivant.",
                    "WorldBox déjà présente",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        char currentName = currentWorld.getName();
        char nextName = (char) (currentName + 1);
        char lastAllowedName = (char) ('A' + wantedWorldCount - 1);

        if (nextName > lastAllowedName) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tu as déjà atteint le dernier monde prévu.",
                    "Dernier monde",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        if (rootWorld.findWorldByName(nextName) != null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Le monde " + nextName + " existe déjà.",
                    "Monde déjà créé",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        AbstractBox existing = currentWorld.getBoxAt(pos);
        if (existing != null) {
            currentWorld.removeBox(existing);
            notifyWorldListChanged();
        }

        World innerWorld = new World(
                nextName,
                currentWorld.getSize(),
                new Color(180, 120, 220)
        );

        buildBorderWalls(innerWorld);

        currentWorld.addBox(new WorldBox(pos, innerWorld));

        notifyWorldListChanged();
        repaint();
    }

    private boolean hasWorldBox(World world) {
        for (AbstractBox box : world.getBoxes()) {
            if (box instanceof WorldBox) {
                return true;
            }
        }
        return false;
    }

    private void openWorldBoxAt(Position pos) {
        AbstractBox box = currentWorld.getBoxAt(pos);

        if (box instanceof WorldBox worldBox && worldBox.getInnerWorld() != null) {
            currentWorld = worldBox.getInnerWorld();
            notifyWorldListChanged();
            repaint();
        }
    }

    public boolean goToParentWorld() {
        if (currentWorld.getParentWorld() == null) {
            return false;
        }

        currentWorld = currentWorld.getParentWorld();
        notifyWorldListChanged();
        repaint();
        return true;
    }

    private Position findFirstFreeCell(World world) {
        for (int r = 1; r < world.getSize() - 1; r++) {
            for (int c = 1; c < world.getSize() - 1; c++) {
                Position p = new Position(r, c);

                if (world.isInside(p)
                        && !world.isWall(p)
                        && !world.hasBoxAt(p)
                        && !p.equals(world.getPlayerPosition())) {
                    return p;
                }
            }
        }

        return null;
    }

    private void movePlayerToFirstFreeCell(World world) {
        Position free = findFirstFreeCell(world);
        if (free != null) {
            world.setPlayerPosition(free);
        }
    }

    private void buildBorderWalls(World world) {
        int size = world.getSize();

        for (int i = 0; i < size; i++) {
            world.setCell(new Position(0, i), CellType.WALL);
            world.setCell(new Position(size - 1, i), CellType.WALL);
            world.setCell(new Position(i, 0), CellType.WALL);
            world.setCell(new Position(i, size - 1), CellType.WALL);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int boardSize = currentWorld.getSize() * CELL_SIZE;
            int startX = (getWidth() - boardSize) / 2;
            int startY = 90;

            drawBackground(g2);
            drawHeader(g2);
            drawWorld(g2, currentWorld, startX, startY);
            drawFooter(g2);

        } finally {
            g2.dispose();
        }
    }

    private void drawBackground(Graphics2D g2) {
        GradientPaint gp = new GradientPaint(
                0, 0, BG_DARK,
                getWidth(), getHeight(), BG_MID
        );

        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawHeader(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2.drawString("Éditeur récursif - Monde " + currentWorld.getName(), 30, 40);

        String parent = currentWorld.getParentWorld() == null
                ? "Aucun"
                : String.valueOf(currentWorld.getParentWorld().getName());

        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2.drawString("Parent : " + parent, 30, 65);
        g2.drawString("Outil actuel : " + currentTool, 230, 65);
        g2.drawString("Mondes prévus : " + wantedWorldCount, 430, 65);
    }

    private void drawFooter(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2.drawString(
                "Clic gauche : modifier | Clic droit sur une WorldBox : entrer dans le monde | Bouton Monde parent : revenir",
                30,
                getHeight() - 30
        );
    }

    private void drawWorld(Graphics2D g2, World world, int startX, int startY) {
        for (int r = 0; r < world.getSize(); r++) {
            for (int c = 0; c < world.getSize(); c++) {
                Position pos = new Position(r, c);

                int x = startX + c * CELL_SIZE;
                int y = startY + r * CELL_SIZE;

                drawCell(g2, world, pos, x, y);

                AbstractBox box = world.getBoxAt(pos);
                if (box instanceof WorldBox worldBox) {
                    drawWorldBox(g2, worldBox, x, y);
                } else if (box instanceof TargetBox) {
                    drawTargetBox(g2, x, y);
                }

                if (world.getPlayerPosition() != null
                        && world.getPlayerPosition().equals(pos)) {
                    drawPlayer(g2, x, y);
                }
            }
        }
    }

    private void drawCell(Graphics2D g2, World world, Position pos, int x, int y) {
        if (world.isWall(pos)) {
            g2.setColor(WALL_FRONT);
            g2.fillRoundRect(x, y, CELL_SIZE, CELL_SIZE, ARC, ARC);

            g2.setColor(WALL_TOP);
            g2.fillRoundRect(x + 5, y + 5, CELL_SIZE - 10, 10, ARC, ARC);

            g2.setColor(WALL_SIDE);
            g2.drawRoundRect(x, y, CELL_SIZE, CELL_SIZE, ARC, ARC);
        } else {
            g2.setColor(new Color(230, 230, 245));
            g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);

            if (world.isTarget(pos)) {
                g2.setColor(TARGET);
                g2.fillOval(x + 18, y + 18, 24, 24);
            }
        }

        g2.setColor(new Color(40, 40, 60));
        g2.drawRect(x, y, CELL_SIZE, CELL_SIZE);
    }

    private void drawPlayer(Graphics2D g2, int x, int y) {
        g2.setColor(PLAYER);
        g2.fillOval(x + 12, y + 12, CELL_SIZE - 24, CELL_SIZE - 24);

        g2.setColor(Color.WHITE);
        g2.fillOval(x + 24, y + 25, 6, 6);
        g2.fillOval(x + 34, y + 25, 6, 6);
    }

    private void drawTargetBox(Graphics2D g2, int x, int y) {
        g2.setColor(TARGET_BOX);
        g2.fillRoundRect(x + 10, y + 10, CELL_SIZE - 20, CELL_SIZE - 20, 12, 12);

        g2.setColor(Color.BLACK);
        g2.drawString("B", x + 25, y + 35);
    }

    private void drawWorldBox(Graphics2D g2, WorldBox worldBox, int x, int y) {
        g2.setColor(WORLD_BOX);
        g2.fillRoundRect(x + 8, y + 8, CELL_SIZE - 16, CELL_SIZE - 16, 14, 14);

        g2.setColor(Color.BLACK);
        g2.drawRoundRect(x + 8, y + 8, CELL_SIZE - 16, CELL_SIZE - 16, 14, 14);

        String name = "?";
        if (worldBox.getInnerWorld() != null) {
            name = String.valueOf(worldBox.getInnerWorld().getName());
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString(name, x + 25, y + 35);
    }
}