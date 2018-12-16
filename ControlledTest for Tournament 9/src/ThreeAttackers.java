public class ThreeAttackers extends Mixed {

    @Override
    public void update(Model m){
        beAggressor(m, 0);
        beAggressor(m, 1);
        beAggressor(m, 2);
        iter++;
    }
}
