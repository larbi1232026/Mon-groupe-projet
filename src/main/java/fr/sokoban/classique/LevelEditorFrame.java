package fr.sokoban.classique;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LevelEditorFrame extends JFrame {

    private static final Color BG = new Color(24, 31, 76);
    private static final Color TOOLBAR = new Color(18, 24, 68);
    private static final Color CARD = new Color(245, 238, 220);
    private static final Color WOOD = new Color(130, 84, 45);
    private static final Color WOOD_DARK = new Color(82, 50, 28);
    private static final Color GREEN = new Color(80, 126, 70);
    private static final Color BLUE = new Color(74, 105, 210);
    private static final Color RED = new Color(170, 70, 70);

    private Map<Character, World> worlds;
    private LevelEditorPanel editorPanel;

    public LevelEditorFrame(Map<Character, World> worlds, char startWorld) {
        this.worlds = worlds;
        this.editorPanel = new LevelEditorPanel(worlds, startWorld);

        setTitle("Éditeur de plateau");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        refreshContent();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(TOOLBAR);
        wrapper.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Éditeur de plateau");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(new Color(255, 236, 175));

        JLabel subtitle = new JLabel("Construis ton niveau, sauvegarde-le ou lance-le directement");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(220, 230, 255));

        titlePanel.add(title);
        titlePanel.add(subtitle);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        buttonsPanel.setOpaque(false);

        buttonsPanel.add(createSectionLabel("Outils"));
        buttonsPanel.add(createToolButton("Mur", EditorTool.WALL, WOOD_DARK));
        buttonsPanel.add(createToolButton("Boîte", EditorTool.BOX, WOOD));
        buttonsPanel.add(createToolButton("Joueur", EditorTool.PLAYER, BLUE));
        buttonsPanel.add(createToolButton("Cible", EditorTool.TARGET, GREEN));
        buttonsPanel.add(createToolButton("Effacer", EditorTool.ERASE, RED));

        buttonsPanel.add(createSeparator());

        JButton borderBtn = createActionButton("Bordures", WOOD_DARK);
        borderBtn.addActionListener(e -> createBorders());

        JButton resetBtn = createActionButton("Reset", RED);
        resetBtn.addActionListener(e -> resetWorld());

        JButton playBtn = createActionButton("Jouer", GREEN);
        playBtn.addActionListener(e -> choosePlayMode());

        JButton saveBtn = createActionButton("Sauvegarder", BLUE);
        saveBtn.addActionListener(e -> saveLevel());

        JButton loadBtn = createActionButton("Lire depuis un fichier", WOOD);
        loadBtn.addActionListener(e -> loadLevel());

        buttonsPanel.add(createSectionLabel("Actions"));
        buttonsPanel.add(borderBtn);
        buttonsPanel.add(resetBtn);
        buttonsPanel.add(playBtn);
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(loadBtn);

        wrapper.add(titlePanel, BorderLayout.WEST);
        wrapper.add(buttonsPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text + " :");
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(255, 236, 175));
        return label;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 34));
        separator.setForeground(new Color(255, 255, 255, 80));
        return separator;
    }

    private JButton createToolButton(String name, EditorTool tool, Color color) {
        JButton btn = createActionButton(name, color);
        btn.addActionListener(e -> editorPanel.setCurrentTool(tool));
        return btn;
    }

    private JButton createActionButton(String name, Color color) {
        JButton btn = new JButton(name);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 70)),
                new EmptyBorder(9, 12, 9, 12)
        ));
        return btn;
    }

    private void refreshContent() {
        getContentPane().removeAll();
        add(createTopPanel(), BorderLayout.NORTH);
        add(editorPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void choosePlayMode() {
        String[] options = {"Monde actuel", "Depuis fichier"};

        int choice = JOptionPane.showOptionDialog(
                this,
                "Comment veux-tu jouer ?",
                "Mode de jeu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            playCurrentWorld();
        } else if (choice == 1) {
            playFromFile();
        }
    }

    private void playCurrentWorld() {
        World world = worlds.get(editorPanel.getCurrentWorldName());

        if (!isLevelValid(world)) {
            JOptionPane.showMessageDialog(this, "Niveau invalide : il faut un joueur, une boîte et une cible.");
            return;
        }

        World copy = world.deepCopy();
        GameState state = new GameState(copy);
        GameModel model = new GameModel(state);
        new GameFrame(model);
    }

    private void playFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir un niveau à jouer");

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            World world = loadWorldFromFile(chooser.getSelectedFile());

            if (!isLevelValid(world)) {
                JOptionPane.showMessageDialog(this, "Niveau invalide : il faut un joueur, une boîte et une cible.");
                return;
            }

            GameState state = new GameState(world);
            GameModel model = new GameModel(state);
            new GameFrame(model);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lecture fichier : " + e.getMessage());
        }
    }

    private void saveLevel() {
        World world = worlds.get(editorPanel.getCurrentWorldName());

        if (!isLevelValid(world)) {
            JOptionPane.showMessageDialog(this, "Niveau invalide : il faut un joueur, une boîte et une cible.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Sauvegarder le niveau");
        chooser.setSelectedFile(new File("niveau_custom.txt"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        try (FileWriter writer = new FileWriter(file)) {
            int size = world.getSize();
            writer.write(world.getName() + " " + size + "\n");

            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    writer.write(editorPanel.getCellChar(row, col));
                }
                writer.write("\n");
            }

            JOptionPane.showMessageDialog(this, "Niveau sauvegardé : " + file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur sauvegarde : " + e.getMessage());
        }
    }

    private void loadLevel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lire un niveau depuis un fichier");

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            World world = loadWorldFromFile(chooser.getSelectedFile());

            worlds.clear();
            worlds.put(world.getName(), world);

            editorPanel = new LevelEditorPanel(worlds, world.getName());
            refreshContent();

            JOptionPane.showMessageDialog(this, "Niveau chargé !");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement : " + e.getMessage());
        }
    }

    private World loadWorldFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine == null || firstLine.trim().isEmpty()) {
                throw new IOException("Fichier vide.");
            }

            String[] parts = firstLine.trim().split("\\s+");
            if (parts.length < 2 || parts[0].length() != 1) {
                throw new IOException("Première ligne invalide. Format attendu : A 10");
            }

            char name = parts[0].charAt(0);
            int size;
            try {
                size = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new IOException("Taille du monde invalide.");
            }

            if (size <= 0) {
                throw new IOException("La taille du monde doit être positive.");
            }

            World world = new World(name, size, Color.BLUE);
            world.setPlayerPosition(null);

            for (int row = 0; row < size; row++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("Fichier incomplet : ligne " + (row + 2) + " manquante.");
                }

                for (int col = 0; col < size; col++) {
                    char ch = col < line.length() ? line.charAt(col) : ' ';
                    applyCharToWorld(world, new Position(row, col), ch);
                }
            }

            return world;
        }
    }

    private void createBorders() {
        World world = worlds.get(editorPanel.getCurrentWorldName());
        if (world == null) return;

        int size = world.getSize();
        for (int i = 0; i < size; i++) {
            world.setCell(new Position(0, i), CellType.WALL);
            world.setCell(new Position(size - 1, i), CellType.WALL);
            world.setCell(new Position(i, 0), CellType.WALL);
            world.setCell(new Position(i, size - 1), CellType.WALL);
        }

        editorPanel.repaint();
    }

    private void resetWorld() {
        char name = editorPanel.getCurrentWorldName();
        World oldWorld = worlds.get(name);
        int size = oldWorld != null ? oldWorld.getSize() : 10;

        World newWorld = new World(name, size, Color.BLUE);
        newWorld.setPlayerPosition(null);

        worlds.clear();
        worlds.put(name, newWorld);

        editorPanel = new LevelEditorPanel(worlds, name);
        refreshContent();
    }

    private boolean isLevelValid(World world) {
        if (world == null || world.getPlayerPosition() == null) {
            return false;
        }

        if (world.getBoxes().isEmpty() || world.countTargets() == 0) {
            return false;
        }

        if (world.getBoxes().size() != world.countTargets()) {
            return false;
        }

        if (world.isWall(world.getPlayerPosition()) || world.hasBoxAt(world.getPlayerPosition())) {
            return false;
        }

        for (Box box : world.getBoxes()) {
            Position pos = box.getPosition();
            if (!world.isInside(pos) || world.isWall(pos) || pos.equals(world.getPlayerPosition())) {
                return false;
            }
        }

        return true;
    }

    private void applyCharToWorld(World world, Position pos, char ch) {
        switch (ch) {
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

    public static Map<Character, World> createDefaultWorlds() {
        Map<Character, World> map = new HashMap<>();
        World world = new World('A', 10, Color.BLUE);
        world.setPlayerPosition(null);
        map.put('A', world);
        return map;
    }
}
