package fr.sokoban.recursif;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

public class GamePanel extends JPanel {
    private static final int CELL_SIZE = 70;
    private static final int ARC = 18;
    private static final int MAX_DEPTH = 4;

    private static final Color BG_DARK = new Color(24, 31, 76);
    private static final Color BG_MID = new Color(56, 74, 160);

    private static final Color WALL_FRONT = new Color(47, 62, 123);
    private static final Color WALL_TOP = new Color(112, 150, 255);
    private static final Color WALL_SIDE = new Color(165, 202, 255);
    private static final Color WALL_OUTLINE = new Color(15, 20, 56);

    private static final Color PLAYER = new Color(222, 110, 210);
    private static final Color PLAYER_DARK = new Color(127, 49, 116);
    private static final Color PLAYER_DOT = new Color(72, 24, 72);

    private static final Color TARGET_BOX = new Color(194, 130, 59);
    private static final Color TARGET_BOX_DARK = new Color(115, 72, 31);
    private static final Color TARGET_BOX_OUTLINE = new Color(74, 45, 20);

    private final GameModel model;
    private Consumer<List<Direction>> onCheminTrouve;

    public GamePanel(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(980, 900));
        setBackground(BG_DARK);
        setFocusable(true);
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                gererClic(e.getX(), e.getY());
            }
        });
    }

    public void setOnCheminTrouve(Consumer<List<Direction>> callback) {
        this.onCheminTrouve = callback;
    }

    private void gererClic(int mx, int my) {
        if (model.isWin()) return;

        World world = model.getState().getCurrentWorld();
        int boardSize = world.getSize() * CELL_SIZE;
        int startX = (getWidth() - boardSize) / 2;
        int startY = 82;

        int col = (mx - startX) / CELL_SIZE;
        int row = (my - startY) / CELL_SIZE;

        Position cible = new Position(row, col);

        if (!world.isInside(cible)) return;
        if (world.isWall(cible)) return;
        if (world.hasBoxAt(cible)) return;

        Position joueur = world.getPlayerPosition();
        List<Direction> chemin = AStarPathFinder.trouverChemin(world, joueur, cible);

        if (chemin == null || chemin.isEmpty()) return;

        if (onCheminTrouve != null) {
            onCheminTrouve.accept(chemin);
        }
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
            int boardSize = world.getSize() * CELL_SIZE;
            int startX = (getWidth() - boardSize) / 2;
            int startY = 82;

            drawBackground(g2);
            drawBoardShadow(g2, startX, startY, boardSize);
            drawWorldRecursive(g2, world, startX, startY, CELL_SIZE, MAX_DEPTH);

            if (model.isWin()) {
                drawEndMessage(g2, "Vous avez gagné !", new Color(120, 225, 120));
            } else if (model.isLose()) {
                drawEndMessage(g2, "Vous avez perdu !", new Color(230, 100, 100));
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

        GradientPaint leftPanel = new GradientPaint(
                30, 30, new Color(255, 255, 255, 20),
                30, 210, new Color(255, 255, 255, 8)
        );
        g2.setPaint(leftPanel);
        g2.fillRoundRect(28, 24, 300, 96, 28, 28);
    }

    private void drawBoardShadow(Graphics2D g2, int startX, int startY, int boardSize) {
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(startX + 14, startY + 16, boardSize, boardSize, 30, 30);

        g2.setColor(new Color(255, 255, 255, 18));
        g2.fillRoundRect(startX - 8, startY - 8, boardSize + 16, 20, 26, 26);
    }

    private void drawWorldRecursive(Graphics2D g2, World world, int startX, int startY, int cellSize, int depth) {
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

                AbstractBox box = world.getBoxAt(pos);
                if (box != null) {
                    drawRecursiveBox(g2, box, x, y, cellSize, depth, world.isTarget(pos));
                }
            }
        }

        Position playerPos = world.getPlayerPosition();
        if (playerPos != null) {
            int px = startX + playerPos.col() * cellSize;
            int py = startY + playerPos.row() * cellSize;
            drawPlayer(g2, px, py, cellSize);
        }

        g2.setColor(new Color(10, 14, 42));
        g2.setStroke(new BasicStroke(Math.max(2f, cellSize / 30f)));
        g2.drawRoundRect(startX - 2, startY - 2, boardSize + 4, boardSize + 4, 24, 24);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawBaseCell(Graphics2D g2, World world, Position pos, int x, int y, int size) {
        if (!world.isWall(pos)) {
            Color worldBase = mix(world.getColor(), Color.WHITE, 0.25);

            GradientPaint tilePaint = new GradientPaint(
                    x, y, lighten(worldBase, 0.08),
                    x, y + size, darken(worldBase, 0.05)
            );
            g2.setPaint(tilePaint);
            g2.fillRoundRect(x, y, size, size, ARC, ARC);

            g2.setColor(new Color(255, 255, 255, 36));
            g2.fillRoundRect(x + 4, y + 4, size - 8, Math.max(8, size / 2), ARC, ARC);

            g2.setColor(new Color(255, 255, 255, 18));
            g2.drawRoundRect(x + 1, y + 1, size - 2, size - 2, ARC, ARC);

            g2.setColor(new Color(0, 0, 0, 20));
            g2.drawRoundRect(x, y, size, size, ARC, ARC);

            if (world.isTarget(pos)) {
                drawTarget(g2, x, y, size);
            }
        }
    }

    private void drawTarget(Graphics2D g2, int x, int y, int size) {
        int s = Math.max(12, size / 3);
        int cx = x + (size - s) / 2;
        int cy = y + (size - s) / 2;

        g2.setColor(new Color(255, 224, 100, 60));
        g2.fillOval(cx - 8, cy - 8, s + 16, s + 16);

        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(cx + 3, cy + 4, s, s);

        GradientPaint gp = new GradientPaint(
                cx, cy, new Color(255, 245, 170),
                cx, cy + s, new Color(245, 185, 40)
        );
        g2.setPaint(gp);
        g2.fillOval(cx, cy, s, s);

        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillOval(cx + 4, cy + 4, Math.max(4, s / 3), Math.max(4, s / 3));

        g2.setColor(new Color(180, 120, 20, 120));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx, cy, s, s);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawWall(Graphics2D g2, int x, int y, int size) {
        int depth = Math.max(5, size / 7);

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

        GradientPaint wallFront = new GradientPaint(
                x, y, lighten(WALL_FRONT, 0.05),
                x, y + size, darken(WALL_FRONT, 0.08)
        );
        g2.setPaint(wallFront);
        g2.fillRoundRect(x, y, size, size, ARC, ARC);

        g2.setColor(new Color(255, 255, 255, 26));
        g2.fillRoundRect(x + 4, y + 4, Math.max(8, size - 8), Math.max(8, size / 3), ARC, ARC);

        g2.setColor(WALL_OUTLINE);
        g2.setStroke(new BasicStroke(Math.max(1.5f, size / 32f)));
        g2.drawRoundRect(x, y, size, size, ARC, ARC);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawPlayer(Graphics2D g2, int x, int y, int size) {
        int margin = Math.max(8, size / 7);
        int bodySize = size - 2 * margin;

        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillRoundRect(x + margin + 3, y + margin + 5, bodySize, bodySize, 16, 16);

        g2.setColor(PLAYER_DARK);
        g2.fillRoundRect(x + margin + 2, y + margin + 2, bodySize, bodySize, 16, 16);

        GradientPaint playerPaint = new GradientPaint(
                x + margin, y + margin, lighten(PLAYER, 0.08),
                x + margin, y + margin + bodySize, darken(PLAYER, 0.06)
        );
        g2.setPaint(playerPaint);
        g2.fillRoundRect(x + margin, y + margin, bodySize, bodySize, 16, 16);

        g2.setColor(new Color(255, 255, 255, 62));
        g2.fillRoundRect(x + margin + 3, y + margin + 3, Math.max(6, bodySize - 6), Math.max(6, bodySize / 4), 12, 12);

        int eyeSize = Math.max(4, size / 8);
        int eyeY = y + margin + bodySize / 2 - eyeSize / 2;
        int leftEyeX = x + margin + bodySize / 3 - eyeSize / 2;
        int rightEyeX = x + margin + (2 * bodySize) / 3 - eyeSize / 2;

        g2.setColor(PLAYER_DOT);
        g2.fillOval(leftEyeX, eyeY, eyeSize, eyeSize);
        g2.fillOval(rightEyeX, eyeY, eyeSize, eyeSize);

        g2.setColor(new Color(60, 20, 60, 80));
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawRoundRect(x + margin, y + margin, bodySize, bodySize, 16, 16);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawRecursiveBox(Graphics2D g2, AbstractBox box, int x, int y, int size, int depth, boolean onTarget) {
        if (box instanceof WorldBox) {
            World innerWorld = getInnerWorldFromWorldBox((WorldBox) box);
            Color worldColor = innerWorld != null ? innerWorld.getColor() : new Color(130, 232, 118);

            drawWorldBoxFrame(g2, x, y, size, onTarget, worldColor);

            if (depth <= 0) {
                return;
            }

            if (innerWorld == null) {
                drawFallbackLetter(g2, x, y, size, "W");
                return;
            }

            int margin = Math.max(6, size / 7);
            int innerX = x + margin;
            int innerY = y + margin;
            int innerSize = size - 2 * margin;

            g2.setColor(new Color(235, 240, 255, 35));
            g2.fillRoundRect(innerX, innerY, innerSize, innerSize, 10, 10);

            Shape oldClip = g2.getClip();
            g2.setClip(new RoundRectangle2D.Double(innerX, innerY, innerSize, innerSize, 10, 10));

            drawMiniWorld(g2, innerWorld, innerX, innerY, innerSize, depth - 1);

            g2.setClip(oldClip);

            g2.setColor(new Color(255, 255, 255, 70));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(innerX, innerY, innerSize, innerSize, 10, 10);
            g2.setStroke(new BasicStroke(1f));
        } else if (box instanceof TargetBox) {
            drawTargetBox(g2, x, y, size, onTarget);
        }
    }

    private void drawTargetBox(Graphics2D g2, int x, int y, int size, boolean onTarget) {
        int margin = Math.max(7, size / 8);
        int bodySize = size - 2 * margin;

        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(x + margin + 3, y + margin + 5, bodySize, bodySize, 14, 14);

        g2.setColor(TARGET_BOX_DARK);
        g2.fillRoundRect(x + margin, y + margin, bodySize, bodySize, 14, 14);

        GradientPaint boxPaint = new GradientPaint(
                x + margin - 2, y + margin - 2, lighten(TARGET_BOX, 0.08),
                x + margin - 2, y + margin - 2 + bodySize, darken(TARGET_BOX, 0.08)
        );
        g2.setPaint(boxPaint);
        g2.fillRoundRect(x + margin - 2, y + margin - 2, bodySize, bodySize, 14, 14);

        g2.setColor(new Color(255, 255, 255, 72));
        g2.fillRoundRect(x + margin + 3, y + margin + 3, Math.max(6, bodySize - 8), Math.max(6, bodySize / 3), 12, 12);

        g2.setColor(TARGET_BOX_OUTLINE);
        g2.setStroke(new BasicStroke(Math.max(1.5f, size / 32f)));
        g2.drawRoundRect(x + margin - 2, y + margin - 2, bodySize, bodySize, 14, 14);

        g2.setColor(new Color(95, 55, 20, 110));
        g2.drawLine(x + size / 3, y + size / 3, x + (2 * size) / 3, y + (2 * size) / 3);
        g2.drawLine(x + (2 * size) / 3, y + size / 3, x + size / 3, y + (2 * size) / 3);

        if (onTarget) {
            g2.setColor(new Color(255, 231, 120));
            g2.setStroke(new BasicStroke(Math.max(2f, size / 18f)));
            g2.drawRoundRect(x + 5, y + 5, size - 10, size - 10, 16, 16);
        }

        g2.setStroke(new BasicStroke(1f));
    }

    private void drawWorldBoxFrame(Graphics2D g2, int x, int y, int size, boolean onTarget, Color worldColor) {
        int margin = Math.max(7, size / 8);
        int bodySize = size - 2 * margin;

        Color frameLight = mix(worldColor, Color.WHITE, 0.25);
        Color frameDark = darken(worldColor, 0.18);
        Color outline = darken(worldColor, 0.45);

        g2.setColor(new Color(0, 0, 0, 36));
        g2.fillRoundRect(x + margin + 3, y + margin + 5, bodySize, bodySize, 14, 14);

        g2.setColor(frameDark);
        g2.fillRoundRect(x + margin, y + margin, bodySize, bodySize, 14, 14);

        GradientPaint gp = new GradientPaint(
                x + margin - 2, y + margin - 2, lighten(frameLight, 0.08),
                x + margin - 2, y + margin - 2 + bodySize, darken(frameLight, 0.06)
        );
        g2.setPaint(gp);
        g2.fillRoundRect(x + margin - 2, y + margin - 2, bodySize, bodySize, 14, 14);

        g2.setColor(new Color(255, 255, 255, 75));
        g2.fillRoundRect(x + margin + 3, y + margin + 3, Math.max(6, bodySize - 8), Math.max(6, bodySize / 3), 12, 12);

        g2.setColor(outline);
        g2.setStroke(new BasicStroke(Math.max(1.5f, size / 32f)));
        g2.drawRoundRect(x + margin - 2, y + margin - 2, bodySize, bodySize, 14, 14);

        if (onTarget) {
            g2.setColor(new Color(255, 231, 120));
            g2.setStroke(new BasicStroke(Math.max(2f, size / 18f)));
            g2.drawRoundRect(x + 5, y + 5, size - 10, size - 10, 16, 16);
        }

        g2.setStroke(new BasicStroke(1f));
    }

    private void drawMiniWorld(Graphics2D g2, World world, int x, int y, int size, int depth) {
        int n = world.getSize();
        if (n <= 0) return;

        int cell = Math.max(4, size / n);

        Color bg = mix(world.getColor(), Color.WHITE, 0.35);
        g2.setColor(bg);
        g2.fillRoundRect(x, y, n * cell, n * cell, 8, 8);

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                Position pos = new Position(r, c);
                int cx = x + c * cell;
                int cy = y + r * cell;
                drawMiniCell(g2, world, pos, cx, cy, cell);
            }
        }

        for (AbstractBox b : world.getBoxes()) {
            Position pos = b.getPosition();
            int bx = x + pos.col() * cell;
            int by = y + pos.row() * cell;

            if (b instanceof WorldBox && depth > 0 && cell >= 10) {
                drawMiniRecursiveBox(g2, (WorldBox) b, bx, by, cell, depth - 1);
            } else {
                g2.setColor(new Color(116, 225, 98));
                g2.fillRoundRect(bx + 1, by + 1, Math.max(2, cell - 2), Math.max(2, cell - 2), 4, 4);
            }
        }

        Position p = world.getPlayerPosition();
        if (p != null) {
            int px = x + p.col() * cell;
            int py = y + p.row() * cell;
            drawMiniPlayer(g2, px, py, cell);
        }
    }

    private void drawMiniCell(Graphics2D g2, World world, Position pos, int x, int y, int size) {
        if (world.isWall(pos)) {
            drawMiniWall(g2, x, y, size);
            return;
        }

        Color base = mix(world.getColor(), Color.WHITE, 0.35);
        g2.setColor(base);
        g2.fillRoundRect(x, y, size, size, 4, 4);

        if (world.isTarget(pos)) {
            int s = Math.max(3, size / 3);
            int cx = x + (size - s) / 2;
            int cy = y + (size - s) / 2;

            g2.setColor(new Color(255, 220, 95));
            g2.fillOval(cx, cy, s, s);
        }
    }

    private void drawMiniWall(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(50, 65, 130));
        g2.fillRoundRect(x, y, size, size, 4, 4);

        g2.setColor(new Color(130, 170, 255, 80));
        g2.fillRoundRect(x + 1, y + 1, Math.max(2, size - 2), Math.max(2, size / 3), 4, 4);
    }

    private void drawMiniPlayer(Graphics2D g2, int x, int y, int size) {
        int m = Math.max(1, size / 5);
        int s = size - 2 * m;

        g2.setColor(new Color(220, 103, 206));
        g2.fillRoundRect(x + m, y + m, s, s, 4, 4);

        if (size >= 10) {
            g2.setColor(new Color(70, 28, 72));
            g2.fillOval(x + m + s / 4, y + m + s / 3, 2, 2);
            g2.fillOval(x + m + (2 * s) / 3, y + m + s / 3, 2, 2);
        }
    }

    private void drawMiniRecursiveBox(Graphics2D g2, WorldBox box, int x, int y, int size, int depth) {
        World inner = getInnerWorldFromWorldBox(box);
        Color color = inner != null ? mix(inner.getColor(), Color.WHITE, 0.2) : new Color(120, 225, 104);

        g2.setColor(color);
        g2.fillRoundRect(x + 1, y + 1, Math.max(2, size - 2), Math.max(2, size - 2), 4, 4);

        if (inner == null || depth <= 0 || size < 12) {
            return;
        }

        int margin = Math.max(2, size / 5);
        int innerX = x + margin;
        int innerY = y + margin;
        int innerSize = size - 2 * margin;

        Shape oldClip = g2.getClip();
        g2.setClip(new RoundRectangle2D.Double(innerX, innerY, innerSize, innerSize, 3, 3));

        drawMiniWorld(g2, inner, innerX, innerY, innerSize, depth - 1);

        g2.setClip(oldClip);
    }

    private void drawFallbackLetter(Graphics2D g2, int x, int y, int size, String letter) {
        g2.setColor(new Color(255, 255, 255, 210));
        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(16, size / 3)));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (size - fm.stringWidth(letter)) / 2;
        int ty = y + (size + fm.getAscent()) / 2 - 6;
        g2.drawString(letter, tx, ty);
    }

    private void drawEndMessage(Graphics2D g2, String message, Color color) {
        int w = 460;
        int h = 118;
        int x = (getWidth() - w) / 2;
        int y = 22;

        GradientPaint popupPaint = new GradientPaint(
                x, y, new Color(255, 255, 255, 240),
                x, y + h, new Color(242, 246, 255, 235)
        );
        g2.setPaint(popupPaint);
        g2.fillRoundRect(x, y, w, h, 26, 26);

        g2.setColor(color);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(x, y, w, h, 26, 26);

        g2.setColor(new Color(255, 255, 255, 90));
        g2.fillRoundRect(x + 8, y + 8, w - 16, 24, 18, 18);

        g2.setFont(new Font("SansSerif", Font.BOLD, 32));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (w - fm.stringWidth(message)) / 2;
        g2.setColor(color.darker());
        g2.drawString(message, tx, y + 46);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        String sub = "Ctrl+Z pour revenir en arrière";
        FontMetrics fm2 = g2.getFontMetrics();
        int sx = x + (w - fm2.stringWidth(sub)) / 2;

        g2.setColor(Color.DARK_GRAY);
        g2.drawString(sub, sx, y + 84);
    }

    private Color mix(Color c1, Color c2, double ratio) {
        int r = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(r, g, b);
    }

    private Color lighten(Color c, double amount) {
        int r = (int) Math.min(255, c.getRed() + (255 - c.getRed()) * amount);
        int g = (int) Math.min(255, c.getGreen() + (255 - c.getGreen()) * amount);
        int b = (int) Math.min(255, c.getBlue() + (255 - c.getBlue()) * amount);
        return new Color(r, g, b);
    }

    private Color darken(Color c, double amount) {
        int r = (int) Math.max(0, c.getRed() * (1 - amount));
        int g = (int) Math.max(0, c.getGreen() * (1 - amount));
        int b = (int) Math.max(0, c.getBlue() * (1 - amount));
        return new Color(r, g, b);
    }

    private World getInnerWorldFromWorldBox(WorldBox box) {
        String[] methodNames = {
                "getInnerWorld",
                "getWorld",
                "getContainedWorld",
                "getChildWorld",
                "getDestinationWorld"
        };

        for (String name : methodNames) {
            try {
                Method m = box.getClass().getMethod(name);
                Object result = m.invoke(box);
                if (result instanceof World) {
                    return (World) result;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }
}