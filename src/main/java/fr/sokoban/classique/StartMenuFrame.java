package fr.sokoban.classique;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StartMenuFrame extends JFrame {
    private static final Color BG_TOP = new Color(10, 16, 42);
    private static final Color BG_BOTTOM = new Color(35, 48, 105);
    private static final Color CARD = new Color(245, 238, 220);
    private static final Color CARD_HOVER = new Color(255, 248, 225);
    private static final Color WOOD = new Color(130, 84, 45);
    private static final Color WOOD_DARK = new Color(82, 50, 28);
    private static final Color TEXT_DARK = new Color(45, 35, 28);

    public StartMenuFrame() {
        setTitle("Sokoban classique - Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new BackgroundPanel());
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createLevelsScroll(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        setSize(1050, 780);
        setMinimumSize(new Dimension(950, 700));
        setLocationRelativeTo(null);
        setResizable(true);
        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 45, 18, 45));

        JPanel sign = new JPanel(new BorderLayout());
        sign.setBackground(WOOD);
        sign.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WOOD_DARK, 5),
                new EmptyBorder(18, 25, 18, 25)
        ));

        JLabel title = new JLabel("SOKOBAN CLASSIQUE");
        title.setFont(new Font("Serif", Font.BOLD, 42));
        title.setForeground(new Color(255, 236, 175));

        JLabel subtitle = new JLabel("Pousse les caisses jusqu’aux cibles sans les bloquer");
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        subtitle.setForeground(Color.WHITE);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 4));
        text.setOpaque(false);
        text.add(title);
        text.add(subtitle);

        JButton quit = createWoodButton("Quitter");
        quit.addActionListener(e -> System.exit(0));

        sign.add(text, BorderLayout.WEST);
        sign.add(quit, BorderLayout.EAST);

        header.add(sign, BorderLayout.CENTER);
        return header;
    }

    private JScrollPane createLevelsScroll() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(5, 45, 10, 45));

        JLabel section = new JLabel("Choisis ton niveau");
        section.setFont(new Font("Serif", Font.BOLD, 30));
        section.setForeground(new Color(255, 240, 190));
        section.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel grid = new JPanel(new GridLayout(2, 3, 22, 22));
        grid.setOpaque(false);

        grid.add(createLevelCard(1, "Début", "Un premier niveau simple.", "niveau1.txt", 1));
        grid.add(createLevelCard(2, "Couloir", "Premiers murs et obstacles.", "niveau2.txt", 2));
        grid.add(createLevelCard(3, "Deux caisses", "Il faut organiser plusieurs déplacements.", "niveau3.txt", 3));
        grid.add(createLevelCard(4, "Entrepôt", "Plus de murs et de choix.", "niveau4.txt", 4));
        grid.add(createLevelCard(5, "Casse-tête", "Attention aux coins et aux blocages.", "niveau5.txt", 5));
        grid.add(createLevelCard(6, "Défi final", "Le niveau le plus difficile.", "niveau6.txt", 6));

        wrapper.add(section, BorderLayout.NORTH);
        wrapper.add(grid, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        return scroll;
    }

    private JPanel createLevelCard(int level, String title, String description, String fileName, int difficulty) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WOOD_DARK, 4),
                new EmptyBorder(18, 18, 18, 18)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(280, 210));

        JLabel levelLabel = new JLabel("Niveau " + level);
        levelLabel.setFont(new Font("Serif", Font.BOLD, 28));
        levelLabel.setForeground(WOOD_DARK);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_DARK);

        JTextArea desc = new JTextArea(description);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        desc.setForeground(TEXT_DARK);
        desc.setOpaque(false);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFocusable(false);

        JLabel stars = new JLabel("Difficulté : " + "★".repeat(difficulty) + "☆".repeat(6 - difficulty));
        stars.setFont(new Font("SansSerif", Font.BOLD, 14));
        stars.setForeground(new Color(160, 105, 25));

        JButton play = createActionButton("Jouer");
        play.addActionListener(e -> launchLevel(fileName));

        JButton edit = createActionButton("Éditer");
        edit.addActionListener(e -> editLevel(fileName));

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setOpaque(false);
        top.add(levelLabel);
        top.add(titleLabel);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        buttons.add(play);
        buttons.add(edit);

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        bottom.add(stars, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(CARD_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(CARD);
            }
        });

        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 45, 24, 45));

        JLabel controls = new JLabel("Contrôles : flèches = bouger | Ctrl+Z = annuler | S = solveur automatique");
        controls.setFont(new Font("SansSerif", Font.BOLD, 14));
        controls.setForeground(new Color(255, 245, 215));

        JButton editor = createWoodButton("Créer un niveau vide");
        editor.addActionListener(e -> openEmptyEditor());

        footer.add(controls, BorderLayout.WEST);
        footer.add(editor, BorderLayout.EAST);

        return footer;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(80, 126, 70));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return button;
    }

    private JButton createWoodButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(WOOD_DARK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        return button;
    }

    private void launchLevel(String fileName) {
        World world = loadWorld(fileName);
        if (world == null) return;

        GameState state = new GameState(world.deepCopy());
        GameModel model = new GameModel(state);
        new GameFrame(model);
    }

    private void editLevel(String fileName) {
        World world = loadWorld(fileName);
        if (world == null) return;

        Map<Character, World> worlds = new HashMap<>();
        worlds.put('A', world.deepCopy());
        new LevelEditorFrame(worlds, 'A');
    }

    private void openEmptyEditor() {
        Map<Character, World> worlds = LevelEditorFrame.createDefaultWorlds();
        World world = worlds.get('A');

        for (int i = 0; i < world.getSize(); i++) {
            world.setCell(new Position(0, i), CellType.WALL);
            world.setCell(new Position(world.getSize() - 1, i), CellType.WALL);
            world.setCell(new Position(i, 0), CellType.WALL);
            world.setCell(new Position(i, world.getSize() - 1), CellType.WALL);
        }

        world.setPlayerPosition(new Position(1, 1));
        new LevelEditorFrame(worlds, 'A');
    }

    private World loadWorld(String fileName) {
        try {
            return loadWorldFromFile(new File(fileName));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors du chargement de " + fileName + " :\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }

    private World loadWorldFromFile(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("Fichier introuvable : " + file.getName());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();

            if (firstLine == null || firstLine.trim().isEmpty()) {
                throw new IOException("Fichier vide.");
            }

            String[] parts = firstLine.trim().split("\\s+");
            char name = parts[0].charAt(0);
            int size = Integer.parseInt(parts[1]);

            World world = new World(name, size, Color.BLUE);
            world.setPlayerPosition(null);

            for (int row = 0; row < size; row++) {
                String line = reader.readLine();

                if (line == null) {
                    throw new IOException("Fichier incomplet.");
                }

                for (int col = 0; col < size; col++) {
                    char ch = col < line.length() ? line.charAt(col) : ' ';
                    applyCharToWorld(world, new Position(row, col), ch);
                }
            }

            return world;
        }
    }

    private void applyCharToWorld(World world, Position pos, char ch) {
        switch (ch) {
            case '#':
                world.setCell(pos, CellType.WALL);
                world.removeBoxAt(pos);
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
                break;
        }
    }

    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            GradientPaint gradient = new GradientPaint(
                    0, 0, BG_TOP,
                    0, getHeight(), BG_BOTTOM
            );

            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(80, 120, 80, 70));
            g2.fillOval(-120, getHeight() - 170, 420, 220);
            g2.fillOval(getWidth() - 250, getHeight() - 150, 420, 220);

            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillOval(80, 80, 160, 160);
            g2.fillOval(getWidth() - 260, 70, 120, 120);

            g2.dispose();
        }
    }
}
