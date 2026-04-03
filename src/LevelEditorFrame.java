import javax.swing.*;
import java.awt.*;

public class LevelEditorFrame extends JFrame {

    public LevelEditorFrame() {
        setTitle("Éditeur de plateau");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LevelEditorPanel editorPanel = new LevelEditorPanel();

        JPanel toolsPanel = new JPanel(new GridLayout(0, 1));

        JButton emptyButton = new JButton("Vide");
        JButton wallButton = new JButton("Mur");
        JButton playerButton = new JButton("Joueur");
        JButton boxButton = new JButton("Boîte");
        JButton targetButton = new JButton("Cible");
        JButton worldBoxButton = new JButton("Boîte-monde");

        emptyButton.addActionListener(e -> editorPanel.setCurrentTool(EditorTool.EMPTY));
        wallButton.addActionListener(e -> editorPanel.setCurrentTool(EditorTool.WALL));
        playerButton.addActionListener(e -> editorPanel.setCurrentTool(EditorTool.PLAYER));
        boxButton.addActionListener(e -> editorPanel.setCurrentTool(EditorTool.BOX));
        targetButton.addActionListener(e -> editorPanel.setCurrentTool(EditorTool.TARGET));
        worldBoxButton.addActionListener(e -> editorPanel.setCurrentTool(EditorTool.WORLD_BOX));

        toolsPanel.add(emptyButton);
        toolsPanel.add(wallButton);
        toolsPanel.add(playerButton);
        toolsPanel.add(boxButton);
        toolsPanel.add(targetButton);
        toolsPanel.add(worldBoxButton);

        add(editorPanel, BorderLayout.CENTER);
        add(toolsPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
