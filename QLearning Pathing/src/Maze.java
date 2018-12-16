import javax.swing.text.Position;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Maze {

    //Use dimensions of 20 wide and 10 high.
    public static final int WIDTH = 10; //column
    public static final int HEIGHT = 20; //row


    //Modify to reflect point system
    protected double emptyValue = 0.0;
    protected double goalValue = 1.0;
    protected double obstableValue = -1.0;
    protected double startValue = 2.0;
    //0.0 means not visited
    //1.0 mean G or goal
    //-1.0 mean # or obstacle
    //2.0 mean S or start

    protected double[][] maze = new double[HEIGHT][WIDTH];

    private Point start;
    private Point end;

    public Maze(){

    }



    public Maze(String file){
        loadFile(file);
    }


    public void loadFile(String file){
        //https://stackoverflow.com/questions/4716503/reading-a-plain-text-file-in-java
        int currentHeight = 0;
        int currentWidth = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) {
                //Parsing
                String[] tokens = line.split(" ");
                for(String token : tokens){
                    switch(token){
                        case "G" : maze[currentHeight][currentWidth] = goalValue;
                            end  = new Point(currentHeight, currentWidth);
                            break;
                        case "#" : maze[currentHeight][currentWidth] = obstableValue; break;
                        case "0" : maze[currentHeight][currentWidth] = emptyValue; break;
                        case "S" : maze[currentHeight][currentWidth] = startValue;
                            start = new Point(currentHeight, currentWidth);
                            break;
                        default:
                            System.out.println("Invalid input check file!"); break;
                    }
                    currentWidth++;
                }
                line = br.readLine();
                currentHeight++;
                currentWidth = 0;
            }
        } catch(IOException e){
            System.err.println("Failed to open file " + e.getMessage());
        } finally {
            try {
                if(br != null){
                    br.close();
                }
            } catch (IOException e) {
                System.err.println("Failed to close file " + e.getMessage());
            }
        }
    }

    public String toString(){
        StringBuilder str = new StringBuilder();
        String lineSeparator = System.lineSeparator();

        for (double[] row : maze) {
            str.append(Arrays.toString(row))
                    .append(lineSeparator);
        }

        return str.toString();
    }

    public double get(Point point){
        return maze[point.getRow()][point.getColumn()];
    }


    public static int getWIDTH() {
        return WIDTH;
    }

    public static int getHEIGHT() {
        return HEIGHT;
    }

    public double getEmptyValue() {
        return emptyValue;
    }

    public void setEmptyValue(double emptyValue) {
        this.emptyValue = emptyValue;
    }

    public double getGoalValue() {
        return goalValue;
    }

    public void setGoalValue(double goalValue) {
        this.goalValue = goalValue;
    }

    public double getObstableValue() {
        return obstableValue;
    }

    public void setObstableValue(double obstableValue) {
        this.obstableValue = obstableValue;
    }

    public double getStartValue() {
        return startValue;
    }

    public void setStartValue(double startValue) {
        this.startValue = startValue;
    }


    public boolean isValid(Point current, Action action){
        if (current == null || action == null) {
            return false;
        }

        Point newLocation = current.doAction(action);

        if( newLocation.getColumn() < 0 || newLocation.getColumn() >= WIDTH ){
            return false;
        }
        if( newLocation.getRow() < 0 || newLocation.getRow() >= HEIGHT ){
            return false;
        }
        return true;
    }


    public List<Action> getValidMoves(Point current){
        List<Action> validActions = new ArrayList<>();
        for(Action action : Action.values()){
            if(isValid(current, action)){
                validActions.add(action);
            }
        }
        return validActions;
    }

    public double[][] getMaze() {
        return maze;
    }

    public void setMaze(double[][] maze) {
        this.maze = maze;
    }

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

//    public static void main(String[] args){
//        Maze board = new Maze("Board1.txt");
//        System.out.println(board);
//    }
}
