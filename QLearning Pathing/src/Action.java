import java.util.*;

public enum Action {
    up('^'), down('v'),left('<'),right('>');

    //https://stackoverflow.com/questions/1972392/pick-a-random-value-from-an-enum/14257525
    private static final List<Action> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();
    private char symbol;

    Action(char symbol){
        this.symbol = symbol;
    }


    public char getSymbol() {
        return symbol;
    }

    public static char indexToChar(int index){
        return values()[index].symbol;
    }


    public static Action getRandomAction(){
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
