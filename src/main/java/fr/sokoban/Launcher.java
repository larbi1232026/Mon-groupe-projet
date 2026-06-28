package fr.sokoban;

import javax.swing.*;
import java.awt.*;

public class Launcher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Launcher::showChoiceMenu);
    }

    private static void showChoiceMenu() {
        JFrame frame = new JFrame("Sokoban - Choix de la version");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3, 1, 10, 10));

        JLabel title = new JLabel("Choisissez une version", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JButton classiqueBtn = new JButton("Sokoban Classique");
        classiqueBtn.addActionListener(e -> {
            frame.dispose();
            new fr.sokoban.classique.StartMenuFrame();
        });

        JButton recursifBtn = new JButton("Sokoban Récursif");
        recursifBtn.addActionListener(e -> {
            frame.dispose();
            new fr.sokoban.recursif.StartMenuFrame();
        });

        frame.add(title);
        frame.add(classiqueBtn);
        frame.add(recursifBtn);

        frame.setSize(350, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}