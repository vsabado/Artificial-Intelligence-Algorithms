public class RewardTable extends Maze{


    public RewardTable() {
    }

    public RewardTable(String file) {
        setGoalValue(100);
        setObstableValue(-10);
        setStartValue(0);
        setEmptyValue(0);
        loadFile(file);
    }

    public double getReward(Point newState){
        return maze[newState.getRow()][newState.getColumn()];
    }

//    public static void main(String[] args){
//        System.out.println("Reward Table Test");
//        RewardTable board = new RewardTable("Board1.txt");
//        System.out.println(board);
//    }
}