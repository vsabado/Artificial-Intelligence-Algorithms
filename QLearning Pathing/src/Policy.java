import java.util.Arrays;

public class Policy {
    char[][] policy;

    public Policy(int height, int width) {
        this.policy = new char[height][width];
    }

    public Policy(Qtable qtable){
        this(Policy.translate(qtable));
    }

    public Policy(Policy policy){
        https://stackoverflow.com/questions/1686425/copy-a-2d-array-in-java
        this.policy = new char[policy.getPolicy().length][];
        for(int i = 0; i < policy.getPolicy().length; i++)
            this.policy[i] = policy.getPolicy()[i].clone();
    }

    public char[][] getPolicy() {
        return policy;
    }

    public static Policy translate(Qtable qtable){
        Policy newPolicy = new Policy(Qtable.HEIGHT, Qtable.WIDTH);
        for(int i = 0; i < Qtable.HEIGHT; i++){
            for(int j = 0; j < Qtable.WIDTH; j++){
                //index of action with highest value
                double[] actionPoints = qtable.getQtable()[i][j];

                //Find the best action
                double bestAction = actionPoints[0];
                int bestActionIndex = 0;
                for(int k = 0; k < Qtable.NUM_ACTIONS; k++){
                    if( actionPoints[k] > bestAction){
                        bestAction = actionPoints[k];
                        bestActionIndex = k;
                    }
                }
                newPolicy.getPolicy()[i][j] = Action.indexToChar(bestActionIndex);
            }
        }

        return newPolicy;
    }



    public String toString(){
        StringBuilder str = new StringBuilder();
        String lineSeparator = System.lineSeparator();

        for (char[] row : policy) {
            str.append(Arrays.toString(row))
                    .append(lineSeparator);
        }

        return str.toString();
    }

}
