import java.util.ArrayList;
import java.util.List;

public class ImprovedMixed extends Mixed{

    Sprite[] sprites = new Sprite[3];


    public ImprovedMixed() {
        super();
        for (int i = 0; i < sprites.length; i++) {
            sprites[i] = new Sprite(i, 100, 100);
        }
    }

    @Override
    void avoidBombs(Model m, int i) {
        if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
            float dx = m.getX(i) - m.getBombTargetX(index);
            float dy = m.getY(i) - m.getBombTargetY(index);
            if(dx == 0 && dy == 0)
                dx = 1.0f;

            float newX = m.getX(i) + dx * 10.0f;
            float newY =  m.getY(i) + dy * 10.0f;
            System.out.println("Sprite" + i + " newX: " + newX + ", " + " newY: " + newY);
            boolean isMoveRight = false;
            boolean isMoveDown = false;
            if(dx > 0){
                isMoveRight = true;
            }
            if(dy > 0){
                isMoveDown = true;
            }
            sprites[i].setDestination(newX, newY);
            if( !(0 > newX || newX >= 1200 || 0 > newY || newY >= 600)){
                System.out.println("xDestination: " + sprites[i].xDestination);
                System.out.println("yDestination: " + sprites[i].yDestination);
                System.out.println("Travel Speed: " + getTravelSpeed( m, sprites[i].xDestination, sprites[i].yDestination));
                System.out.println(sprites[i].isHeadingToWater());
            }
            m.setDestination(i, newX, newY);
        }
    }

    public float getTravelSpeed(Model m, float x, float y) {
        if (!(0 > x || x >= 1200 || 0 > y || y >= 600)) {
            return m.getTravelSpeed(x, y);
        }
        return 0;
    }


    @Override
    public void update(Model m) {
        //Set model if not yet set
        if(sprites[0].getModel() == null){
            for(Sprite sprite : sprites){
                sprite.setModel(m);
            }
        }
//        beFlagAttacker(m, 0);
        beAggressor(m, 0);
        beAggressor(m, 1);
        beAggressor(m, 2);
//        beDefender(m, 2);

        iter++;
    }

    class Sprite{
        public Model model;
        public int index;

        public float xPosition;
        public float yPosition;
        public float xDestination;
        public float yDestination;

        public Sprite(int i, float xPosition, float yPosition) {
            index = i;
            this.xPosition = xPosition;
            this.yPosition = yPosition;
        }

        public Model getModel() {
            return model;
        }

        public void setModel(Model model) {
            this.model = model;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public float getxPosition() {
            return xPosition;
        }

        public void setxPosition(float xPosition) {
            this.xPosition = xPosition;
        }

        public float getyPosition() {
            return yPosition;
        }

        public void setyPosition(float yPosition) {
            this.yPosition = yPosition;
        }

        public float getxDestination() {
            return xDestination;
        }

        public void setxDestination(float xDestination) {
            this.xDestination = xDestination;
        }

        public float getyDestination() {
            return yDestination;
        }

        public void setyDestination(float yDestination) {
            this.yDestination = yDestination;
        }

        public void update() {
            if(model == null){
                return;
            }

            yPosition = model.getX(index);
            yPosition = model.getY(index);
//            //Move to the preset position
//            if (iter % 2 == 0) { moveToPosition(xPosition, yPosition); }
//            //Keep your energy up
//            if (energy() < 0.7f) { stay(); }
//            Enemy nearestEnemy = getEnemyNearestMe();
//            if (nearestEnemy != null) {
//                shootEnemy(nearestEnemy);
//            }
//            //Shoot the flag if you can
//            shootTheFlag();
//            //Don't let enemies get too close (can come within 90% of throwing distance)
//            avoidEnemies();
//            //And always avoid bombs
//            avoidBombs();
        }

//        private boolean avoidEnemies() {
//            Enemy nearestEnemy = getEnemyNearestMe();
//            if(nearestEnemy != null && nearestEnemy.distance * 0.9f <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
//                float dx = x() - model.getXOpponent(nearestEnemy.index);
//                float dy = y() - model.getYOpponent(nearestEnemy.index);
//                if(dx == 0 && dy == 0)
//                    dx = 1.0f;
//                move(x() + dx * 10.0f, y() + dy * 10.0f);
//                return true;
//            }
//            return false;
//        }

//        private Enemy getEnemyNearestMe() {
//            int enemyIndex = -1;
//            float dd = Float.MAX_VALUE;
//            for(int i = 0; i < model.getSpriteCountOpponent(); i++) {
//                if(model.getEnergyOpponent(i) < 0)
//                    continue; // don't care about dead opponents
//                float d = sq_dist(x(), y(), model.getXOpponent(i), model.getYOpponent(i));
//                if(d < dd) {
//                    dd = d;
//                    enemyIndex = i;
//                }
//            }
//            if (enemyIndex == -1) {
//                return null;
//            }
//            return new Enemy(enemyIndex, dd);
//        }

//        private boolean avoidBombs() {
//            Bomb nearestBomb = getBombNearestMe();
//            if(nearestBomb != null && nearestBomb.distance <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
//                float dx = x() - model.getBombTargetX(nearestBomb.index);
//                float dy = y() - model.getBombTargetY(nearestBomb.index);
//                if(dx == 0 && dy == 0)
//                    dx = 1.0f;
//                move(x() + dx * 10.0f, y() + dy * 10.0f);
//                return true;
//            }
//            return false;
//        }

//        private Bomb getBombNearestMe() {
//            Bomb nearestBomb = new Bomb(-1, Float.MAX_VALUE);
//            float dd = Float.MAX_VALUE;
//            for(int i = 0; i < model.getBombCount(); i++) {
//                float d = sq_dist(x(), y(), model.getBombTargetX(i), model.getBombTargetY(i));
//                if(d < dd) {
//                    nearestBomb = new Bomb(i, d);
//                }
//            }
//            if (nearestBomb.index == -1) {
//                return null;
//            }
//            return nearestBomb;
//        }

        //Uses UCS to find an optimal path
//        private void moveToPosition(float x, float y) {
//            State[][] costMap = TreeUtility.uniformCostSearch(
//                    terrainMap,
//                    terrainMap[(int) x() / 10][(int) y() / 10],
//                    terrainMap[(int) x / 10][(int) y / 10]);
//
//            //Backtrack to the first step we need to take
//            if (costMap != null) {
//                State currentState = costMap[(int) x / 10][(int) y / 10];
//                State prevState = null;
//                while (currentState.parent != null) {
//                    prevState = currentState;
//                    currentState = prevState.parent;
//                }
//                if (prevState != null) {
//                    move(prevState.x * 10, prevState.y * 10);
//                }
//            }
//        }

//        private void shootTheFlag() {
//            if (energy() > 0.5f & sq_dist(x(), y(), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
//                shoot(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
//            }
//        }

//        private void shootEnemy(Enemy enemy) {
//            //Center the shot on the enemy if I can
//            if (enemy.distance <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
//                shoot(model.getXOpponent(enemy.index), model.getYOpponent(enemy.index));
//
//                //Otherwise try to get them in the blast radius anyhow
//            }else if (Math.sqrt(enemy.distance) < Model.MAX_THROW_RADIUS + (Model.BLAST_RADIUS * 0.25 ) ) {
//                float dx = model.getXOpponent(enemy.index) - x();
//                float dy = model.getYOpponent(enemy.index) - y();
//                float scale = (float) (Model.MAX_THROW_RADIUS / Math.sqrt(enemy.distance));
//                float throwX = dx * scale + x();
//                float throwY = dy * scale + y();
//                shoot(throwX, throwY);
//            }
//        }

        public void setDestination(float x, float y) {
            if(getEnergy() >= 0) { // when you are dead, you cannot change your destination
                    if( (x - xDestination) * (x - xDestination) + (y - yDestination) * (y - yDestination) < 100) {
                        x += 15;
                        y += 10;
                    }
                }
                xDestination = x;
                yDestination = y;
        }

        public boolean isHeadingToWater(){
            float travelSpeed = getTravelSpeed(model,xDestination, yDestination);
            return !(travelSpeed == 0) && travelSpeed <= 0.3;
        }

        public float x() {
            return model.getX(index);
        }

        public float y() {
            return model.getY(index);
        }

        public float getEnergy() {
            return model.getEnergySelf(index);
        }

        public void move(float x, float y) {
            model.setDestination(index, x, y);
        }

        public void stay() {
            model.setDestination(index, x(), y());
        }

        public void shoot(float x, float y) {
            model.throwBomb(index, x, y);
        }
    }

    public int getAliveOpponent(Model m) {
        int alive = 0;
        for (int i = 0; i < m.getSpriteCountOpponent(); i++) {
            //Counts the number of alive opponents
            if (m.getEnergyOpponent(i) > 0) {
                // don't care about dead opponents
                alive++;
            }
        }
        return alive;
    }

}
