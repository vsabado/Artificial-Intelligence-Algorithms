import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;



public class ShepherdSabrina implements IAgent {

	static int iter;
	float xPreset, yPreset;
	ArrayList<Sprite> sprites = new ArrayList<Sprite>();
	static State[][] terrainMap;
	public static Model model;
	static Random r;


	ShepherdSabrina() {
		reset();
		//The endgame coordinates for the sprites, close to the enemy flag
		xPreset = Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS / (float) Math.sqrt(2) * 0.9f;
		yPreset = Model.YFLAG_OPPONENT + Model.MAX_THROW_RADIUS / (float) Math.sqrt(2)  * 0.9f;
		for (int i = 0; i < 3; i++) {
			sprites.add(new Sprite(i, xPreset, yPreset));
		}
		r = new Random(1234);
	}

	@Override
	public void reset() {
		iter = 0;
	}

	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	@Override
	public void update(Model m) {
			getTerrainStateMap(m);
			model = m;
			sprites.sort((a, b) ->Boolean.compare(b.defender, a.defender));

			//set defender
			setDefenders();
			for (Sprite sprite : sprites) {
				sprite.update();
			}
		iter++;
	}

	public void setDefenders() {

		double myDistToOwnFlag0 = sprites.get(0).distFromOwnFlag();
		double myDistToOwnFlag1 = sprites.get(1).distFromOwnFlag();
		int countOppCloserToFlag0 = 0;
		int countOppCloserToFlag1 = 0;
		for(int i = 0; i < model.getSpriteCountOpponent(); i++) {
			if(model.getEnergyOpponent(i) < 0)
				continue; // don't care about dead opponents
			float d = sq_dist(model.getXOpponent(i), model.getYOpponent(i), Model.XFLAG, Model.YFLAG);
			if (d < myDistToOwnFlag0 + Model.MAX_THROW_RADIUS)
				countOppCloserToFlag0++;
			if (d < myDistToOwnFlag1 + Model.MAX_THROW_RADIUS)
				countOppCloserToFlag1++;
		}


		if(countOppCloserToFlag0 > 0)
			sprites.get(0).defender = true;
		else
			sprites.get(0).defender = false;

		if(countOppCloserToFlag1 > 1)
			sprites.get(1).defender = true;
		else
			sprites.get(1).defender = false;

		sprites.get(2).defender = false;
	}

	public State[][] getTerrainStateMap(Model m){
		if (terrainMap == null) {
			terrainMap = TreeUtility.createTerrainStateMap(m);
		}
		return terrainMap;
	}


	public static class Sprite{
		public int index;

		public float xPosition;
		public float yPosition;
		public boolean defender;

		public Sprite(int i, float xPosition, float yPosition) {
			index = i;
			this.xPosition = xPosition;
			this.yPosition = yPosition;
		}

		public void update() {
			if (!defendTheFlag()) {
				//Move to the preset position
				if (iter % 2 == 0) { moveToPosition(xPosition, yPosition); }
			}
			//Keep your energy up
			if (energy() < 0.7) { stay(); }

			//if enemies are closer to the flag then follow back to defend

			Enemy nearestEnemy = getEnemyNearestMe();
			if (nearestEnemy != null) {
				shootEnemy(nearestEnemy);
			}

			//Shoot the flag if you can
			shootTheFlag();
			//Don't let enemies get too close (can come within 90% of throwing distance)
			avoidEnemies();
			//And always avoid bombs
			avoidBombs();
		}

		//Uses UCS to find an optimal path
		private void moveToPosition(float x, float y) {

			State[][] costMap = TreeUtility.uniformCostSearch(
					terrainMap, 
					terrainMap[(int) x() / 10][(int) y() / 10], 
					terrainMap[(int) x / 10][(int) y / 10]);

			//Backtrack to the first step we need to take
			if (costMap != null) {
				State currentState = costMap[(int) x / 10][(int) y / 10];
				State prevState = null;
				while (currentState.parent != null) {
					prevState = currentState;
					currentState = prevState.parent;
				}
				if (prevState != null) {
					move(prevState.x * 10, prevState.y * 10);
				}
			}
		}

		private boolean defendTheFlag() {
			if(defender) {
				// Find the opponent nearest to my flag
				Enemy nearestToFlag = getNearestEnemy(Model.XFLAG,Model.YFLAG);
				//check to see if this current sprite is the nearest to the enemy
				//if it is then do everything in here
				if(nearestToFlag != null) {

					// Stay between the enemy and my flag
					moveToPosition( 0.5f * (Model.XFLAG + nearestToFlag.x()), 0.5f * (Model.YFLAG + nearestToFlag.y()));
					return true;
				}
			}
			return false;
		}

		private void shootTheFlag() {
			if (energy() > 0.45f & sq_dist(x(), y(), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				shoot(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
			}
		}

		private boolean avoidEnemies() {
			Enemy nearestEnemy = getEnemyNearestMe();
			//distance should be based on speed to escape the current position
			if(nearestEnemy != null && nearestEnemy.distance * 0.9f <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				float dx = x() - model.getXOpponent(nearestEnemy.index);
				float dy = y() - model.getYOpponent(nearestEnemy.index);
				if(dx == 0 && dy == 0)
					dx = 1.0f;
				move(x() + dx * 10.0f, y() + dy * 10.0f);
				return true;
			}
			return false;
		}

		private Enemy getEnemyNearestMe() {
			int enemyIndex = -1;
			float dd = Float.MAX_VALUE;
			for(int i = 0; i < model.getSpriteCountOpponent(); i++) {
				if(model.getEnergyOpponent(i) < 0)
					continue; // don't care about dead opponents
				float d = sq_dist(x(), y(), model.getXOpponent(i), model.getYOpponent(i));
				if(d < dd) {
					dd = d;
					enemyIndex = i;
				}
			}
			if (enemyIndex == -1) {
				return null;
			}
			return new Enemy(enemyIndex, dd);
		}

		private Enemy getNearestEnemy(float x, float y) {
			int enemyIndex = -1;
			float dd = Float.MAX_VALUE;
			for(int i = 0; i < model.getSpriteCountOpponent(); i++) {
				if(model.getEnergyOpponent(i) < 0)
					continue; // don't care about dead opponents
				float d = sq_dist(x, y, model.getXOpponent(i), model.getYOpponent(i));
				if(d < dd) {
					dd = d;
					enemyIndex = i;
				}
			}
			if (enemyIndex == -1) {
				return null;
			}
			return new Enemy(enemyIndex, dd);
		}

		private Sprite getNearestSprite(float x, float y) {
			int spriteIndex = -1;
			float dd = Float.MAX_VALUE;
			for(int i = 0; i < model.getSpriteCountSelf(); i++) {
				if(model.getEnergySelf(i) < 0)
					continue; // don't care about dead self
				float d = sq_dist(x, y, model.getX(i), model.getY(i));
				if(d < dd) {
					dd = d;
					spriteIndex = i;
				}
			}
			if (spriteIndex == -1) {
				return null;
			}
			return new Sprite(spriteIndex, model.getX(spriteIndex),model.getY(spriteIndex));
		}

		private boolean avoidBombs() {
			Bomb nearestBomb = getBombNearestMe();
			if(nearestBomb != null && nearestBomb.distance <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
				float dx = x() - model.getBombTargetX(nearestBomb.index);
				float dy = y() - model.getBombTargetY(nearestBomb.index);
				if(Model.XFLAG > x() && energy() >= 0.5 && model.getTravelSpeed(Math.min(Math.max(0,dx),Model.XMAX - 0.5f), Math.min(Math.max(0,dy),Model.YMAX - 0.5f)) > 0.3){
					if(dx == 0 && dy == 0)
						dx = 1.0f;
					move(x() + dx * 10.0f, y() + dy * 10.0f);
				}else {
					forkAvoidBombs(model.getBombTargetX(nearestBomb.index), model.getBombTargetY(nearestBomb.index));
				}
				return true;
			}
			return false;
		}

		void forkAvoidBombs(float bombX, float bombY) {

			float bestX = x();
			float bestY = y();
			float bestDodge = 0;
			for(int sim = 0; sim < 8; sim++) { // try 8 candidate destinations
				float x = (float)(x()+(Math.cos((Math.PI*sim)/4)*Model.BLAST_RADIUS));
				float y = (float)(y()+(Math.sin((Math.PI*sim)/4)*Model.BLAST_RADIUS));
				if(model.getTravelSpeed(Math.min(Math.max(0,x),Model.XMAX - 0.5f), Math.min(Math.max(0,y),Model.YMAX - 0.5f)) > 0.3) {
					// Fork the universe and simulate it for 10 time-steps
					Controller cFork = model.getController().fork(new Shadow(x, y), new OpponentShadow());
					Model mFork = cFork.getModel();
					for(int j = 0; j < 10; j++)
						cFork.update();

					// See how close the current sprite got to the opponent's flag in the forked universe
					//				float sqd = sq_dist(mFork.getX(i), mFork.getY(i), XGoal, YGoal);
					//				float iEnergy = sq_dist(mFork.getX(i), mFork.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
					float dodge = sq_dist(mFork.getX(index), mFork.getY(index), bombX, bombY);

					if(dodge > bestDodge) {
						bestDodge = dodge;
						bestX = x;
						bestY = y;
					}
				}

				//				if(dodge > Model.BLAST_RADIUS && dodge < bestDodge) {
				//					bestDodge = dodge;
				//					bestX = x;
				//					bestY = y;
				//				}
			}

			// Head for the point that worked out best in simulation
			move(bestX, bestY);
		}

		private Bomb getBombNearestMe() {
			Bomb nearestBomb = new Bomb(-1, Float.MAX_VALUE);
			float dd = Float.MAX_VALUE;
			for(int i = 0; i < model.getBombCount(); i++) {
				float d = sq_dist(x(), y(), model.getBombTargetX(i), model.getBombTargetY(i));
				if(d < dd) {
					nearestBomb = new Bomb(i, d);
				}
			}
			if (nearestBomb.index == -1) {
				return null;
			}
			return nearestBomb;
		}

		private void shootEnemy(Enemy enemy) {
			//Center the shot on the enemy if I can
			if (enemy.distance <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				shoot(model.getXOpponent(enemy.index), model.getYOpponent(enemy.index));

				//Otherwise try to get them in the blast radius anyhow
			}else if (Math.sqrt(enemy.distance) < Model.MAX_THROW_RADIUS + (Model.BLAST_RADIUS * 0.35) ) {
				float dx = model.getXOpponent(enemy.index) - x();
				float dy = model.getYOpponent(enemy.index) - y();
				float scale = (float) (Model.MAX_THROW_RADIUS / Math.sqrt(enemy.distance));
				float throwX = dx * scale + x();
				float throwY = dy * scale + y();
				shoot(throwX, throwY);
			}
		}

		public float x() {
			return model.getX(index);
		}

		public float y() {
			return model.getY(index);
		}

		public float energy() {
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

		public double distFromOwnFlag() {
			return sq_dist(x(), y(), Model.XFLAG, Model.YFLAG);
		}

	}

	public static class Enemy{
		int index;
		float distance;

		public Enemy(int index, float distance) {
			this.index = index;
			this.distance = distance;
		}

		public float energy() {
			return model.getEnergyOpponent(index);
		}
		public float x() {
			return model.getXOpponent(index);
		}

		public float y() {
			return model.getYOpponent(index);
		}
	}

	public static class Bomb{
		int index;
		float distance;

		public Bomb(int index) {
			this.index = index;

		}

		public Bomb(int index, float distance) {
			this.index = index;
			this.distance = distance;
		}
	}

	public static class State implements Comparable<State>{
		public double cost;
		public double actionCost;
		public State parent;
		public int x;
		public int y;

		public State(double actionCost, int x, int y) {
			this.cost = Double.MAX_VALUE;
			this.actionCost = actionCost;
			this.x = x;
			this.y = y;
		}

		public State[] getAdjacent(State[][] states){
			int maxX = states.length - 1;
			int maxY = states[0].length - 1;

			State[] adjacentList = new State[4];
			adjacentList[0] = (x + 1 <= maxX) ? states[x + 1][y] : null;
			adjacentList[1] = (x - 1 >= 0) ? states[x - 1][y] : null;
			adjacentList[2] = (y + 1 <= maxY) ? states[x][y + 1] : null;
			adjacentList[3] = (y - 1 >= 0) ? states[x][y - 1] : null;

			return adjacentList;
		}

		@Override
		public int compareTo(State state) {
			if (this.cost > state.cost){
				return 1;
			}
			if (this.cost < state.cost){
				return -1;
			}
			return 0;
		}
	}

	public static class TreeUtility{
		//I'm representing the map as 1/10th of XMAX and YMAX to cut down on computation time.
		public static State[][] createTerrainStateMap(Model model){
			State[][] map = new State[((int)Model.XMAX / 10) + 1][((int) Model.YMAX / 10) + 1];
			for(int x = 0; x < map.length; x++) {
				for (int y = 0; y < map[0].length; y++) {
					double speed = model.getTravelSpeed(x * 10, y * 10);
					//make water cost more
					//					if (speed < 0.3)
					//						speed *= 0.05;
					map[x][y] = new State(1 / speed, x, y);
				}
			}
			return map;
		}

		public static State[][] uniformCostSearch(State[][] map, State start, State end){
			PriorityQueue<State> queue = new PriorityQueue<State>();
			boolean[][] visited = new boolean[map.length][map[0].length];
			start.cost = 0;
			start.parent = null;
			visited[start.x][start.y] = true;
			queue.add(start);

			while (queue.size() > 0){
				State state = queue.remove();
				if (state.equals(end)){
					return map;
				}
				for(State adjacentState : state.getAdjacent(map)){
					if (adjacentState == null) break;
					if (visited[adjacentState.x][adjacentState.y]){
						if (state.cost + adjacentState.actionCost < adjacentState.cost){
							adjacentState.cost = state.cost + adjacentState.actionCost;
							adjacentState.parent = state;
						}
					}else{
						adjacentState.cost = state.cost + adjacentState.actionCost;
						adjacentState.parent = state;
						queue.add(adjacentState);
						visited[adjacentState.x][adjacentState.y] = true;
					}
				}
			}
			return null;
		}
	}



	static class Shadow implements IAgent
	{
		float dx;
		float dy;

		Shadow(float destX, float destY) {
			dx = destX;
			dy = destY;
		}

		public void reset() {
		}

		public void update(Model m) {
			for(int i = 0; i < 3; i++) {
				if (dx > 0 && dx < Model.XMAX && dy > 0 && dy < Model.YMAX)
					m.setDestination(i, dx, dy);
			}
		}
	}

	static class OpponentShadow implements IAgent
	{
		static int iter;
		float xPreset, yPreset;
		SpriteShadow[] sprites = new SpriteShadow[3];
		static StateShadow[][] terrainMap;

		int index;
		OpponentShadow() {
			xPreset = Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS / (float) Math.sqrt(2) * 0.9f;
			yPreset = Model.YFLAG_OPPONENT + Model.MAX_THROW_RADIUS / (float) Math.sqrt(2)  * 0.9f;
			for (int i = 0; i < sprites.length; i++) {
				sprites[i] = new SpriteShadow(i, xPreset, yPreset);
			}
		}

		public void reset() {
		}

		public static float sq_dist(float x1, float y1, float x2, float y2) {
			return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
		}

		float nearestOpponent(Model m, float x, float y) {
			index = -1;
			float dd = Float.MAX_VALUE;
			for(int i = 0; i < m.getSpriteCountOpponent(); i++) {
				if(m.getEnergyOpponent(i) < 0)
					continue; // don't care about dead opponents
				float d = sq_dist(x, y, m.getXOpponent(i), m.getYOpponent(i));
				if(d < dd) {
					dd = d;
					index = i;
				}
			}
			return dd;
		}

		float nearestBombTarget(Model m, float x, float y) {
			index = -1;
			float dd = Float.MAX_VALUE;
			for(int i = 0; i < m.getBombCount(); i++) {
				float d = sq_dist(x, y, m.getBombTargetX(i), m.getBombTargetY(i));
				if(d < dd) {
					dd = d;
					index = i;
				}
			}
			return dd;
		}

		void avoidBombs(Model m, int i) {
			if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
				float dx;
				dx = m.getX(i) + m.getBombTargetX(index);
				float dy;
				if (m.getY(i) > Model.YFLAG_OPPONENT)
					dy = m.getY(i) - m.getBombTargetY(index);
				else
					dy = m.getY(i) + m.getBombTargetY(index);
				if(dx == 0 && dy == 0)
					dx = 1.0f;
				m.setDestination(i, m.getX(i) + dx, m.getY(i) + dy);
			}	
		}

		public void update(Model m) {
			getTerrainStateMap(m);
			SpriteShadow.model = m;
			for (SpriteShadow sprite : sprites) {
				sprite.update();
			}
			iter++;
		}
		public StateShadow[][] getTerrainStateMap(Model m){
			if (terrainMap == null) {
				terrainMap = TreeUtilityShadow.createTerrainStateMap(m);
			}
			return terrainMap;
		}


		public static class BombShadow{
			int index;
			float distance;

			public BombShadow(int index) {
				this.index = index;

			}

			public BombShadow(int index, float distance) {
				this.index = index;
				this.distance = distance;
			}
		}

		public static class EnemyShadow{
			int index;
			float distance;

			public EnemyShadow(int index, float distance) {
				this.index = index;
				this.distance = distance;
			}
		}

		public static class SpriteShadow{
			public static Model model;
			public int index;

			public float xPosition;
			public float yPosition;

			public SpriteShadow(int i, float xPosition, float yPosition) {
				index = i;
				this.xPosition = xPosition;
				this.yPosition = yPosition;
			}

			public void update() {
				//Move to the preset position
				if (iter % 2 == 0) { moveToPosition(xPosition, yPosition); }
				//Keep your energy up
				if (energy() < 0.7f) { stay(); }
				EnemyShadow nearestEnemy = getEnemyNearestMe();
				if (nearestEnemy != null) {
					shootEnemy(nearestEnemy);
				}
				//Shoot the flag if you can
				shootTheFlag();
				//Don't let enemies get too close (can come within 90% of throwing distance)
				avoidEnemies();
				//And always avoid bombs
				avoidBombs();
			}

			private boolean avoidEnemies() {
				EnemyShadow nearestEnemy = getEnemyNearestMe();
				if(nearestEnemy != null && nearestEnemy.distance * 0.9f <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
					float dx = x() - model.getXOpponent(nearestEnemy.index);
					float dy = y() - model.getYOpponent(nearestEnemy.index);
					if(dx == 0 && dy == 0)
						dx = 1.0f;
					move(x() + dx * 10.0f, y() + dy * 10.0f);
					return true;
				}
				return false;
			}

			private EnemyShadow getEnemyNearestMe() {
				int enemyIndex = -1;
				float dd = Float.MAX_VALUE;
				for(int i = 0; i < model.getSpriteCountOpponent(); i++) {
					if(model.getEnergyOpponent(i) < 0)
						continue; // don't care about dead opponents
					float d = sq_dist(x(), y(), model.getXOpponent(i), model.getYOpponent(i));
					if(d < dd) {
						dd = d;
						enemyIndex = i;
					}
				}
				if (enemyIndex == -1) {
					return null;
				}
				return new EnemyShadow(enemyIndex, dd);
			}

			private boolean avoidBombs() {
				BombShadow nearestBomb = getBombNearestMe();
				if(nearestBomb != null && nearestBomb.distance <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
					float dx = x() - model.getBombTargetX(nearestBomb.index);
					float dy = y() - model.getBombTargetY(nearestBomb.index);
					if(dx == 0 && dy == 0)
						dx = 1.0f;
					move(x() + dx * 10.0f, y() + dy * 10.0f);
					return true;
				}
				return false;
			}

			private BombShadow getBombNearestMe() {
				BombShadow nearestBomb = new BombShadow(-1, Float.MAX_VALUE);
				float dd = Float.MAX_VALUE;
				for(int i = 0; i < model.getBombCount(); i++) {
					float d = sq_dist(x(), y(), model.getBombTargetX(i), model.getBombTargetY(i));
					if(d < dd) {
						nearestBomb = new BombShadow(i, d);
					}
				}
				if (nearestBomb.index == -1) {
					return null;
				}
				return nearestBomb;
			}

			//Uses UCS to find an optimal path
			private void moveToPosition(float x, float y) {
				StateShadow[][] costMap = TreeUtilityShadow.uniformCostSearch(
						terrainMap, 
						terrainMap[(int) x() / 10][(int) y() / 10], 
						terrainMap[(int) x / 10][(int) y / 10]);

				//Backtrack to the first step we need to take
				if (costMap != null) {
					StateShadow currentState = costMap[(int) x / 10][(int) y / 10];
					StateShadow prevState = null;
					while (currentState.parent != null) {
						prevState = currentState;
						currentState = prevState.parent;
					}
					if (prevState != null) {
						move(prevState.x * 10, prevState.y * 10);
					}
				}
			}

			private void shootTheFlag() {
				if (energy() > 0.5f & sq_dist(x(), y(), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
					shoot(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
				}
			}

			private void shootEnemy(EnemyShadow enemy) {
				//Center the shot on the enemy if I can
				if (enemy.distance <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
					shoot(model.getXOpponent(enemy.index), model.getYOpponent(enemy.index));

					//Otherwise try to get them in the blast radius anyhow
				}else if (Math.sqrt(enemy.distance) < Model.MAX_THROW_RADIUS + (Model.BLAST_RADIUS * 0.25 ) ) {
					float dx = model.getXOpponent(enemy.index) - x();
					float dy = model.getYOpponent(enemy.index) - y();
					float scale = (float) (Model.MAX_THROW_RADIUS / Math.sqrt(enemy.distance));
					float throwX = dx * scale + x();
					float throwY = dy * scale + y();
					shoot(throwX, throwY);
				}
			}

			public float x() {
				return model.getX(index);
			}

			public float y() {
				return model.getY(index);
			}

			public float energy() {
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

		public static class StateShadow implements Comparable<StateShadow>{
			public double cost;
			public double actionCost;
			public StateShadow parent;
			public int x;
			public int y;

			public StateShadow(double actionCost, int x, int y) {
				this.cost = Double.MAX_VALUE;
				this.actionCost = actionCost;
				this.x = x;
				this.y = y;
			}

			public StateShadow[] getAdjacent(StateShadow[][] states){
				int maxX = states.length - 1;
				int maxY = states[0].length - 1;

				StateShadow[] adjacentList = new StateShadow[4];
				adjacentList[0] = (x + 1 <= maxX) ? states[x + 1][y] : null;
				adjacentList[1] = (x - 1 >= 0) ? states[x - 1][y] : null;
				adjacentList[2] = (y + 1 <= maxY) ? states[x][y + 1] : null;
				adjacentList[3] = (y - 1 >= 0) ? states[x][y - 1] : null;

				return adjacentList;
			}

			@Override
			public int compareTo(StateShadow state) {
				if (this.cost > state.cost){
					return 1;
				}
				if (this.cost < state.cost){
					return -1;
				}
				return 0;
			}
		}

		public static class TreeUtilityShadow{
			//I'm representing the map as 1/10th of XMAX and YMAX to cut down on computation time.
			public static StateShadow[][] createTerrainStateMap(Model model){
				StateShadow[][] map = new StateShadow[((int)Model.XMAX / 10) + 1][((int) Model.YMAX / 10) + 1];
				for(int x = 0; x < map.length; x++) {
					for (int y = 0; y < map[0].length; y++) {
						map[x][y] = new StateShadow(1 / model.getTravelSpeed(x * 10, y * 10), x, y);
					}
				}
				return map;
			}

			public static StateShadow[][] uniformCostSearch(StateShadow[][] map, StateShadow start, StateShadow end){
				PriorityQueue<StateShadow> queue = new PriorityQueue<StateShadow>();
				boolean[][] visited = new boolean[map.length][map[0].length];
				start.cost = 0;
				start.parent = null;
				visited[start.x][start.y] = true;
				queue.add(start);

				while (queue.size() > 0){
					StateShadow state = queue.remove();
					if (state.equals(end)){
						return map;
					}
					for(StateShadow adjacentState : state.getAdjacent(map)){
						if (adjacentState == null) break;
						if (visited[adjacentState.x][adjacentState.y]){
							if (state.cost + adjacentState.actionCost < adjacentState.cost){
								adjacentState.cost = state.cost + adjacentState.actionCost;
								adjacentState.parent = state;
							}
						}else{
							adjacentState.cost = state.cost + adjacentState.actionCost;
							adjacentState.parent = state;
							queue.add(adjacentState);
							visited[adjacentState.x][adjacentState.y] = true;
						}
					}
				}
				return null;
			}
		}


	}


}
