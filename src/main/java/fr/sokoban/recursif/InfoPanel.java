package fr.sokoban.recursif;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InfoPanel extends JPanel {
    private final GameModel model;

    private final JLabel worldLabel;
    private final JLabel parentLabel;
    private final JLabel movesLabel;
    private final JLabel boxesLabel;
    private final JLabel stateLabel;

    public InfoPanel(GameModel model) {
        this.model = model;

        setPreferredSize(new Dimension(260, 0));
        setBackground(new Color(35, 45, 105));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 18, 20, 18));

        JLabel title = new JLabel("Informations");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(title);
        add(Box.createVerticalStrut(18));

        worldLabel = createInfoLabel();
        parentLabel = createInfoLabel();
        movesLabel = createInfoLabel();
        boxesLabel = createInfoLabel();
        stateLabel = createInfoLabel();

        add(createSection("Monde actuel", worldLabel));
        add(Box.createVerticalStrut(10));
        add(createSection("Monde parent", parentLabel));
        add(Box.createVerticalStrut(10));
        add(createSection("Nombre de coups", movesLabel));
        add(Box.createVerticalStrut(10));
        add(createSection("Boîtes sur cibles", boxesLabel));
        add(Box.createVerticalStrut(10));
        add(createSection("État", stateLabel));
        add(Box.createVerticalStrut(20));

        JLabel controlsTitle = new JLabel("Touches");
        controlsTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        controlsTitle.setForeground(new Color(255, 255, 255));
        controlsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(controlsTitle);
        add(Box.createVerticalStrut(10));

        add(createControlLabel("↑ ↓ ← → : déplacer"));
        add(createControlLabel("Ctrl+Z : annuler"));
        add(createControlLabel("Backspace : sortir"));
        add(createControlLabel("E : entrer"));
        add(createControlLabel("S : solveur"));
        add(createControlLabel("I : indice"));

        add(Box.createVerticalGlue());

        JLabel footer = new JLabel("Projet Sokoban récursif");
        footer.setForeground(new Color(210, 220, 255));
        footer.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(footer);

        refresh();
    }

    private JPanel createSection(String title, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 6));
        panel.setBackground(new Color(52, 66, 140));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(230, 235, 255));

        panel.add(sectionTitle, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createInfoLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JLabel createControlLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(new Color(225, 232, 255));
        label.setBorder(new EmptyBorder(3, 0, 3, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public void refresh() {
        World world = model.getState().getCurrentWorld();

        String worldName = world == null ? "-" : String.valueOf(world.getName());
        String parentName = "-";
        if (world != null && world.getParentWorld() != null) {
            parentName = String.valueOf(world.getParentWorld().getName());
        }

        worldLabel.setText(worldName);
        parentLabel.setText(parentName);
        movesLabel.setText(String.valueOf(model.getMoveCount()));

        if (world != null) {
            boxesLabel.setText(world.countBoxesOnTargets() + " / " + world.countTargetBoxes());
        } else {
            boxesLabel.setText("-");
        }

        if (model.isWin()) {
            stateLabel.setText("Gagné");
            stateLabel.setForeground(new Color(150, 255, 170));
        } else if (model.isLose()) {
            stateLabel.setText("Perdu");
            stateLabel.setForeground(new Color(255, 140, 140));
        } else {
            stateLabel.setText("En cours");
            stateLabel.setForeground(Color.WHITE);
        }
    }
}