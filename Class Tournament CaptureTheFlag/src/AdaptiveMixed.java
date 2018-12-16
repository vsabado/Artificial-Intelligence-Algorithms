public class AdaptiveMixed extends Mixed {
    private int aliveOpponent = 3;

    @Override
    public void update(Model m){
        aliveOpponent = getAliveOpponent(m);
        if( aliveOpponent == 3){
            beAggressor(m, 0);
            beAggressor(m, 1);
            beAggressor(m, 2);

        }else{
            beDefender(m, 0);
            beDefender(m, 1);
            beDefender(m, 2);
        }
        iter++;
    }

    public int getAliveOpponent(Model m) {
        int alive = 0;
        for (int i = 0; i < m.getSpriteCountOpponent(); i++) {
            if (m.getEnergyOpponent(i) > 0) {
                // don't care about dead opponents
                alive++;
            }
        }
        return alive;
    }
}
