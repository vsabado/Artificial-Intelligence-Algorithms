import javax.xml.bind.SchemaOutputResolver;
import java.util.*;

class MyAI implements IAgent {
    protected int iter;

    //Game Related
    private Model model;

    //Targetting
    protected int targetIndex = -1;
    protected int nearestBoomTarget = -1;
    protected int[] attackCounters = new int[3];
    protected float prevX = 0;
    protected int wait = 0;
    protected Location targetLoc = null;

    //Filled with List<GameState>
    protected Astar astar;

    //Enable Flags
    protected boolean debuggingMode = false;
    protected boolean engaged = false;
    protected int mobilizationTime = 600;
    protected float ASTAR_THRESHOLD = 50;
    protected float energyThreshold = 0.4f;
    protected int numberOfAttacks = 4;
    protected float ZONING_MODIFIER = 0.4f;
    protected int GIVE_UP_MODIFIER = 0; //Default 5000
    protected int MOVE_PATH_THRESHOLD = 25; //75 isn't too bad
    protected float BATTLE_RANGE_SQUARE = (float) Math.pow(Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS + 200, 2);
    protected boolean AGGRESSIVE_MODE = false;

    //Match Info
    protected final static int NUMBER_OF_SPRITES = 3;
    protected Location enemyFlag = new Location(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
    protected Location myFlag = new Location(Model.XFLAG, Model.YFLAG);
    protected Location[] spritesStart = new Location[]{
            new Location(100, 100), new Location(100, 300), new Location(100, 500)
    };
    protected Location[] campPoints = getCampPoints();
    protected Object[] bestPathsToFlag = new Object[NUMBER_OF_SPRITES];
    protected Object[] bestPathsCurrentEnemy = new Object[NUMBER_OF_SPRITES];
    protected Object[] bestRetreatPath = new Object[NUMBER_OF_SPRITES];
    protected Location[] currentEnemy = new Location[NUMBER_OF_SPRITES];
    protected Location[] previousEnemy = new Location[NUMBER_OF_SPRITES];

    //Time Related
    protected Long TIME_BALANCE_PER_TURN = null;
    protected TimeBalanceManager timeBalanceManager;

    //Precalculate all important square distance
    protected float MAX_THROW_SQUARE = Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS;
    protected float MAX_RANGE_SQUARE = (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS);


    protected boolean[] healMode = new boolean[NUMBER_OF_SPRITES];


    public MyAI() {
        reset();
    }

    public MyAI(float zoning_modifier) {
        this();
        this.ZONING_MODIFIER = zoning_modifier;
    }

    @Override
    public void reset() {
        //Reset is very important. This is the only way to clear up new resources or your code will run very slow!
        iter = 0;
        //Game Related
        model = null;
        //Targetting
        targetIndex = -1;
        nearestBoomTarget = -1;
        attackCounters = new int[3];
        prevX = 0;
        wait = 0;
        targetLoc = null;

        //Filled with List<GameState>
        astar = null;

        //Enable Flags
        debuggingMode = false;
        engaged = false;
        AGGRESSIVE_MODE = false;

        //Match Info
        //Precalculate all important square distance
        bestPathsCurrentEnemy = new Object[NUMBER_OF_SPRITES];
        currentEnemy = new Location[NUMBER_OF_SPRITES];
        previousEnemy = new Location[NUMBER_OF_SPRITES];
        bestRetreatPath = new Object[NUMBER_OF_SPRITES];
        healMode = new boolean[NUMBER_OF_SPRITES];

        //Time Related
        TIME_BALANCE_PER_TURN = null;


        //Ask for garbage collection before game start to clear some resources for the fight
        System.gc();
    }

    //============================================
    //Main functions
    //============================================
    @Override
    public void update(Model m) {
        if (model == null) {
            model = m;
        }

        if (TIME_BALANCE_PER_TURN == null) {
            TIME_BALANCE_PER_TURN = m.getTimeBalance() / 20;
        }

        //long startTime = System.nanoTime();
        //Set up Astar
        if (astar == null) {
            astar = new Astar(m);
            preGeneratePathsToMap(m);
        }

        if(iter == 20){
            for(int i = 0; i < NUMBER_OF_SPRITES; i++){
                groupUp(m, i);
            }
        }

        //Get nearest enemy
        Result res = findNearestAliveOpponentToLocation(m, myFlag);

        for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
            //Check if they should be in heal mode in the first place
            checkValidHealMode(m,i);

            if (healMode[i]) {
                if (isDead(m, i)) {
                    continue;
                }
                //Dodge just in case
                //Retreat only if there is an opponent alive
                //getLocationB is null if none is alive
                if (res.getLocationB() != null) {
                    //Shoot at the nearest target if we can
                    //This usually means we are in danger.
                    Result enemy = findNearestAliveOpponentToLocation(m, Location.getLocation(m, i));
                    shootIfPossible(m, i, enemy.getLocationB());
                    //Pick retreat strategy. Retreat to base may be more useful and safer.
                    //retreatOptimal(m, i, 5, 10, res.getLocationB());
                    retreatToBase(m, i);
                }
                healMode(m, i);
            }
        }

        if (getAliveOpponent(m) != 0) {
            if (engaged) {
                timeToFight(m, res);
                debugPrint("Sprite" + ":" + "attack mode");
            }
            //Check if nearest enemy is within throwing distance(+some) to our flag. Then engage!
            else if (getSquareDistance(myFlag, targetLoc) <= MAX_RANGE_SQUARE + 100) {
                engaged = true;
            } else {
                wait++;
                if (wait > mobilizationTime) {
                    engaged = true;
                }
                debugPrint("Sprite" + ":" + "wait mode");
            }
            //}

        } else {
            //Case where all opponent are dead
            for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
                if (isDead(m, i)) {
                    continue;
                }
                if (m.getEnergySelf(i) >= energyThreshold) {
                    attackCounters[i] = numberOfAttacks;
                    debugPrint("Sprite" + i + ":" + "healed fully");
                }

                if (attackCounters[i] != 0) {
                    shootAtTarget(m, i, enemyFlag, true); //Set first person to see this line as camper
                    debugPrint("Sprite" + i + ":" + "attack flag mode");
                } else {
                    //do nothing and rest
                    stayInPlace(m, i);
                    debugPrint("Sprite" + i + ":" + "heal mode");
                }
            }
        }
        iter++;
//        Long endTime = System.nanoTime();
//
//        if ((endTime - startTime) > TIME_BALANCE_PER_TURN) {
//            debugPrint("Went over time_balance_per_turn: " + (endTime - startTime));
//        }

    }

    public void checkValidHealMode(Model m, int index) {
        //Disable heal mode when alone, all is in heal mode, or healed, or near flag
        if (getAliveSprites(m) == 1 || isAllInHealMode() || m.getEnergySelf(index) > 0.8f ||
                getDistance(Location.getLocation(model, index), myFlag) < 50) {
            healMode[index] = false;
        }
    }


    public void healMode(Model m, int index) {
        //Don't go in heal mode if alone, everyone is heading to heal. One must stay. Or if healed
        if (getAliveSprites(m) == 1 || isAllInHealMode() || m.getEnergySelf(index) > 0.8f) {
            healMode[index] = false;
        } else {
            //Check if too close to opponent
            Location myLocation = Location.getLocation(m, index);
            Result res = findNearestAliveOpponentToLocation(m, myLocation);
            float safeZone = Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS + 40;
            if (res.getLocationB() == null || getSquareDistance(myLocation, res.getLocationB()) >= Math.pow(safeZone, 2)) {
                //Going here means its already safe
                stayInPlace(m, index);
            } else {
                //Find best retreat location that is farthest
                retreatToBase(m, index);
            }
        }
    }

    public Location retreatOptimal(Model model, int index, float offset, int simTime, Location closestEnemy) {
        float[] bestX = new float[NUMBER_OF_SPRITES];
        float[] bestY = new float[NUMBER_OF_SPRITES];
        float[] farthest = new float[NUMBER_OF_SPRITES];
        Location myLocation = Location.getLocation(model, index);
        for (int sim = 0; sim < 8; sim++) { // try 8 candidate destinations
            float[] xValues = new float[NUMBER_OF_SPRITES];
            float[] yValues = new float[NUMBER_OF_SPRITES];
            for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
                //Don't move or undesired sprites
                if (isDead(model, i) || index != i) {
                    xValues[i] = model.getX(i);
                    yValues[i] = model.getY(i);
                    continue;
                }

                //Generate all the directions in the unit circle based on "sim" index of the for loop. Think of it like North,South,West,East
                //NE,NW,SE,SW etc. And create a new location that is sized based on Model.BLAST_RADIUS + offset. Which means is out of the danger zone.
                //ex: pi/2 is 90 degrees
                xValues[i] = (float) (model.getX(i) + (Math.cos((Math.PI * sim) / 4) * (Model.BLAST_RADIUS + offset)));
                yValues[i] = (float) (model.getY(i) + (Math.sin((Math.PI * sim) / 4) * (Model.BLAST_RADIUS + offset)));
            }

            // Fork the universe and simulate it for simulation time-steps
            Controller forkController = model.getController().fork(new Shadow(xValues[0], yValues[0], xValues[1], yValues[1], xValues[2], yValues[2]), new Shadow());
            Model modelFork = forkController.getModel();
            for (int j = 0; j < simTime; j++) {
                modelFork.update();
            }

            for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
                float distance = (float) getDistance(new Location(modelFork.getX(i), modelFork.getY(i)), closestEnemy);
                if (distance > farthest[i]) {
                    bestX[i] = modelFork.getX(i);
                    bestY[i] = modelFork.getY(i);
                    farthest[i] = distance;
                }
            }

        }

        for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
            model.setDestination(i, bestX[i], bestY[i]);
        }

        return new Location(bestX[index], bestY[index]);
    }


    /**
     * Create a retreat point to flag and follow it.
     *
     * @param model Model
     * @param index sprite index
     * @return Return the location to head to. This function already sends the command.
     */
    public Location retreatToBase(Model model, int index) {
        Location myLocation = Location.getLocation(model, index);

        //If path does not exist create.
        if (bestRetreatPath[index] == null) {
            bestRetreatPath[index] = astar.getBestPaths(myLocation, myFlag);
        }

        List<GameState> retreatPath = (List<GameState>) bestRetreatPath[index];
        //Since we know that X value decreases as we get closer to our flag, we can just search the X location in decreasing order
        //The assumption here is that a path exist.
        float distanceToPoint = (float) getDistance(myLocation, retreatPath.get(0).getLocation());

        Location nextPath = null;
        float distanceThreshold = 25;
        for (int i = 0; i < retreatPath.size(); i++) {
            float currentDistance = (float) getDistance(myLocation, retreatPath.get(i).getLocation());
            if (currentDistance > distanceThreshold) {
                nextPath = retreatPath.get(i).getLocation();
            }
        }

        //If nextPath is null here, that means we are near are our flag. We can just move back a bit in that case
        if (nextPath == null) {
            //model.setDestination(index, 0, 0); //Head to the corner
            //Can't do anything here. Just dodge
            avoidBombs(model, index);
        } else {
            moveSprites(index, model, nextPath, false);
        }

        return nextPath;
    }


    public boolean isAllInHealMode() {
        return healMode[0] && healMode[1] && healMode[2];
    }

    public void timeToFight(Model m, Result res) {
        boolean isAStar = true;
        Location targetLocation = res.getLocationB();
        for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
            if (isDead(m, i)) {
                continue;
            }
            Location currentLocation = new Location(m.getX(i), m.getY(i));

            //If close enough
            float enemySquareDistance = getSquareDistance(currentLocation, targetLocation);
            float enemyFlagSquareDistance = getSquareDistance(currentLocation, enemyFlag);

            //Once we distance from nearest and flag. Prioritize the nearest one.
            float nearestSquare = Float.MIN_VALUE;
            boolean isEnemy = false;

            //When the flag is about to die, just focus it
            if (m.getFlagEnergyOpponent() < 0.3f) {
                shootAtTarget(m, i, enemyFlag, true); //shoot at flag
            } else if (enemySquareDistance <= enemyFlagSquareDistance) {
                nearestSquare = enemySquareDistance;
                isEnemy = true;
            } else {
                //If flag is closer, we target the flag instead
                nearestSquare = enemyFlagSquareDistance;
                targetLocation = enemyFlag;
            }

            if (currentEnemy[i] == null) {
                currentEnemy[i] = targetLocation;
            } else {
                previousEnemy[i] = currentEnemy[i];
                currentEnemy[i] = targetLocation;
            }

            //Zoning
            float closeEnoughDistance = Model.MAX_THROW_RADIUS + (Model.BLAST_RADIUS * ZONING_MODIFIER);

            //Nearest is within range, we attack
            if (nearestSquare <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
                //ShootAtTarget assume that TargetLoc is set, so we'll set the target
                shootAtTarget(m, i, targetLocation, false);
                avoidBombs(m, i);
            }
            //If nearest is an enemy and not within range, we try to zone them at a certain distance
            //This calculation uses the sqrt, hence we do not square the other side
            else if (isEnemy && Math.sqrt(enemySquareDistance) <= closeEnoughDistance) {
                //This is zoning code
                //Location furthestRange = getFurthestRange(currentLocation, targetLoc, (float) (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS * (0.9)));
                //Otherwise try to get them in the blast radius anyhow
                float dx = targetLocation.getX() - currentLocation.getX();
                float dy = targetLocation.getY() - currentLocation.getY();
                float scale = (float) (Model.MAX_THROW_RADIUS / Math.sqrt(nearestSquare));
                float throwX = dx * scale + m.getX(i);
                float throwY = dy * scale + m.getY(i);

                shoot(m, i, new Location(throwX, throwY));
                avoidBombs(m, i);
            } else if (isEnemy && enemySquareDistance <= BATTLE_RANGE_SQUARE) {

                if (m.getEnergySelf(i) < energyThreshold) {
                    healMode[i] = true;
                    debugPrint("Healing");
                } else {
                    debugPrint("Move Sprites");
                    moveSprites(i, m, targetLocation, true);
                }
                avoidBombs(m, i);
            } else {
                List<GameState> bestPaths = (List<GameState>) bestPathsCurrentEnemy[i];

                //Call A* once, then just follow the path.
                if (bestPathsCurrentEnemy[i] == null || bestPaths.isEmpty()) {
                    moveSprites(i, m, targetLocation, true);
                } else {
                    //long startTime = System.nanoTime();
                    //If new target is within MOVE_PATH_THRESHOLD, don't recalculate
                    if (bestPathsCurrentEnemy[i] != null && currentEnemy[i] != null
                            && !bestPaths.isEmpty()) {
                        //Run extension if needed
                        moveSpriteFollowExtend(m, i, targetLocation);
                        //Use follow path
                        moveSpriteFollow(m, i, (List<GameState>) bestPathsCurrentEnemy[i]);
                    }
                    //long endTime = System.nanoTime();
                }

            }
            //Do avoid optimal during fight, healMode don't do this
            avoidOptimal(m, 5.0f, 10);
        }

    }

    //============================================
    //Move
    //============================================

    /**
     * Move the sprite using A*
     *
     * @param i           sprite index
     * @param m           Model
     * @param destination destination location
     * @param savePath    Boolean flag whether to save the generated path as best path.
     * @return List of locations towards the path
     */
    public List<GameState> moveSprites(int i, Model m, Location destination, boolean savePath) {
        if (i >= NUMBER_OF_SPRITES) {
            return null;
        }
        GameState bestPath = astar.findPath(new Location(m.getX(i), m.getY(i)), destination);
        List<GameState> bestPathList = null;
        if (bestPath == null) {
            //Do nothing
            debugPrint("No best path");
        } else {
            bestPathList = bestPath.getPathList();
            //Remove zero because it's the current location
            bestPathList.remove(0);

            if (savePath) {
                //Set enemy current path list
                bestPathsCurrentEnemy[i] = bestPathList;
            }
            if (!bestPathList.isEmpty()) {
                GameState newState = bestPathList.remove(0);
                m.setDestination(i, newState.location.getX(), newState.location.getY());
            }
        }

        return bestPathList;
    }


    /**
     * Retreat and heal if possible. Based on safe distance. If unsafe, returns false.
     *
     * @param m     Model
     * @param index sprite index
     * @return Returns true is successful.
     */
    public boolean retreatAndHeal(Model m, int index) {
        Location myLocation = new Location(m.getX(index), m.getY(index));
        Result res = findNearestAliveOpponentToLocation(m, myLocation);

        //If nearest opponent is too close to our base, we must fight
        if (getSquareDistance(myFlag, res.getLocationB()) < Math.pow(Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS + 50, 2)) {
            return false;
        } else {
            //If the opponent is far away from base, we can afford to retreat
            return true;
        }
    }

    public void retreatAndHeal(Model m, int i, float enemyDistance) {
        m.setDestination(i, Model.XFLAG, Model.YFLAG);
        if (enemyDistance > Math.pow(Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS + 5.0f, 2)) {
            stayInPlace(m, i);
        }
    }

    /**
     * Calculates the endpoint of an extended line segment by length. Given an existing line segment.
     *
     * @param a      start location
     * @param b      end location
     * @param length length to extend the line segment
     * @return return the endpoint of the extended line segment
     */
    public Location getFurthestRange(Location a, Location b, float length) {
        //Otherwise try to get them in the blast radius anyhow
        //https://stackoverflow.com/questions/7740507/extend-a-line-segment-a-specific-distance
        float dx = b.getX() - a.getX();
        float dy = b.getY() - a.getY();
        float lenAB = (float) getDistance(a, b);
        float extensionX = dx * length / lenAB;
        float extensionY = dy * length / lenAB;
        return new Location(a.getX() + extensionX, a.getY() + extensionY);
    }

    public void moveSpritePregen(int index, Model m, List<GameState> paths) {

        Location spriteLocation = new Location(m.getX(index), m.getY(index));

        //If it is close enough end location, just run send the last point, saves time when it is close
        Location endLocation = paths.get(paths.size() - 1).getLocation();
        if (getDistance(spriteLocation, endLocation) <= 10f) {
            //This check is here to skip the long search if its close enough
            m.setDestination(index, endLocation.getX(), endLocation.getY());
            return;
        }
        float pathThreshold = 15f;
        int startPointIndex = 0;

        GameState previous = paths.get(0);
        for (int i = 0; i < paths.size(); i++) {
            GameState currentGamestate = paths.get(i);
            if (spriteLocation.getX() < currentGamestate.getLocation().getX()) {
                startPointIndex = i;
                float pregenThresholdDistance = 20;
                if (getDistance(spriteLocation, currentGamestate.getLocation()) <= pregenThresholdDistance) {
                    m.setDestination(index, currentGamestate.getLocation().getX(), currentGamestate.getLocation().getY());
                } else {
                    moveSprites(index, m, currentGamestate.getLocation(), false);
                }
                return;
            }
            previous = currentGamestate;
        }
        //Path not close enough for some reason, just go to flag
    }


    /**
     * Note that this modifies the passed in path
     */
    public void moveSpriteFollow(Model m, int index, List<GameState> paths) {
        if (paths.isEmpty()) {
            System.out.printf("Code Error, this shouldn't be empty at this call!");
        }

        Location nextPath = null;

        Location spriteLocation = new Location(m.getX(index), m.getY(index));
        List<GameState> currentBestPaths = (List<GameState>) bestPathsCurrentEnemy[index];

        if (paths.size() < 2) {
            nextPath = currentBestPaths.get(0).getLocation();
            m.setDestination(index, nextPath.getX(), nextPath.getY());
            return;
        }

        boolean isIncreasing = false;
        //Predetermine is increasing or decreasing distance
        //Assumed that size greater than 1
        float distance1 = (float) getDistance(spriteLocation, paths.get(0).getLocation());
        float distance2 = (float) getDistance(spriteLocation, paths.get(1).getLocation());
        if (distance1 < distance2) {
            isIncreasing = true;
        }

        //Get closest index location
        float shortestDistance = distance1;
        int shortestIndex = 0;


        if (isIncreasing) {
            //Since the path is increasing, we should use the first location
            float currentDistance = (float) getDistance(spriteLocation, currentBestPaths.get(0).getLocation());

            //Make sure the location is far enough, if not trim it
            while (currentDistance < 15f) {
                currentBestPaths.remove(0);
                currentDistance = (float) getDistance(spriteLocation, currentBestPaths.get(0).getLocation());
            }
            nextPath = currentBestPaths.get(0).location;
        } else {
            for (int i = 1; i < paths.size(); i++) {
                float newDistance = (float) getDistance(spriteLocation, paths.get(i).getLocation());
                if (shortestDistance > newDistance) {
                    shortestDistance = newDistance;
                    shortestIndex = index;
                } else {
                    //Previous shortest distance is the best location
                    nextPath = paths.get(shortestIndex).getLocation();
                    //sublist is fromIndex inclusive, toIndex exclusive
                    currentBestPaths = currentBestPaths.subList(shortestIndex, currentBestPaths.size());
                    break;
                }
            }
        }

        //Check if the start paths first location is too far
        //If further than 10f, recalculate and append to the beginning the calculated path

        //We want the best paths so we recalculate using A* based on threshold
        float distanceThreshold = 15;
        int j = 0;
        for (; j < currentBestPaths.size(); j++) {
            Location tmpLocation = currentBestPaths.get(j).location;
            if (getDistance(spriteLocation, tmpLocation) > distanceThreshold) {
                break;
            }
        }
        //Trim out the earlier ones so we can get the best path
        currentBestPaths = currentBestPaths.subList(j, currentBestPaths.size());

        List<GameState> preList = astar.getBestPaths(spriteLocation, currentBestPaths.get(0).getLocation());
        //Remove the first location because it is the start
        preList.remove(0);

        //Append the first location
        preList.addAll(currentBestPaths);
        currentBestPaths = preList;
        bestPathsCurrentEnemy[index] = currentBestPaths;

        //Get next path, and remove from the path
        nextPath = currentBestPaths.remove(0).getLocation();
        m.setDestination(index, nextPath.getX(), nextPath.getY());
    }


    /**
     * Extend the best path if the enemy is too far! with X value larger.
     */
    private void moveSpriteFollowExtend(Model m, int index, Location newTarget) {
        List<GameState> bestPaths = (List<GameState>) bestPathsCurrentEnemy[index];
        if (bestPaths.isEmpty()) {
            System.out.println("Code Error paths shouldn't be empty here");
            return;
        }

        boolean isNewPathFurther = false;
        Location myLocation = new Location(m.getX(index), m.getY(index));
        Location previousEnd = bestPaths.get(bestPaths.size() - 1).location;
        float previousDistance = (float) getDistance(myLocation, previousEnd);
        float newDistance = (float) getDistance(myLocation, newTarget);
        float calculateDistanceThreshold = 20f;

        //Check if we even need to recalculate
        if (Math.abs(previousDistance - newDistance) < calculateDistanceThreshold) {
            return;
        }

        if (previousDistance < newDistance) {
            isNewPathFurther = true;
        }

        if (isNewPathFurther) {
            List<GameState> postList = astar.getBestPaths(previousEnd, newTarget);
            //Remove first because its the same as last
            postList.remove(0);
            bestPaths.addAll(postList);
        } else {
            Location currentLocation = null;
            for (int i = bestPaths.size() - 1; i >= 0; i--) {
                currentLocation = bestPaths.get(i).location;
                if (getDistance(currentLocation, newTarget) < calculateDistanceThreshold) {
                    bestPaths.remove(i);
                }
            }


            //Now see how much to calculate for A*
            if (!bestPaths.isEmpty()) {
                //This is the case if the new target is too far, but there are still have some close ones on the list
                //We append the path from the end
                previousEnd = bestPaths.get(bestPaths.size() - 1).location;
                List<GameState> postList = astar.getBestPaths(previousEnd, newTarget);
                //Remove first because its the same as last
                postList.remove(0);

                bestPaths.addAll(postList);

            } else {
                //No location on path within the threshold, recalculate
                bestPaths = moveSprites(index, m, newTarget, true);
            }
        }

        bestPathsCurrentEnemy[index] = bestPaths;
    }


    //============================================
    //Battle Functions
    //============================================

    /**
     * Finds the nearest oppponent in reference to a location
     *
     * @param m        Model
     * @param location current location
     * @return Returns result, containing square distance
     */
    public Result findNearestAliveOpponentToLocation(Model m, Location location) { //Pass my flag's x and y here
        float dd = Float.MAX_VALUE;
        //Use the old one just in case there is currently none.
        Location nearestOpponent = null;
        for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
            //If opponent is dead, skip
            if (m.getEnergyOpponent(i) < 0) {
                continue;
            }
            Location opponentLocation = new Location(m.getXOpponent(i), m.getYOpponent(i));

            //Target the opponent nearest to our flag
            float d = (float) getSquareDistance(location, opponentLocation);
            if (d < dd) {
                dd = d;
                targetIndex = i;
                nearestOpponent = opponentLocation;
                targetLoc = nearestOpponent;
            }
        }
        return new Result(dd, targetIndex, location, nearestOpponent);
    }

    /**
     * Gets the location and the distance from the nearest opponent
     *
     * @param m   Model
     * @param loc current location
     * @return Result containing the location and the distance
     */
    private Result findNearestOpponent(Model m, Location loc) {
        Location closestLocation = new Location(m.getXOpponent(0), m.getYOpponent(0));
        double closestDistance = getDistance(closestLocation, loc);
        int index = 0;
        for (int i = 1; i < NUMBER_OF_SPRITES; i++) {
            Location opponentLocation = new Location(m.getXOpponent(i), m.getYOpponent(i));
            double newDistance = getDistance(opponentLocation, loc);
            if (newDistance < closestDistance) {
                closestDistance = newDistance;
                closestLocation = opponentLocation;
                index = i;
            }
        }
        return new Result((float) closestDistance, index, loc, closestLocation);
    }

    public void shoot(Model m, int index, Location target) {
        //Throw Bomb Notes:
        //Shoot at the target location,
        //If target location is further than the MAX_THROW_DISTANCE, it throws it at the furthest location possible
        m.throwBomb(index, target.getX(), target.getY());
    }

    public float nearestBombTarget(Model m, float x, float y) {
        float dd = Float.MAX_VALUE;
        for (int i = 0; i < m.getBombCount(); i++) {
            float d = getSquareDistance(x, y, m.getBombTargetX(i), m.getBombTargetY(i));
            if (d < dd) {
                dd = d;
                nearestBoomTarget = i;
            }
        }
        return dd;
    }

    /**
     * This method find the nearest bomb target based on passed in location.
     *
     * @param m        Model
     * @param location reference location
     * @return Returns the square distance from the nearest bomb
     */
    public Result nearestBombTargetToLocation(Model m, Location location) {
        float dd = Float.MAX_VALUE;
        int nearest = nearestBoomTarget;
        Location nearestBombTarget = null;

        for (int i = 0; i < m.getBombCount(); i++) {
            Location bombTarget = new Location(m.getBombTargetX(i), m.getBombTargetY(i));
            float d = (float) getSquareDistance(location, bombTarget);
            if (d < dd) {
                dd = d;
                nearestBoomTarget = i;
                nearestBombTarget = bombTarget;
            }
        }
        return new Result(dd, nearestBoomTarget, location, nearestBombTarget);
    }

    public void shootAtTarget(Model m, int i, Location targetLocation, boolean isFlag) {
        Location myLocation = new Location(m.getX(i), m.getY(i));
        if (isFlag) {
            debugPrint("Moving index " + i + " using A*");
            //moveSprites(i, m, campPoints[i]);
            targetLocation = enemyFlag;

            //Check if within range of flag.
            if (getSquareDistance(targetLocation, myLocation) <= MAX_THROW_SQUARE) {
                //ATTACK!!
                m.throwBomb(i, targetLocation.getX(), targetLocation.getY());
                attackCounters[i]--;
            } else {
                //Move closer to enemy flag
                //Change index to make all of the sprites follow a specific path, otherwise use i to have all sprites use their own paths.
                moveSpritePregen(i, m, (List<GameState>) bestPathsToFlag[1]); //Everyone uses the middle
            }

            return;
        }
        if (targetIndex >= 0) {
            //It only attacks if you have advantage and opponent is alive.
            if (m.getEnergySelf(i) >= m.getEnergyOpponent(targetIndex) && isOpponentAlive(m, targetIndex)) {

                // Get close enough to throw a bomb at the enemy
                float dx = m.getX(i) - targetLocation.getX();
                float dy = m.getY(i) - targetLocation.getY();
                float t = 1.0f / Math.max(Model.EPSILON, (float) Math.sqrt(dx * dx + dy * dy));
                dx *= t;
                dy *= t;

                //Move slightly after attacking
                m.setDestination(i, targetLocation.getX() + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), targetLocation.getY() + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));

                // Throw bombs
                if (getSquareDistance(myLocation, targetLocation) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
                    m.throwBomb(i, targetLoc.getX(), targetLoc.getY());
                    avoidBombs(m, i);
                }
            }
        }
    }


    /**
     * This is function that shoot if possible considering current health and distance to enough
     *
     * @param m              Model
     * @param idx            sprite index
     * @param targetLocation Target location to shoot
     */
    public void shootIfPossible(Model m, int idx, Location targetLocation) {
        Location myLocation = new Location(m.getX(idx), m.getY(idx));
        float healthThreshold = 0;
        if (m.getEnergySelf(idx) > healthThreshold && getSquareDistance(targetLocation, myLocation) <= MAX_THROW_SQUARE) {
            m.throwBomb(idx, targetLocation.getX(), targetLocation.getY());
        }
    }


    /**
     * Avoid bombs or Heal
     *
     * @param m Model
     * @param i sprite index
     */
    public void avoidBombs(Model m, int i) {
        Location myLocation = new Location(m.getX(i), m.getY(i));
        Result res = nearestBombTargetToLocation(m, myLocation);

        //If the nearest bomb target is close enough to my location
        //res.getValue() here is the square distance
        if (res.getValue() <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
            debugPrint("Bomb in range!");
            Location nearestBombLocation = res.getLocationB();
            float dx = myLocation.getX() - nearestBombLocation.getX(); //Maybe needs to be more dynamic
            float dy = myLocation.getY() - nearestBombLocation.getY();
            if (dx == 0 && dy == 0)
                dx = 1.0f;
            m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);
        }
    }


    /**
     * Dodge for best health.
     *
     * @param model
     * @param offset
     * @param simTime
     */
    public void avoidOptimal(Model model, float offset, int simTime) {
        float[] bestX = new float[NUMBER_OF_SPRITES];
        float[] bestY = new float[NUMBER_OF_SPRITES];
        float[] bestHealth = new float[NUMBER_OF_SPRITES];

        //Initialize health
        for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
            bestHealth[i] = model.getEnergySelf(i);
        }
        Map<Integer, List<Location>> bestLocations = new HashMap<>();

        //This is the order the unit circle is set. This is added so that I can prioritize certain directions.
        //Back 5,7,6
        //Left 4
        //Right 0
        //Forward 2,3,1
        int[] directinOrder = {2, 3, 1, 0, 4, 5, 7, 6};

        //Simulate
        for (int sim = 0; sim < 9; sim++) { // try 9 candidate destinations, last is to stay.
            float[] xValues = new float[NUMBER_OF_SPRITES];
            float[] yValues = new float[NUMBER_OF_SPRITES];
            for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
                if (isDead(model, i)) {
                    xValues[i] = model.getX(i);
                    yValues[i] = model.getY(i);
                }

                //Generate all the directions in the unit circle based on "sim" index of the for loop. Think of it like North,South,West,East
                //NE,NW,SE,SW etc. And create a new location that is sized based on Model.BLAST_RADIUS + offset. Which means is out of the danger zone.
                //ex: pi/2 is 90 degrees
                if (sim == 8) {
                    //Don't move
                    xValues[i] = model.getX(i);
                    yValues[i] = model.getY(i);
                } else {
                    //Directions
                    xValues[i] = (float) (model.getX(i) + (Math.cos((Math.PI * directinOrder[sim]) / 4) * (Model.BLAST_RADIUS + offset)));
                    yValues[i] = (float) (model.getY(i) + (Math.sin((Math.PI * directinOrder[sim]) / 4) * (Model.BLAST_RADIUS + offset)));
                }
            }

            // Fork the universe and simulate it for simulation time-steps
            Controller forkController = model.getController().fork(new Shadow(xValues[0], yValues[0], xValues[1], yValues[1], xValues[2], yValues[2]), new Shadow());
            Model modelFork = forkController.getModel();
            for (int j = 0; j < simTime; j++) {
                modelFork.update();
            }

            for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
                if (modelFork.getEnergySelf(i) >= bestHealth[i]) {
                    bestX[i] = modelFork.getX(i);
                    bestY[i] = modelFork.getY(i);
                    bestHealth[i] = modelFork.getEnergySelf(i);
                }
            }
        }

        for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
            if (isDead(model, i)) {
                continue;
            }

            //If we are going to get hit no matter, what we may as well just fight forward
            if (bestHealth[i] < model.getEnergySelf(i)) {
                float xValues = (float) (model.getX(i) + (Math.cos((Math.PI * 2) / 4) * (Model.BLAST_RADIUS + offset)));
                float yValues = (float) (model.getY(i) + (Math.sin((Math.PI * 2) / 4) * (Model.BLAST_RADIUS + offset)));
                Location forward = new Location(xValues, yValues);
                moveSprites(i, model, forward, false);
            } else {
                //We won't get hit
                //Let's dodge to best place
                model.setDestination(i, bestX[i], bestY[i]);
            }

        }
    }

    //============================================
    //Checks
    //============================================
    public boolean isDead(Model m, int i) {
        return m.getEnergySelf(i) <= 0;
    }

    public boolean isOpponentAlive(Model m, int targetIndex) {
        return m.getEnergyOpponent(targetIndex) > 0;
    }

    //============================================
    //Utility
    //============================================
    public void stayInPlace(Model m, int index) {
        //Stay in place
        m.setDestination(index, m.getX(index), m.getY(index));
    }

    public Location[] getCampPoints() {
        Location campPoint0 = new Location(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS / 2, Model.YFLAG_OPPONENT);
        Location campPoint1 = new Location(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS / 2, Model.YFLAG_OPPONENT + Model.MAX_THROW_RADIUS / 2);
        Location campPoint2 = new Location(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT + Model.MAX_THROW_RADIUS / 2);
        return new Location[]{campPoint0, campPoint1, campPoint2};
    }

    public void preGeneratePathsToMap(Model m) {
        //https://stackoverflow.com/questions/529085/how-to-create-a-generic-array-in-java
        for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
            bestPathsToFlag[i] = astar.getBestPaths(spritesStart[i], campPoints[i]);
        }
    }

    public int getAliveOpponent(Model m) {
        int alive = 0;
        for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
            //Counts the number of alive opponents
            if (m.getEnergyOpponent(i) > 0) {
                // don't care about dead opponents
                alive++;
            }
        }
        return alive;
    }

    public int getAliveSprites(Model m) {
        int alive = 0;
        for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
            //Counts the number of alive opponents
            if (m.getEnergySelf(i) > 0) {
                // don't care about dead opponents
                alive++;
            }
        }
        return alive;
    }


    public void debugPrint(String str) {
        if (debuggingMode) {
            System.out.println(str);
        }
    }

    public void groupUp(Model m, int i) {
        m.setDestination(i, 100, 300);
    }

    public TimeBalanceManager getTimeBalanceManager() {
        return timeBalanceManager;
    }


    public static float getSquareDistance(float x1, float y1, float x2, float y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    public static float getSquareDistance(Location a, Location b) {
        return ((a.getX() - b.getX()) * (a.getX() - b.getX())) + ((a.getY() - b.getY()) * (a.getY() - b.getY()));
    }

    public static double getDistance(Location current, Location destination) {
        return Math.sqrt((destination.getX() - current.getX()) * (destination.getX() - current.getX())
                + (destination.getY() - current.getY()) * (destination.getY() - current.getY()));
    }


    //======================================================================================================================
//A* Search
//======================================================================================================================
    static class Shadow implements IAgent {

        Location[] locations = null;

        public Shadow() {

        }

        public Shadow(float x0, float y0, float x1, float y1, float x2, float y2) {
            locations = new Location[NUMBER_OF_SPRITES];
            locations[0] = new Location(x0, y0);
            locations[1] = new Location(x1, y1);
            locations[2] = new Location(x2, y2);
        }

        @Override
        public void reset() {
            //iter is private
        }

        @Override
        public void update(Model m) {
            if (locations != null) {
                for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
                    m.setDestination(i, locations[i].getX(), locations[i].getY());
                }
            }
        }
    }

    static class Astar {

        private PriorityQueue<GameState> frontier;
        private TreeSet<GameState> explored;
        private Model model;
        private static double closeEnoughRange = 10.0;
        private List<String> actions;
        float maxSpeed = Float.MIN_VALUE;

        public Astar(Model model) {
            explored = new TreeSet<>(new StateLocationComparator());
            frontier = new PriorityQueue<>(new CostHeuristicComparator());
            actions = new ArrayList<>(Arrays.asList("Right", "Left", "Up", "Down", "TopRight", "BottomRight", "TopLeft", "BottomLeft"));
            this.model = model;
        }

        public PriorityQueue<GameState> getFrontier() {
            return frontier;
        }

        public static double getCloseEnoughRange() {
            return closeEnoughRange;
        }

        public List<String> getActions() {
            return actions;
        }

        public void setActions(List<String> actions) {
            this.actions = actions;
        }


        public GameState findPath(Location start, Location end) {
            //Clear so that we can reuse Astar
            explored.clear();
            frontier.clear();

            GameState startState = new GameState(0, null, start, 0);
            explored.add(startState);
            frontier.add(startState);
            while (!frontier.isEmpty()) {
                GameState front = frontier.poll();
                if (MyAI.getDistance(front.getLocation(), end) < getCloseEnoughRange()) {
                    return front;
                }

                for (String action : actions) {
                    generateChild(front, action, end);
                }
                //System.out.println("Call for Astar has been made!");
            }
            return null;
        }

        public List<GameState> getBestPaths(Location start, Location end) {
            return findPath(start, end).getPathList();
        }

        public float getMaxSpeed() {
            if (maxSpeed == Float.MIN_VALUE) {
                for (int x = 0; x < 1200; x += 10) {
                    for (int y = 0; y < 600; y += 10) {
                        float currentSpeed = model.getTravelSpeed(x, y);
                        if (currentSpeed > maxSpeed) {
                            maxSpeed = currentSpeed;
                        }
                    }
                }
            }
            return maxSpeed;
        }


        public void generateChild(GameState current, String action, Location end) {
            GameState child = new GameState(current);
            //Checking for out of bounds
            float xLocation = current.getLocation().getX();
            float yLocation = current.getLocation().getY();
            float currentSpeed = model.getTravelSpeed(xLocation, yLocation);
            float maxSpeed = getMaxSpeed();
            float actionCost = 0;


            switch (action) {
                case "Right":
                    child.getLocation().setX(xLocation + 10);
                    actionCost += (10 / currentSpeed);

                    break;
                case "Left":
                    child.getLocation().setX(xLocation - 10);
                    actionCost += (10 / currentSpeed);
                    break;
                case "Up":
                    child.getLocation().setY(yLocation - 10);
                    actionCost += (10 / currentSpeed);
                    break;
                case "Down":
                    child.getLocation().setY(yLocation + 10);
                    actionCost += (10 / currentSpeed);
                    break;
                case "TopRight":
                    child.getLocation().setX(xLocation + 10);
                    child.getLocation().setY(yLocation - 10);
                    actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
                    break;
                case "BottomRight":
                    child.getLocation().setX(xLocation + 10);
                    child.getLocation().setY(yLocation + 10);
                    actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
                    break;
                case "TopLeft":
                    child.getLocation().setX(xLocation - 10);
                    child.getLocation().setY(yLocation - 10);
                    actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
                    break;
                case "BottomLeft":
                    child.getLocation().setX(xLocation - 10);
                    child.getLocation().setY(yLocation + 10);
                    actionCost += ((Math.sqrt(2) * 10) / currentSpeed);
                    break;
            }
            child.heuristic = (float) MyAI.getDistance(child.getLocation(), end) / maxSpeed;

            if (child.location.getY() >= 600) {
                return;
            } else if (child.location.getY() < 0) {
                return;
            }
            if (child.location.getX() >= 1200) {
                return;
            } else if (child.location.getX() < 0) {
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

    static class CostHeuristicComparator implements Comparator<GameState> {
        public int compare(GameState o1, GameState o2) {
            return Float.compare((o1.cost + o1.heuristic), (o2.cost + o2.heuristic));
        }
    }

    static class StateLocationComparator implements Comparator<GameState> {
        LocationComparator locationComparator = new LocationComparator();

        public int compare(GameState a, GameState b) {
            return locationComparator.compare(a.getLocation(), b.getLocation());
        }
    }

    static class LocationComparator implements Comparator<Location> {
        public int compare(Location o1, Location o2) {
            return Float.compare(o1.getX(), o2.getX()) == 0 ?
                    Float.compare(o1.getY(), o2.getY()) : Float.compare(o1.getX(), o2.getX());
        }
    }

    static class Location {
        private float x;
        private float y;

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

        /**
         * Get location of the sprite of the current index
         *
         * @param m     Model
         * @param index index of the sprite
         */
        public static Location getLocation(Model m, int index) {
            return new Location(m.getX(index), m.getY(index));
        }

        @Override
        public String toString() {
            return String.format("%s, %s", getX(), getY());
        }

    }

    static class GameState {
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
            this.location = new Location(s.location.getX(), s.location.getY());
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

    //======================================================================================================================
//Utility Classes
//======================================================================================================================
    static class TimeBalanceManager {

        public long MAX_TIME_BALANCE = 0;
        private long BALANCE_PER_TURN = 0;
        private Model m;
        private Map<Integer, Long> codeExecutionTimes = new HashMap<>();


        TimeBalanceManager(Model m) {
            this.m = m;
            calibrationTime();
        }

        public Map<Integer, Long> getCodeExecutionTimes() {
            return codeExecutionTimes;
        }

        public void setCodeExecutionTimes(Map<Integer, Long> codeExecutionTimes) {
            this.codeExecutionTimes = codeExecutionTimes;
        }

        private void calibrationTime() {
            long timeA = System.nanoTime();
            for (int i = 0; i < 420; i++)
                for (int y = 0; y < 60; y++)
                    for (int x = 0; x < 120; x++)
                        m.getTravelSpeed(10 * x, 10 * y);
            long timeB = System.nanoTime();

            BALANCE_PER_TURN = timeB - timeA;
            MAX_TIME_BALANCE = 20 * BALANCE_PER_TURN;
            //System.out.println("Cycles=" + Long.toString(agent_frame_time));
            //Here is the limit. agent_frame_time is set to the time it takes model.getTravel on all cells
        }
    }

    static class Result {
        private float value; //Distance
        private int index; //Index of the opponent
        private Location locationA;
        private Location locationB;

        public Result() {
            value = 0;
            index = -1;
            locationA = null;
            locationB = null;
        }

        public Result(float value, int index, Location locationA, Location locationB) {
            setValue(value);
            setIndex(index);
            setLocationA(locationA);
            setLocationB(locationB);
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Location getLocationA() {
            return locationA;
        }

        public void setLocationA(Location locationA) {
            this.locationA = locationA;
        }

        public Location getLocationB() {
            return locationB;
        }

        public void setLocationB(Location locationB) {
            this.locationB = locationB;
        }
    }
}
