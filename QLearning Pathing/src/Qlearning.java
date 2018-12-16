import java.util.List;
import java.util.Random;

public class Qlearning {
    private double discountRate;
    private double learningRate;
    private double epsilonStart;
    private Qtable qTable;
    private RewardTable rewardTable;
    private Maze maze;
    private int numMoves = 0;


    public static final int NUMBER_ITERATIONS = 10000000;

    //https://en.wikipedia.org/wiki/Q-learning
    //Qnew = (1 - alpha)Qold + learningRate * (reward + discountfactor * estimateOfFutureVal)

    public Qlearning() {
        qTable = new Qtable();
        this.rewardTable = rewardTable;
        discountRate = 0.97;
        learningRate = 0.1;
        epsilonStart = 0.3;
    }

    public RewardTable getRewardTable() {
        return rewardTable;
    }

    public void setRewardTable(RewardTable rewardTable) {
        this.rewardTable = rewardTable;
    }

    public Maze getMaze() {
        return maze;
    }

    public void setMaze(Maze maze) {
        this.maze = maze;
    }

    public void train(Point start, Maze maze, RewardTable rewardTable, Double epsilon) {
        setMaze(maze);
        setRewardTable(rewardTable);
        Point currentState = start;
        Action action = null;
        Random rand = new Random();
        numMoves = 0;
        while(maze.get(currentState) != maze.getGoalValue() ){
            //System.out.println("Current Location: " + currentState.getRow() + "," + currentState.getColumn());
            if( rand.nextDouble() < epsilon){
                //Explore by making random move
                action = pickRandomAction(currentState, maze);
            }else{
                // Exploit (pick the best action)
                List<Action> actions = maze.getValidMoves(currentState);
                action = actions.get(0);
                for(Action act : actions){
                    if( qTable.get(currentState, act) > qTable.get(currentState, action)){
                        action = act;
                    }
                }

                //If no best move, choose random
                if( qTable.get(currentState,action) == 0.0){
                    action = pickRandomAction(currentState, maze);
                }
            }

            Point newState = currentState.doAction(action);
            double reward = rewardTable.getReward(newState);

            //Learn from that experience
            double qTableValue = (1 - learningRate) * qTable.get(currentState, action)
                    + learningRate*(reward + discountRate * maxValueMove(newState, maze));
            qTable.set(currentState, action, qTableValue);
            currentState = newState;
            numMoves++;
        }
        //Debugger
//        System.out.println(qTable);
//        System.out.println("NumMoves: " + numMoves);
    }


    public void learn(Maze maze, RewardTable rewardTable){
        setMaze(maze);
        setRewardTable(rewardTable);

        //for loop for however many iterations
        for( int i = 1; i <= NUMBER_ITERATIONS; i++){
            train(pickFixedStart(), maze, rewardTable, epsilonStart * (1 - (i)/Double.valueOf(NUMBER_ITERATIONS)));
        }

        System.out.println("Number of moves based on random start: " + numMoves);
        System.out.println("Final Qtable: ");
        System.out.println(qTable);

        System.out.println("Policy without the penalties shown explicitly: ");
        Policy policy = Policy.translate(qTable);
        System.out.println(policy);
        policy.policy[0][0] = 'S';
        policy.policy[17][9] = 'G';
        policy.policy[0][4] = '#';
        policy.policy[1][4] = '#';
        policy.policy[2][4] = '#';
        policy.policy[3][4] = '#';
        policy.policy[4][4] = '#';
        policy.policy[5][4] = '#';
        policy.policy[6][4] = '#';
        policy.policy[7][4] = '#';
        policy.policy[9][4] = '#';
        policy.policy[10][4] = '#';
        policy.policy[12][4] = '#';
        policy.policy[13][4] = '#';
        policy.policy[17][4] = '#';
        policy.policy[18][4] = '#';
        policy.policy[19][4] = '#';
        System.out.println("Policy with the penalties shown explicitly: ");
        System.out.println(policy);


    }

    private Point pickRandomStart(){
        Random rand = new Random();
        return new Point(rand.nextInt(Maze.getHEIGHT()), rand.nextInt(Maze.getWIDTH()));
    }

    private Point pickFixedStart(){
        return new Point(0, 0);
    }

    public double maxValueMove(Point state, Maze maze){
        List<Action> actions = maze.getValidMoves(state);

        double largest = qTable.get(state, actions.get(0));
        for(Action action : actions){
            double candidate = qTable.get(state, action);
            if( candidate > largest){
                largest = candidate;
            }
        }
        return largest;
    }


    public Action pickRandomAction(Point current, Maze maze){
        // Explore (pick a random action)
        // Check if valid
        Random rand = new Random();
        Point newLocation = new Point(current);
        List<Action> validActions = maze.getValidMoves(current);
        Action actionChosen = validActions.get(rand.nextInt(validActions.size()));
        return actionChosen;
    }



    public static void main(String[] args) {
        RewardTable rewardTable = new RewardTable("Board1.txt");
        Maze maze = new Maze("Board1.txt");
        Qlearning qlearning = new Qlearning();
        Point start = new Point(0,0);
        System.out.println("Going through " + qlearning.NUMBER_ITERATIONS + " number of iterations...");
        qlearning.learn(maze, rewardTable);
//        System.out.println(qlearning.qTable);
    }

}
