import javax.swing.*;

public class StartMenuFrame extends JFrame {

    public StartMenuFrame() {
        setTitle("Menu");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton startButton = new JButton("Start Game");

        startButton.addActionListener(e -> {
            CellType[][] grid = {
                {CellType.EMPTY, CellType.EMPTY, CellType.EMPTY},
                {CellType.EMPTY, CellType.EMPTY, CellType.EMPTY},
                {CellType.EMPTY, CellType.EMPTY, CellType.EMPTY}
            };

            World world = new World(grid);
            GameModel model = new GameModel(world, new Position(1, 1));

            new GameFrame(model);
            dispose();
        });

        add(startButton);

        setVisible(true);
    }
}
