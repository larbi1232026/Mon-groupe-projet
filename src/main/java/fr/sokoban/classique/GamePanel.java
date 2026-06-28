package fr.sokoban.classique;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private static final int ARC = 18;

    private static final int TOP_MARGIN = 80;
    private static final int BOTTOM_HUD_HEIGHT = 150;
    private static final int MIN_CELL_SIZE = 18;
    private static final int MAX_CELL_SIZE = 70;

    private static final Color BG_DARK = new Color(28, 36, 84);
    private static final Color BG_MID = new Color(63, 82, 170);

    private static final Color FLOOR = new Color(103, 132, 235);
    private static final Color WALL_FRONT = new Color(44, 59, 118);
    private static final Color WALL_TOP = new Color(103, 141, 247);
    private static final Color WALL_SIDE = new Color(152, 194, 255);
    private static final Color WALL_OUTLINE = new Color(17, 24, 63);

    private static final Color PLAYER = new Color(220, 103, 206);
    private static final Color PLAYER_DARK = new Color(120, 44, 112);
    private static final Color PLAYER_DOT = new Color(70, 28, 72);

    private static final Color BOX = new Color(128, 228, 108);
    private static final Color BOX_DARK = new Color(58, 145, 53);
    private static final Color BOX_OUTLINE = new Color(26, 85, 26);

    private static final Color TARGET = new Color(255, 220, 95);

    private final GameModel model;

    public GamePanel(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(1100, 920));
        setBackground(BG_DARK);
        setFocusable(true);
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            World world = model.getState().getCurrentWorld();

            int availableWidth = getWidth() - 80;
            int availableHeight = getHeight() - TOP_MARGIN - BOTTOM_HUD_HEIGHT;

            int cellSize = Math.min(
                    availableWidth / world.getSize(),
                    availableHeight / world.getSize()
            );

            cellSize = Math.max(MIN_CELL_SIZE, Math.min(MAX_CELL_SIZE, cellSize));

            int boardSize = world.getSize() * cellSize;

            int startX = (getWidth() - boardSize) / 2;
            int startY = TOP_MARGIN;

            drawBackground(g2);
            drawBoardShadow(g2, startX, startY, boardSize);
            drawWorld(g2, world, startX, startY, cellSize);
            drawHUD(g2, world, startX, startY + boardSize + 18, boardSize);

            if (model.isWin()) {
                drawEndMessage(g2, "Vous avez gagné !", new Color(120, 225, 120));
            }

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

        g2.setColor(new Color(255, 255, 255, 18));
        g2.fillRoundRect(30, 25, 260, 70, 26, 26);

        g2.setColor(new Color(255, 255, 255, 10));
        g2.fillRoundRect(getWidth() - 210, 40, 140, getHeight() - 80, 35, 35);
    }

    private void drawBoardShadow(Graphics2D g2, int startX, int startY, int boardSize) {
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillRoundRect(startX + 14, startY + 16, boardSize, boardSize, 30, 30);
    }

    private void drawWorld(Graphics2D g2, World world, int startX, int startY, int cellSize) {
        int boardSize = world.getSize() * cellSize;

        g2.setColor(new Color(18, 24, 64, 95));
        g2.fillRoundRect(startX - 8, startY - 8, boardSize + 16, boardSize + 16, 30, 30);

        for (int r = 0; r < world.getSize(); r++) {
            for (int c = 0; c < world.getSize(); c++) {
                Position pos = new Position(r, c);
                int x = startX + c * cellSize;
                int y = startY + r * cellSize;
                drawBaseCell(g2, world, pos, x, y, cellSize);
            }
        }

        for (int r = 0; r < world.getSize(); r++) {
            for (int c = 0; c < world.getSize(); c++) {
                Position pos = new Position(r, c);
                int x = startX + c * cellSize;
                int y = startY + r * cellSize;

                if (world.isWall(pos)) {
                    drawWall(g2, x, y, cellSize);
                }
            }
        }

        for (int r = 0; r < world.getSize(); r++) {
            for (int c = 0; c < world.getSize(); c++) {
                Position pos = new Position(r, c);
                int x = startX + c * cellSize;
                int y = startY + r * cellSize;

                Box box = world.getBoxAt(pos);
                if (box != null) {
                    drawBox(g2, x, y, cellSize, world.isTarget(pos));
                }
            }
        }

        Position playerPos = world.getPlayerPosition();

        if (playerPos != null) {
            int x = startX + playerPos.col() * cellSize;
            int y = startY + playerPos.row() * cellSize;
            drawPlayer(g2, x, y, cellSize);
        }

        g2.setColor(new Color(10, 14, 42));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(startX - 2, startY - 2, boardSize + 4, boardSize + 4, 24, 24);
    }

    private void drawBaseCell(Graphics2D g2, World world, Position pos, int x, int y, int size) {
        if (!world.isWall(pos)) {
            g2.setColor(FLOOR);
            g2.fillRoundRect(x, y, size, size, ARC, ARC);

            g2.setColor(new Color(255, 255, 255, 34));
            g2.fillRoundRect(x + 4, y + 4, Math.max(1, size - 8), Math.max(1, size / 2), ARC, ARC);

            g2.setColor(new Color(0, 0, 0, 22));
            g2.drawRoundRect(x, y, size, size, ARC, ARC);

            if (world.isTarget(pos)) {
                drawTarget(g2, x, y, size);
            }
        }
    }

    private void drawTarget(Graphics2D g2, int x, int y, int size) {
        int s = Math.max(6, size / 3);
        int cx = x + (size - s) / 2;
        int cy = y + (size - s) / 2;

        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillOval(cx + 2, cy + 3, s, s);

        g2.setColor(TARGET);
        g2.fillOval(cx, cy, s, s);
    }

    private void drawWall(Graphics2D g2, int x, int y, int size) {
        int depth = Math.max(3, size / 7);

        Polygon top = new Polygon();
        top.addPoint(x, y);
        top.addPoint(x + depth, y - depth);
        top.addPoint(x + size + depth, y - depth);
        top.addPoint(x + size, y);

        Polygon side = new Polygon();
        side.addPoint(x + size, y);
        side.addPoint(x + size + depth, y - depth);
        side.addPoint(x + size + depth, y + size - depth);
        side.addPoint(x + size, y + size);

        g2.setColor(WALL_TOP);
        g2.fillPolygon(top);

        g2.setColor(WALL_SIDE);
        g2.fillPolygon(side);

        g2.setColor(WALL_FRONT);
        g2.fillRoundRect(x, y, size, size, ARC, ARC);

        g2.setColor(WALL_OUTLINE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, size, size, ARC, ARC);
    }

    private void drawPlayer(Graphics2D g2, int x, int y, int size) {
        int margin = Math.max(3, size / 6);
        int bodySize = Math.max(6, size - 2 * margin);

        g2.setColor(PLAYER_DARK);
        g2.fillRoundRect(x + margin + 2, y + margin + 2, bodySize, bodySize, 16, 16);

        g2.setColor(PLAYER);
        g2.fillRoundRect(x + margin, y + margin, bodySize, bodySize, 16, 16);

        int eyeSize = Math.max(3, size / 8);
        int eyeY = y + margin + bodySize / 2 - eyeSize / 2;
        int leftEyeX = x + margin + bodySize / 3 - eyeSize / 2;
        int rightEyeX = x + margin + (2 * bodySize) / 3 - eyeSize / 2;

        g2.setColor(PLAYER_DOT);
        g2.fillOval(leftEyeX, eyeY, eyeSize, eyeSize);
        g2.fillOval(rightEyeX, eyeY, eyeSize, eyeSize);
    }

    private void drawBox(Graphics2D g2, int x, int y, int size, boolean onTarget) {
        int margin = Math.max(3, size / 7);
        int bodySize = Math.max(6, size - 2 * margin);

        g2.setColor(BOX_DARK);
        g2.fillRoundRect(x + margin, y + margin, bodySize, bodySize, 14, 14);

        g2.setColor(BOX);
        g2.fillRoundRect(x + margin - 1, y + margin - 1, bodySize, bodySize, 14, 14);

        g2.setColor(BOX_OUTLINE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x + margin - 1, y + margin - 1, bodySize, bodySize, 14, 14);

        if (onTarget) {
            g2.setColor(new Color(255, 231, 120));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(x + 4, y + 4, size - 8, size - 8, 16, 16);
        }

        g2.setStroke(new BasicStroke(1f));
    }

    private void drawHUD(Graphics2D g2, World world, int x, int y, int boardSize) {
        int hudX = x - 5;
        int hudY = y;
        int hudW = boardSize + 10;
        int hudH = 132;

        g2.setColor(new Color(14, 20, 56, 185));
        g2.fillRoundRect(hudX, hudY, hudW, hudH, 24, 24);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("SOKOBAN  •  Monde " + world.getName(), hudX + 18, hudY + 30);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2.setColor(new Color(235, 240, 255));
        g2.drawString("Déplacements : " + model.getMoveCount(), hudX + 18, hudY + 58);
        g2.drawString("Boîtes sur cibles : " + world.countBoxesOnTargets() + " / " + world.getBoxes().size(), hudX + 18, hudY + 82);
        g2.drawString("Flèches = bouger | Ctrl+Z = annuler | S = résoudre", hudX + 18, hudY + 106);

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));

        if (model.isWin()) {
            g2.setColor(new Color(160, 255, 170));
            g2.drawString("État : Gagné", hudX + hudW - 130, hudY + 30);
        } else if (model.isLose()) {
            g2.setColor(new Color(255, 200, 120));
            g2.drawString("État : Bloqué", hudX + hudW - 135, hudY + 30);
        } else {
            g2.setColor(new Color(210, 220, 255));
            g2.drawString("État : En cours", hudX + hudW - 145, hudY + 30);
        }
    }

    private void drawEndMessage(Graphics2D g2, String message, Color color) {
        int overlayWidth = 460;
        int overlayHeight = 118;

        int x = (getWidth() - overlayWidth) / 2;
        int y = 24;

        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(x, y, overlayWidth, overlayHeight, 26, 26);

        g2.setColor(color);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(x, y, overlayWidth, overlayHeight, 26, 26);

        g2.setFont(new Font("SansSerif", Font.BOLD, 32));
        FontMetrics fm = g2.getFontMetrics();

        int textX = x + (overlayWidth - fm.stringWidth(message)) / 2;
        int textY = y + 46;

        g2.drawString(message, textX, textY);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));

        String subText = "Appuyez sur Ctrl+Z pour revenir en arrière";
        FontMetrics fm2 = g2.getFontMetrics();
        int subTextX = x + (overlayWidth - fm2.stringWidth(subText)) / 2;

        g2.setColor(Color.DARK_GRAY);
        g2.drawString(subText, subTextX, y + 84);
    }
}