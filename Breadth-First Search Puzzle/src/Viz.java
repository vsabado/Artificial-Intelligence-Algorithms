import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static java.lang.Thread.sleep;

class View extends JPanel {
    Viz viz;
    byte[] state;
    Graphics graphics;
    int size;
    List<Shape> shapes;
    List<Point> blackSquares;
    GameState current;

    View(Viz v) throws IOException {
        viz = v;
        shapes = new ArrayList<>();
        state = new byte[22]; //current state of the board so we can draw
        size = 48;
    }

    public void paintComponent(Graphics g) {
        if (blackSquares == null || shapes == null) {
            return;
        }
        //Draw black squares
        for (Point pt : blackSquares) {
            drawBlock(g, new Color(0, 0, 0), pt);
        }

        //Draw shapes
        for (Shape shape : shapes) {
            for (Point point : shape.getPoints()) {
                Point tmp = new Point(point.getX() + current.getState()[2 * shape.getId()],
                        point.getY() + current.getState()[2 * shape.getId() + 1]);
                drawBlock(g, shape.color, tmp);
            }
        }
    }

    public void drawGameState(GameState state, List<Shape> shapes, List<Point> blackSquares) {
        this.current = state;
        this.shapes = shapes;
        this.blackSquares = blackSquares;
        viz.repaint();
    }

    public void drawBlock(Graphics graphics, Color color, Point point) {
        graphics.setColor(color);
        graphics.fillRect(size * point.getX(), size * point.getY(), size, size);
    }

}

public class Viz extends JFrame {
    private View view;

    public Viz() throws Exception {
        view = new View(this);
        this.setTitle("Puzzle");
        this.setSize(482, 505);
        this.getContentPane().add(view);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public static void main(String[] args) throws Exception {
        Viz viz = new Viz();
        Game g = new Game();
        BreadthFirstSearch bfs = new BreadthFirstSearch(g.getInitialState(), g.getGoalState(), g.getBlackSquares(), g.getShapes());


        //Draw Initial Board
        viz.getView().drawGameState(g.getInitialState(), g.getShapes(), g.getBlackSquares());

        long startTime = System.nanoTime();
        GameState solutionState = bfs.compute();
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        if (solutionState == null) {
            System.out.println("No solution");
        } else {
            System.out.println("Number of Pieces: " + g.getShapes().size());
            System.out.println("Runtime:" + duration / 1000000000.0 + " seconds.");
            System.out.println("Final State:" + solutionState);
            System.out.println("Solution Path: " + solutionState.getPathLength());
            solutionState.printPath();

            //Simulate the path that the BFS found
            List<GameState> paths = solutionState.getPathList();
            for (GameState state : paths) {
                sleep(500);
                viz.getView().drawGameState(state, g.getShapes(), g.getBlackSquares());
            }
        }

    }


}