import java.util.Arrays;

public class Qtable {

    private double[][][] qtable = new double[Maze.HEIGHT][Maze.WIDTH][Action.values().length];
    public static final int HEIGHT = Maze.HEIGHT;
    public static final int WIDTH = Maze.WIDTH;
    public static final int NUM_ACTIONS = Action.values().length;

    public Qtable() {
    }


    public double get(Point current, Action action){
        if(current == null || action == null){
            //Should not be here
            throw new RuntimeException("Null state or null action");
        }
        return qtable[current.getRow()][current.getColumn()][action.ordinal()];
    }
    public void set(Point current, Action action, double value){
        if(current == null || action == null){
            return;
        }
        qtable[current.getRow()][current.getColumn()][action.ordinal()] = value;
    }


    public double[][][] getQtable() {
        return qtable;
    }

    public String toString(){
        StringBuilder str = new StringBuilder();
        String lineSeparator = System.lineSeparator();

        for (double[][] row : qtable) {
            for(double[] actions: row){
                    str.append(Arrays.toString(actions)).append(" | ");
            }
            str.append(lineSeparator);
        }

        return str.toString();
    }



}
