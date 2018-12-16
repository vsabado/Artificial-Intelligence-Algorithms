import javax.sound.midi.SysexMessage;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class BreadthFirstSearch {

    private GameState startNode;
    private GameState goalNode; //Dummy Gamestate, all zero except Red
    private List<Point> blackSquares;
    private List<Shape> shapes;
    private Set<GameState> explored;

    public BreadthFirstSearch(GameState start, GameState goalNode, List<Point> blackSquares, List<Shape> shapes) {
        this.blackSquares = blackSquares;
        this.shapes = shapes;
        this.startNode = start;
        this.goalNode = goalNode;
    }

    public GameState compute() {

        //Case where the goalNode is the same as startNode
        if (startNode == goalNode || isGameStateTheGoal(startNode)) {
            System.out.println("Goal Node Found!");
            System.out.println(startNode);
        }

        Queue<GameState> queue = new LinkedList<>();
        explored = new TreeSet<>(new StateComparator()); //TreeSet used here for fast searching
        queue.add(this.startNode);
        queue.add(null);
        int depth = 0;
        System.out.println("Depth:" + depth);

        while (!queue.isEmpty()) {
            GameState current = queue.remove();
            //Added Depth for better code test
            //Keeping track of Depth for debugging.
            //Based on https://stackoverflow.com/questions/31247634/how-to-keep-track-of-depth-in-breadth-first-search
            if (current == null) {
                depth++;
                queue.add(null);
                System.out.println("Depth:" + depth);
                System.out.println("Nodes to check:" + queue.size());
                //For Debugging
//                System.out.println(queue);
                System.out.println("Nodes Explored:" + explored.size());
//                System.out.println(explored);
                if (queue.peek() == null) {
                    break; //Double null means we reached the end
                } else {
                    continue;
                }
            }

            if (explored.contains(current)) {
                continue;
            }
            if (isGameStateTheGoal(current)) {
                return current;
            } else {
                generateGameStates(queue, current);
            }
            explored.add(current);
        }

        return null;
    }

    private void writeList(List<GameState> state) {
        try {
            FileOutputStream fos = new FileOutputStream("explored.tmp");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(state);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isGameStateTheGoal(GameState current) {
        return current.getState()[0] == goalNode.getState()[0]
                && current.getState()[1] == goalNode.getState()[1];
    }

    public void generateGameStates(Queue<GameState> queue, GameState current) {
        //Current GameState is assumed valid
        //Create Board grid
        Set<Point> set = new HashSet<>(blackSquares);

        for (Shape shape : shapes) {
            for (Point point : shape.getPoints()) {
                Point tmp = new Point(point.getX() + current.getState()[2 * shape.getId()],
                        point.getY() + current.getState()[2 * shape.getId() + 1]);
                set.add(tmp);
            }
        }

        //Generate Valid states
        for (int i = 0; i < current.getState().length; i++) {
            //Move down and right
            byte[] newState = Arrays.copyOf(current.getState(), current.getState().length);
            newState[i]++; //state[i] = state[i] + 1;
            GameState tmp = new GameState(current, newState);

            if (!explored.contains(tmp)) {
                //Check validity of the new generate state
                int shapeIndex = i / 2;
                Shape tmpShape = shapes.get(shapeIndex);
                List<Point> originalPts = tmpShape.getPointByState(current);
                List<Point> newPts = tmpShape.getPointByState(tmp);
                boolean isInvalid = false;

                set.removeAll(originalPts);
                for (Point pt : newPts) {
                    if (set.contains(pt)) {
                        isInvalid = true;
                    }
                }
                set.addAll(originalPts);

                //If the generated state is valid, then add to queue for further checking. If invalid, ignore it.
                if (!isInvalid) {
                    queue.add(tmp);
                }
            }


            //Move up and left
            newState = Arrays.copyOf(current.getState(), current.getState().length);
            newState[i]--; //state[i] = state[i] - 1;
            tmp = new GameState(current, newState);

            if (!explored.contains(tmp)) {
                //Check validity of the new generate state
                int shapeIndex = i / 2;
                Shape tmpShape = shapes.get(shapeIndex);
                List<Point> originalPts = tmpShape.getPointByState(current);
                List<Point> newPts = tmpShape.getPointByState(tmp);
                boolean isInvalid = false;

                set.removeAll(originalPts);
                for (Point pt : newPts) {
                    if (set.contains(pt)) {
                        isInvalid = true;
                    }
                }
                set.addAll(originalPts);

                //If the generated state is valid, then add to queue for further checking. If invalid, ignore it.
                if (!isInvalid) {
                    queue.add(tmp);
                }
            }
        }
    }


}