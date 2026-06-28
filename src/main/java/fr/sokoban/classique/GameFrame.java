package fr.sokoban.classique;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class GameFrame extends JFrame {

    public GameFrame(GameModel model) {
        setTitle("Sokoban Classique");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        GamePanel panel = new GamePanel(model);
        setContentPane(panel);
        getContentPane().setBackground(new Color(28, 36, 84));

        bindKeys(panel, model);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void bindKeys(GamePanel panel, GameModel model) {
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUp");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "solve");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "hint");

        actionMap.put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!model.isWin()) {
                    model.move(Direction.UP);
                    panel.repaint();
                }
            }
        });

        actionMap.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!model.isWin()) {
                    model.move(Direction.DOWN);
                    panel.repaint();
                }
            }
        });

        actionMap.put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!model.isWin()) {
                    model.move(Direction.LEFT);
                    panel.repaint();
                }
            }
        });

        actionMap.put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!model.isWin()) {
                    model.move(Direction.RIGHT);
                    panel.repaint();
                }
            }
        });

        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.undo();
                panel.repaint();
            }
        });

        actionMap.put("solve", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (model.isWin()) {
                    return;
                }

                List<Direction> solution = model.solveAutomatically();

                if (solution == null || solution.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            panel,
                            "Aucune solution trouvée pour ce plateau.",
                            "Résolution automatique",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }

                animateSolution(panel, model, solution);
            }
        });

        actionMap.put("hint", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (model.isWin()) {
                    JOptionPane.showMessageDialog(
                            panel,
                            "Le niveau est déjà terminé.",
                            "Indice",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }

                showHint(panel, model);
            }
        });
    }

    private void showHint(GamePanel panel, GameModel model) {
        List<Direction> solution = model.solveAutomatically();

        if (solution == null || solution.isEmpty()) {
            JOptionPane.showMessageDialog(
                    panel,
                    "Aucun indice disponible pour ce plateau.",
                    "Indice",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        Direction firstMove = solution.get(0);

        JOptionPane.showMessageDialog(
                panel,
                "Indice : déplace-toi vers " + directionToFrench(firstMove),
                "Indice",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String directionToFrench(Direction direction) {
        if (direction == null) {
            return "";
        }

        return switch (direction) {
            case UP -> "HAUT";
            case DOWN -> "BAS";
            case LEFT -> "GAUCHE";
            case RIGHT -> "DROITE";
        };
    }

    private void animateSolution(GamePanel panel, GameModel model, List<Direction> solution) {
        final int[] index = {0};

        Timer timer = new Timer(170, null);
        timer.addActionListener(e -> {
            if (index[0] >= solution.size() || model.isWin()) {
                timer.stop();
                panel.repaint();

                if (model.isWin()) {
                    JOptionPane.showMessageDialog(
                            panel,
                            "Résolution terminée avec succès.",
                            "Résolution automatique",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
                return;
            }

            model.move(solution.get(index[0]));
            index[0]++;
            panel.repaint();
        });

        timer.start();
    }
}