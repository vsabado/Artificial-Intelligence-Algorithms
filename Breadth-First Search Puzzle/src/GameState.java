import java.io.Serializable;
import java.util.*;

class GameState implements Serializable {
    private GameState prev;
    private byte[] state;
    private int numShapes;

    GameState(GameState prev, byte[] state) {
        //copy state
        this.prev = prev;
        this.state = Arrays.copyOf(state, state.length);
        this.numShapes = state.length / 2;
    }

    GameState(GameState prev, int numShapes) {
        this.numShapes = numShapes;
        this.prev = prev;
        state = new byte[numShapes * 2];
    }

    public static boolean isValidState(GameState gameState, List<Shape> shapes, List<Point> blackSquares) {
        Set<Point> set = new HashSet<>(blackSquares);

        for (Shape shape : shapes) {
            for (Point point : shape.getPoints()) {
                Point tmp = new Point(point.getX() + gameState.getState()[2 * shape.getId()],
                        point.getY() + gameState.getState()[2 * shape.getId() + 1]);
                if (set.contains(tmp)) {
                    return false;
                } else
                    set.add(tmp);
            }
        }
        return true;
    }

    public GameState getPrev() {
        return prev;
    }

    public void setPrev(GameState prev) {
        this.prev = prev;
    }

    public byte[] getState() {
        return state;
    }

    public void setState(byte[] state) {
        this.state = state;
    }

    public int getPathLength() {
        GameState current = this;
        int count = 0;
        while (current != null) {
            count++;
            current = current.getPrev();
        }
        return count;
    }

    public List<GameState> getPathList(){
        List<GameState> paths = new LinkedList<>();
        GameState current = this;
        while (current != null) {
            ((LinkedList<GameState>) paths).addFirst(current);
            current = current.getPrev();
        }
        return paths;
    }

    public void printPath() {
        List<GameState> path = getPathList();
        for(GameState state : path){
            System.out.println(state);
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numShapes; i++) {
            sb.append("(" + state[2 * i] + "," + state[2 * i + 1] + ") ");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameState gameState = (GameState) o;

        for (int i = 0; i < state.length; i++) {
            if (this.getState()[i] != gameState.getState()[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {

        int sum = 0;
        for (int i = 0; i < state.length; i++) {
            sum += state[i];
        }
        return sum;
    }
}