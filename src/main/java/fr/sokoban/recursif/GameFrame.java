package fr.sokoban.recursif;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameFrame extends JFrame {

    private final GameModel model;
    private final GamePanel panel;
    private final InfoPanel infoPanel;

    private boolean globalWinShown = false;
    private final Set<Character> localWinsShown = new HashSet<>();

    public GameFrame(GameModel model) {
        this.model = model;

        setTitle("Sokoban Récursif");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        this.panel = new GamePanel(model);
        this.infoPanel = new InfoPanel(model);

        panel.setOnCheminTrouve(this::jouerChemin);

        add(createHeader(), BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);

        getContentPane().setBackground(new Color(28, 36, 84));

        bindKeys();

        Timer refreshTimer = new Timer(100, e -> {
            infoPanel.refresh();
            panel.repaint();
        });
        refreshTimer.start();

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

        SwingUtilities.invokeLater(() -> panel.requestFocusInWindow());
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(18, 24, 68));
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JLabel title = new JLabel("Sokoban Récursif");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Déplace les boîtes, entre dans les mondes et résous la récursion");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(new Color(210, 220, 255));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        JLabel controls = new JLabel("↑↓←→ Déplacer   |   E Entrer   |   Backspace Sortir   |   S Solveur   |   I Indice");
        controls.setFont(new Font("SansSerif", Font.BOLD, 14));
        controls.setForeground(new Color(255, 235, 150));

        header.add(textPanel, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        return header;
    }

    private void bindKeys() {
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUp");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight");

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "enterBox");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "exitWorld");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo");

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "solve");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "hint");

        actionMap.put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!model.isWin()) {
                    model.move(Direction.UP);
                    afterAction();
                }
            }
        });

        actionMap.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!model.isWin()) {
                    model.move(Direction.DOWN);
                    afterAction();
                }
            }
        });

        actionMap.put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!model.isWin()) {
                    model.move(Direction.LEFT);
                    afterAction();
                }
            }
        });

        actionMap.put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!model.isWin()) {
                    model.move(Direction.RIGHT);
                    afterAction();
                }
            }
        });

        actionMap.put("enterBox", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (model.isWin()) return;

                Direction dir = askDirection(panel);
                if (dir != null) {
                    model.enterWorldBox(dir);
                    afterAction();
                }
            }
        });

        actionMap.put("exitWorld", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (model.isWin()) return;

                model.exitCurrentWorld();
                afterAction();
            }
        });

        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.undo();
                panel.repaint();
                infoPanel.refresh();

                if (!model.isWin()) {
                    globalWinShown = false;
                }
            }
        });

        actionMap.put("solve", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchAutoSolver();
            }
        });

        actionMap.put("hint", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHint();
            }
        });
    }

    private void jouerChemin(List<Direction> chemin) {
        final int[] i = {0};

        Timer timer = new Timer(120, null);
        timer.addActionListener(e -> {
            if (i[0] >= chemin.size() || model.isWin()) {
                ((Timer) e.getSource()).stop();
                afterAction();
                return;
            }

            model.move(chemin.get(i[0]));
            i[0]++;
            afterAction();
        });

        timer.start();
    }

    private void launchAutoSolver() {
        World rootWorld = model.getState().getRootWorld();
        World currentWorld = model.getState().getCurrentWorld();

        List<RecursiveMove> solution = RecursiveAutoSolver.solve(rootWorld, currentWorld);

        if (solution == null || solution.isEmpty()) {
            JOptionPane.showMessageDialog(
                    panel,
                    "Aucune solution trouvée.",
                    "Résolution automatique",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        animateSolution(solution);
    }

    private void showHint() {
        World rootWorld = model.getState().getRootWorld();
        World currentWorld = model.getState().getCurrentWorld();

        List<RecursiveMove> solution = RecursiveAutoSolver.solve(rootWorld, currentWorld);

        if (solution == null || solution.isEmpty()) {
            JOptionPane.showMessageDialog(
                    panel,
                    "Aucun indice disponible.",
                    "Indice",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        RecursiveMove firstMove = solution.get(0);
        String message = buildHintMessage(firstMove);

        JOptionPane.showMessageDialog(
                panel,
                message,
                "Indice",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String buildHintMessage(RecursiveMove move) {
        if (move == null) {
            return "Aucun indice disponible.";
        }

        return switch (move.getType()) {
            case MOVE -> "Indice : déplace-toi vers " + directionToFrench(move.getDirection());
            case ENTER -> "Indice : entre dans une boîte-monde vers " + directionToFrench(move.getDirection());
            case EXIT -> "Indice : sors du monde courant.";
        };
    }

    private String directionToFrench(Direction direction) {
        if (direction == null) return "";

        return switch (direction) {
            case UP -> "HAUT";
            case DOWN -> "BAS";
            case LEFT -> "GAUCHE";
            case RIGHT -> "DROITE";
        };
    }

    private void animateSolution(List<RecursiveMove> solution) {
        final int[] index = {0};

        Timer timer = new Timer(300, null);
        timer.addActionListener(e -> {
            if (index[0] >= solution.size() || model.isWin()) {
                ((Timer) e.getSource()).stop();
                afterAction();
                return;
            }

            RecursiveMove move = solution.get(index[0]);
            applyRecursiveMove(move);

            index[0]++;
            afterAction();
        });

        timer.start();
    }

    private void applyRecursiveMove(RecursiveMove move) {
        if (move == null) return;

        switch (move.getType()) {
            case MOVE -> model.move(move.getDirection());
            case ENTER -> model.enterWorldBox(move.getDirection());
            case EXIT -> model.exitCurrentWorld();
        }
    }

    private void afterAction() {
        panel.repaint();
        infoPanel.refresh();
        showWinMessagesIfNeeded();
    }

    private Direction askDirection(JComponent parent) {
        Object[] options = {"HAUT", "BAS", "GAUCHE", "DROITE"};

        int choice = JOptionPane.showOptionDialog(
                parent,
                "Choisir la direction de la boîte-monde",
                "Entrer dans une WorldBox",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        return switch (choice) {
            case 0 -> Direction.UP;
            case 1 -> Direction.DOWN;
            case 2 -> Direction.LEFT;
            case 3 -> Direction.RIGHT;
            default -> null;
        };
    }

    private void showWinMessagesIfNeeded() {
        World current = model.getState().getCurrentWorld();

        if (current != null && current.isLocalVictory() && !localWinsShown.contains(current.getName())) {
            localWinsShown.add(current.getName());

            JOptionPane.showMessageDialog(
                    panel,
                    "Le monde " + current.getName() + " est résolu !",
                    "Monde gagné",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        if (model.isWin() && !globalWinShown) {
            globalWinShown = true;

            JOptionPane.showMessageDialog(
                    panel,
                    "Bravo, vous avez gagné tout le jeu !",
                    "Victoire",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}