import java.util.*;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;

class Agent {

    Location destination;
    List<GameState> bestPathList = new ArrayList<>();
    PriorityQueue<GameState> frontier = new PriorityQueue<>();

    void printPathList(){
        for(int i = 0; i < bestPathList.size(); i++){
            System.out.println("At index " + i + " Path list x: " + bestPathList.get(i).getLocation().getX());
            System.out.println("At index " + i + " Path list y: " + bestPathList.get(i).getLocation().getY());
        }
    }




    void drawPlan(Graphics g, Model m) {
        g.setColor(Color.red);

        if (bestPathList == null || bestPathList.isEmpty()) {
            g.drawLine((int) m.getX(), (int) m.getY(), (int) m.getDestinationX(), (int) m.getDestinationY());
        } else {
            for (int i = 0; i < bestPathList.size() - 1; i++) {
                GameState current = bestPathList.get(i);
                GameState next = bestPathList.get(i + 1);
                g.drawLine((int) current.location.x, (int) current.location.y, (int) next.location.x, (int) next.location.y);
            }
        }

        g.setColor(Color.orange);
        for (GameState current : frontier) {
            g.fillOval((int) (current.getLocation().x / 10) * 10, (int) (current.getLocation().y / 10) * 10, 10, 10);
        }
    }

    void update(Model m) {
        Controller c = m.getController();
        while (true) {
            MouseEvent e = c.nextMouseEvent();

            if (e == null && destination == null) {
                break;
            } else {
                if (e != null) {
                    destination = new Location(e.getX(), e.getY());
                }
            }

            if (m.getDistanceToDestination(0) != 0) {
                break;
            } else {
                if(c.isLeft){
                    UniformCostSearch searchPath = new UniformCostSearch(m);
                    GameState bestPath = searchPath.findPath(new Location(m.getX(), m.getY()), destination);
                    bestPathList = bestPath.getPathList();
                    bestPathList.remove(0);
                    if (!bestPathList.isEmpty()) {
                        GameState newState = bestPathList.remove(0);
                        m.setDestination(newState.location.getX(), newState.location.getY());
                    }
                    frontier = searchPath.getFrontier();
                }else if(c.isRight){
                    AstarSearch searchPath = new AstarSearch(m);
                    GameState bestPath = searchPath.findPath(new Location(m.getX(), m.getY()), destination);
                    bestPathList = bestPath.getPathList();
                    printPathList();
                    bestPathList.remove(0);
                    if (!bestPathList.isEmpty()) {
                        GameState newState = bestPathList.remove(0);
                        m.setDestination(newState.location.getX(), newState.location.getY());
                    }
                    frontier = searchPath.getFrontier();
                }

            }
            break;
        }
    }

    public static void main(String[] args) throws Exception {
        Controller.playGame();
    }
}

class GameState {
    public float cost;
    public GameState parent;
    public Location location;
    public float heuristic;

    GameState(float cost, GameState par, Location location, float heuristic) {
        this.cost = cost;
        this.parent = par;
        this.location = location;
        this.heuristic = heuristic;
    }

    GameState(GameState s) {
        this.cost = s.cost;
        this.parent = s.parent;
        this.location = new Location(s.location.x, s.location.y);
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public GameState getParent() {
        return parent;
    }

    public void setParent(GameState parent) {
        this.parent = parent;
    }

    public Location getLocation() {
        return location;
    }

    public float getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(float heuristic) {
        this.heuristic = heuristic;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<GameState> getPathList() {
        List<GameState> path = new LinkedList<>();
        GameState current = this;
        while (current != null) {
            ((LinkedList<GameState>) path).addFirst(current);
            current = current.parent;
        }
        return path;
    }

}

class Location {
    float x;
    float y;

    Location(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}

class LocationComparator implements Comparator<Location> {

    @Override
    public int compare(Location o1, Location o2) {
        return Float.compare(o1.x, o2.x) == 0 ?
                Float.compare(o1.y, o2.y) : Float.compare(o1.x, o2.x);
    }
}

class StateLocationComparator implements Comparator<GameState> {
    LocationComparator locationComparator = new LocationComparator();

    public int compare(GameState a, GameState b) {
        return locationComparator.compare(a.getLocation(), b.getLocation());
    }
}

class CostComparator implements Comparator<GameState> {
    @Override
    public int compare(GameState o1, GameState o2) {
        return Float.compare(o1.cost, o2.cost);
    }


}

class CostHeuristicComparator implements Comparator<GameState> {
    @Override
    public int compare(GameState o1, GameState o2) {
        return Float.compare((o1.cost + o1.heuristic), (o2.cost + o2.heuristic));
    }
}

class UniformCostSearch {
    PriorityQueue<GameState> frontier;
    TreeSet<GameState> explored;
    Model model;
    private static double closeEnoughRange = 10.0;
    List<String> actions;

    public UniformCostSearch(Model model) {
        explored = new TreeSet<>(new StateLocationComparator());
        frontier = new PriorityQueue<>(new CostComparator());
        actions = new ArrayList<>(Arrays.asList("Right", "Left", "Up", "Down", "TopRight", "BottomRight", "TopLeft", "BottomLeft"));
        this.model = model;
    }


    public GameState findPath(Location start, Location end) {
        GameState startState = new GameState(0, null, start, 0);
        explored.add(startState);
        frontier.add(startState);
        while (!frontier.isEmpty()) {
            GameState front = frontier.poll();
            if (getDistance(front.getLocation(), end) < closeEnoughRange) {
                return front;
            }

            for (String action : actions) {
                generateChild(front, action);
            }
            //System.out.println("Call for UCS has been made!");
        }
        throw new RuntimeException("There is no path to the goal");
    }

    void generateChild(GameState current, String action) {
        GameState child = new GameState(current);
        float currentSpeed = model.getTravelSpeed(current.getLocation().x, current.getLocation().y);
        float actionCost = 0;

        switch (action) {
            case "Right":
                child.getLocation().x += 10;
                actionCost += (10 / currentSpeed);
                break;
            case "Left":
                child.getLocation().x -= 10;
                actionCost += (10 / currentSpeed);
                break;
            case "Up":
                child.getLocation().y -= 10;
                actionCost += (10 / currentSpeed);
                break;
            case "Down":
                child.getLocation().y += 10;
                actionCost += (10 / currentSpeed);
                break;
            case "TopRight":
                child.getLocation().x += 10;
                child.getLocation().y -= 10;
                actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
                break;
            case "BottomRight":
                child.getLocation().x += 10;
                child.getLocation().y += 10;
                actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
                break;
            case "TopLeft":
                child.getLocation().x -= 10;
                child.getLocation().y -= 10;
                actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
                break;
            case "BottomLeft":
                child.getLocation().x -= 10;
                child.getLocation().y += 10;
                actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
                break;
        }

        if (child.location.y >= 600) {
            return;
        } else if (child.location.y < 0) {
            return;
        }
        if (child.location.x >= 1200) {
            return;
        } else if (child.location.x < 0) {
            return;
        }

        if (explored.contains(child)) {
            GameState previousState = explored.floor(child);
            if (current.cost + actionCost < previousState.cost) {
                previousState.cost = current.cost + actionCost;
                previousState.parent = current;
            }
        } else {
            child.cost = current.cost + actionCost;
            child.parent = current;
            frontier.add(child);
            explored.add(child);
        }

    }


    public double getDistance(Location current, Location destination) {
        return Math.sqrt((destination.x - current.x) * (destination.x - current.x)
                + (destination.y - current.y) * (destination.y - current.y));
    }


    public PriorityQueue<GameState> getFrontier() {
        return frontier;
    }

    public void setFrontier(PriorityQueue<GameState> frontier) {
        this.frontier = frontier;
    }

    public TreeSet<GameState> getExplored() {
        return explored;
    }

    public void setExplored(TreeSet<GameState> explored) {
        this.explored = explored;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }



    public static double getCloseEnoughRange() {
        return closeEnoughRange;
    }

    public static void setCloseEnoughRange(double closeEnoughRange) {
        UniformCostSearch.closeEnoughRange = closeEnoughRange;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }
}

class AstarSearch extends UniformCostSearch {

    public AstarSearch(Model model) {
        super(model);
        frontier = new PriorityQueue<>(new CostHeuristicComparator());
    }

    @Override
    public GameState findPath(Location start, Location end){
        GameState startState = new GameState(0, null, start, 0);
        explored.add(startState);
        frontier.add(startState);
        while (!frontier.isEmpty()) {
            GameState front = frontier.poll();
            if (getDistance(front.getLocation(), end) < getCloseEnoughRange()) {
                return front;
            }

            for (String action : actions) {
                generateChild(front, action, end);
            }
            //System.out.println("Call for Astar has been made!");
        }
        throw new RuntimeException("There is no path to the goal");
    }

    void generateChild(GameState current, String action, Location end) {
        GameState child = new GameState(current);
        //Checking for out of bounds

        float currentSpeed = model.getTravelSpeed(current.getLocation().x, current.getLocation().y);
        float maxSpeed = model.getMaxSpeed();
        float actionCost = 0;

        switch (action) {
            case "Right":
                child.getLocation().x += 10;
                actionCost += (10 / currentSpeed);

                break;
            case "Left":
                child.getLocation().x -= 10;
                actionCost += (10 / currentSpeed);
                //child.heuristic = (float)getDistance(current.getLocation(), end) * maxSpeed;
                break;
            case "Up":
                child.getLocation().y -= 10;
                actionCost += (10 / currentSpeed);
                //child.heuristic = (float)getDistance(current.getLocation(), end) * maxSpeed;
                break;
            case "Down":
                child.getLocation().y += 10;
                actionCost += (10 / currentSpeed);
                //child.heuristic = (float)getDistance(current.getLocation(), end) * maxSpeed;
                break;
            case "TopRight":
                child.getLocation().x += 10;
                child.getLocation().y -= 10;
                actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
                //child.heuristic = (float)getDistance(current.getLocation(), end) * maxSpeed;
                break;
            case "BottomRight":
                child.getLocation().x += 10;
                child.getLocation().y += 10;
                actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
                //child.heuristic = (float)getDistance(current.getLocation(), end) * maxSpeed;
                break;
            case "TopLeft":
                child.getLocation().x -= 10;
                child.getLocation().y -= 10;
                actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
                //child.heuristic = (float)getDistance(current.getLocation(), end) * maxSpeed;
                break;
            case "BottomLeft":
                child.getLocation().x -= 10;
                child.getLocation().y += 10;
                actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
//                child.heuristic = (float)getDistance(current.getLocation(), end) * maxSpeed;
                break;
        }
        child.heuristic = (float)getDistance(child.getLocation(), end) / maxSpeed;

        if (child.location.y >= 600) {
            return;
        } else if (child.location.y < 0) {
            return;
        }
        if (child.location.x >= 1200) {
            return;
        } else if (child.location.x < 0) {
            return;
        }

        if (explored.contains(child)) {
            GameState previousState = explored.floor(child);
            if (current.cost + actionCost < previousState.cost) {
                previousState.cost = current.cost + actionCost;
                previousState.parent = current;
            }
        } else {
            child.cost = current.cost + actionCost;
            child.parent = current;
            frontier.add(child);
            explored.add(child);
        }

    }









}


