public class TwoOneStrategy extends Mixed {

    @Override
    public void update(Model m){
        beAggressor(m, 0);
        beAggressor(m, 1);
        beDefender(m, 2);
        iter++;
    }
}
