import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;


import static javax.swing.JFrame.*;

public class Game {
    private Placement placement = new Placement();
    private JFrame window = new JFrame();
    private Point lastLocation;


    public Game() {
    }

    public void restart() {
        //Clear previous windows
        window.dispose();

        //Reinitialize windows
        placement = new Placement();
        window = new JFrame();
        if (lastLocation != null) {
            window.setLocation(lastLocation);
        }
        play();
    }

    public void play() {
        window.setLayout(new GridLayout(3, 3));
        JButton[] buttons = new JButton[9];
        for (int i = 0; i < 9; i++) {
            final int index = i;
            final JButton key = new JButton();
            buttons[i] = key;
            key.setPreferredSize(new Dimension(120, 120));
            key.setBackground(Color.gray);
            key.setFont(new Font(null, Font.ITALIC, 110));
            key.addMouseListener(new MouseListener() {
                @Override
                public void mousePressed(MouseEvent e) {
                    doActions(buttons, index, key);
                }

                public void mouseReleased(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }

                public void mouseClicked(MouseEvent e) {
                }
            });
            window.add(key);
        }
        window.setDefaultCloseOperation(EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }


    protected void move(int index) {
        //Update the board
        placement = placement.place(index);
    }

    public void checkEnd() {
        if (placement.gameOver()) {
            String prompt = "";
            if (placement.checkWin('x')) {
                prompt = "The computer won!";
            } else if (placement.checkWin('o')) {
                prompt = "Uh oh, you actually won. Something's wrong with the AI!";
            } else
                prompt = "It's a draw!";
            JOptionPane.showMessageDialog(null, prompt);
            int result = JOptionPane.showConfirmDialog(null, "Play again?");
            if (result == 0) {
                lastLocation = window.getLocation();
                //System.out.println("Game is restarting");
                restart();
            } else if (result == 1) //You just created 2 windows, true
                //Exit on "Cancel" or "No"
                System.exit(0);
        }

    }


    protected void doActions(JButton[] buttons, int index, JButton key) {
        boolean playerMoved = false;
        //Integer[] checkPossible = game.placement.checkPossibleMoves();
        if (Arrays.asList(placement.checkPossibleMoves()).contains(index) && !placement.gameOver()) {
            key.setText("" + placement.getPlayerMove());
            //System.out.println("index " + index);
            move(index);
            playerMoved = true;
        }

        if (!placement.gameOver() && playerMoved) {
            int best = placement.makeBestPlacement();
            buttons[best].setText("" + placement.getPlayerMove());
            move(best);
        }
        System.out.println("The board: ");
        System.out.println(getPlacement().getBoard());
        checkEnd();
    }


    public Placement getPlacement() {
        return placement;
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.play();
    }
}



