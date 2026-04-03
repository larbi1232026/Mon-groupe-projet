import javax.swing.*;

public class GameFrame extends JFrame {

    public GameFrame(GameModel model) {
        setTitle("Parabox-like Game");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GamePanel panel = new GamePanel(model);
        add(panel);

        setVisible(true);
    }
}
