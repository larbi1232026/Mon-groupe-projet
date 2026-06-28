package fr.sokoban.recursif;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class LevelEditorFrame extends JFrame {
    private LevelEditorPanel editorPanel;
    private JComboBox<Character> worldSelector;
    private JSpinner worldBoxCountSpinner;
    private boolean updatingWorldSelector = false;

    public LevelEditorFrame(World rootWorld) {
        this.editorPanel = new LevelEditorPanel(rootWorld);
        this.editorPanel.setWorldListChangedListener(this::refreshWorldSelector);

        setTitle("Éditeur récursif");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildToolbar(), BorderLayout.NORTH);
        add(editorPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private JComponent buildToolbar() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JComboBox<EditorTool> toolSelector = new JComboBox<>(EditorTool.values());
        toolSelector.setSelectedItem(EditorTool.WALL);

        toolSelector.addActionListener(e -> {
            EditorTool tool = (EditorTool) toolSelector.getSelectedItem();
            if (tool != null) {
                editorPanel.setCurrentTool(tool);
            }
        });

        worldSelector = new JComboBox<>();
        worldSelector.addActionListener(e -> {
            if (updatingWorldSelector) {
                return;
            }
            Character selectedWorld = (Character) worldSelector.getSelectedItem();
            if (selectedWorld != null) {
                editorPanel.selectWorld(selectedWorld);
            }
        });
        refreshWorldSelector();

        worldBoxCountSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));

        JButton createWorldBoxesButton = new JButton("Créer boîtes-mondes");
        createWorldBoxesButton.addActionListener(e -> createWorldBoxes());

        JButton parentButton = new JButton("Monde parent");
        parentButton.addActionListener(e -> goToParentWorld());

        JButton playButton = new JButton("Jouer");
        playButton.addActionListener(e -> playLevel());

        JButton saveButton = new JButton("Sauvegarder");
        saveButton.addActionListener(e -> saveLevel());

        JButton loadButton = new JButton("Charger");
        loadButton.addActionListener(e -> loadLevel());

        JButton helpButton = new JButton("Aide");
        helpButton.addActionListener(e -> showHelp());

        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());

        top.add(new JLabel("Outil :"));
        top.add(toolSelector);
        top.add(new JLabel("Monde à éditer :"));
        top.add(worldSelector);
        top.add(new JLabel("Nb boîtes-mondes :"));
        top.add(worldBoxCountSpinner);
        top.add(createWorldBoxesButton);
        top.add(parentButton);
        top.add(playButton);
        top.add(saveButton);
        top.add(loadButton);
        top.add(helpButton);
        top.add(closeButton);

        return top;
    }

    private void refreshWorldSelector() {
        if (worldSelector == null || editorPanel == null) {
            return;
        }

        updatingWorldSelector = true;
        worldSelector.removeAllItems();
        for (Character name : editorPanel.getWorldNames()) {
            worldSelector.addItem(name);
        }
        worldSelector.setSelectedItem(editorPanel.getCurrentWorldName());
        updatingWorldSelector = false;
    }

    private void createWorldBoxes() {
        int count = (Integer) worldBoxCountSpinner.getValue();
        editorPanel.ensureRootWorldBoxes(count);
        refreshWorldSelector();

        JOptionPane.showMessageDialog(
                this,
                "Les mondes ont été préparés. Sélectionnez A, B, C... dans la liste pour éditer chaque monde.",
                "Mondes prêts",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void goToParentWorld() {
        boolean ok = editorPanel.goToParentWorld();
        refreshWorldSelector();

        if (!ok) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vous êtes déjà dans le monde racine.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void playLevel() {
        World root = editorPanel.getRootWorld();

        if (!isValidLevel(root)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Niveau invalide : il faut au moins un joueur, une boîte et une cible.",
                    "Niveau invalide",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        World copy = root.deepCopy();
        GameState state = new GameState(copy);
        GameModel model = new GameModel(state);
        new GameFrame(model);
    }

    private void saveLevel() {
        World root = editorPanel.getRootWorld();

        if (!isValidLevel(root)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Niveau invalide : impossible de sauvegarder.\nIl faut au moins un joueur, une boîte et une cible.",
                    "Niveau invalide",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Sauvegarder le niveau récursif");
        chooser.setSelectedFile(new File("niveau_recursif.txt"));

        int result = chooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {
            WorldSerializer.save(root, file.getAbsolutePath());

            JOptionPane.showMessageDialog(
                    this,
                    "Niveau sauvegardé avec succès.",
                    "Sauvegarde réussie",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de la sauvegarde : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadLevel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Charger un niveau récursif");

        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {
            World loadedRoot = WorldSerializer.load(file.getAbsolutePath());

            getContentPane().removeAll();

            editorPanel = new LevelEditorPanel(loadedRoot);
            editorPanel.setWorldListChangedListener(this::refreshWorldSelector);

            add(buildToolbar(), BorderLayout.NORTH);
            add(editorPanel, BorderLayout.CENTER);

            revalidate();
            repaint();

            JOptionPane.showMessageDialog(
                    this,
                    "Niveau chargé avec succès.",
                    "Chargement réussi",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors du chargement : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean isValidLevel(World root) {
        if (root == null) {
            return false;
        }

        return root.getPlayerPosition() != null
                && root.countTargets() > 0
                && root.countTargetBoxes() > 0;
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(
                this,
                """
                Éditeur récursif :

                - Choisis un outil en haut.
                - Clic gauche : place l'élément sélectionné.
                - Outil TARGET_BOX : place une boîte classique.
                - Outil WORLD_BOX : crée une boîte-monde avec un monde intérieur.
                - Choisis le nombre de boîtes-mondes puis clique sur "Créer boîtes-mondes".
                - Liste "Monde à éditer" : sélectionne A, B, C... pour modifier chaque monde.
                - Clic droit sur une WorldBox : ouvre aussi son monde intérieur.
                - Bouton "Monde parent" : revient au monde parent.
                - Bouton "Jouer" : lance le niveau créé.
                - Bouton "Sauvegarder" : enregistre le niveau dans un fichier texte.
                - Bouton "Charger" : ouvre un niveau depuis un fichier texte.
                """,
                "Aide",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
