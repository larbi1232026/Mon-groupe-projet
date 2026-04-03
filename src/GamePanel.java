import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {

    private GameModel model;

    public GamePanel(GameModel model) {
        this.model = model;

        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        model.move(Direction.UP);
                        break;
                    case KeyEvent.VK_DOWN:
                        model.move(Direction.DOWN);
                        break;
                    case KeyEvent.VK_LEFT:
                        model.move(Direction.LEFT);
                        break;
                    case KeyEvent.VK_RIGHT:
                        model.move(Direction.RIGHT);
                        break;
                }

                repaint(); // mise à jour de l'écran
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawPlayer(g);
    }

    private void drawPlayer(Graphics g) {
        Position p = model.getPlayerPosition();

        int size = 40;

        g.setColor(Color.BLUE);
        g.fillRect(p.getY() * size, p.getX() * size, size, size);
    }
}
