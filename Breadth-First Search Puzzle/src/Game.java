import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private GameState initialState;
    private GameState goalState;
    private List<Shape> shapes;
    private List<Point> blackSquares;


    public Game() {
        shapes = generatePieces();
        blackSquares = generateBlackSquares();

        initialState = new GameState(null, shapes.size());

        //Assummes at least 1 shape
        byte[] goal = new byte[shapes.size() * 2];
        goal[0] = 4; //x offset
        goal[1] = -2; //y offset

        goalState = new GameState(null, goal);
    }

    private List<Point> generateBlackSquares() {
        List<Point> blackSquares = new ArrayList<>();
        // Draw the black squares
        for (int i = 0; i < 10; i++) {
            blackSquares.add(new Point(i, 0));
            blackSquares.add(new Point(i, 9));
        }
        for (int i = 1; i < 9; i++) {
            blackSquares.add(new Point(0, i));
            blackSquares.add(new Point(9, i));
        }
        blackSquares.add(new Point(1, 1));
        blackSquares.add(new Point(1, 2));
        blackSquares.add(new Point(2, 1));
        blackSquares.add(new Point(7, 1));
        blackSquares.add(new Point(8, 1));
        blackSquares.add(new Point(8, 2));
        blackSquares.add(new Point(1, 7));
        blackSquares.add(new Point(1, 8));
        blackSquares.add(new Point(2, 8));
        blackSquares.add(new Point(8, 7));
        blackSquares.add(new Point(7, 8));
        blackSquares.add(new Point(8, 8));
        blackSquares.add(new Point(3, 4));
        blackSquares.add(new Point(4, 4));
        blackSquares.add(new Point(4, 3));
        return blackSquares;
    }

    private List<Shape> generatePieces() {
        //Draw the pieces
        //This must follow the number of shapes to work properly
        List<Shape> shapes = new ArrayList<>();
        Shape red = new Shape(0, new Color(255, 0, 0), 1, 3, 2, 3, 1, 4, 2, 4);
        shapes.add(red);
        Shape lightGreen = new Shape(1, new Color(0, 255, 0), 1, 5, 1, 6, 2, 6);
        shapes.add(lightGreen);
        Shape purple = new Shape(2, new Color(128, 128, 255), 2, 5, 3, 5, 3, 6);
        shapes.add(purple);
        Shape pink = new Shape(3, new Color(255, 128, 128), 3, 7, 3, 8, 4, 8);
        shapes.add(pink);
        Shape yellow = new Shape(4, new Color(255, 255, 128), 4, 7, 5, 7, 5, 8);
        shapes.add(yellow);
        Shape darkGreen = new Shape(5, new Color(128, 128, 0), 6, 7, 7, 7, 6, 8);
        shapes.add(darkGreen);
        Shape lightBlue = new Shape(6, new Color(0, 128, 128), 5, 4, 5, 5, 5, 6, 4, 5);
        shapes.add(lightBlue);
        Shape green = new Shape(7, new Color(0, 128, 0), 6, 4, 6, 5, 6, 6, 7, 5);
        shapes.add(green);
        Shape teal = new Shape(8, new Color(0, 255, 255), 8, 5, 8, 6, 7, 6);
        shapes.add(teal);
        Shape blue = new Shape(9, new Color(0, 0, 255), 6, 2, 6, 3, 5, 3);
        shapes.add(blue);
        Shape orange = new Shape(10, new Color(255, 128, 0), 5, 1, 6, 1, 5, 2);
        shapes.add(orange);
        return shapes;
    }

    public GameState getInitialState() {
        return initialState;
    }

    public void setInitialState(GameState initialState) {
        this.initialState = initialState;
    }

    public GameState getGoalState() {
        return goalState;
    }

    public void setGoalState(GameState goalState) {
        this.goalState = goalState;
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public void setShapes(List<Shape> shapes) {
        this.shapes = shapes;
    }

    public List<Point> getBlackSquares() {
        return blackSquares;
    }

    public void setBlackSquares(List<Point> blackSquares) {
        this.blackSquares = blackSquares;
    }
}
