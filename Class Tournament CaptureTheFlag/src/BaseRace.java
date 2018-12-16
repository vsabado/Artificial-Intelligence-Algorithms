public class BaseRace extends Mixed {

    @Override
    public void update(Model m){
        beFlagAttacker(m, 0);
        beFlagAttacker(m, 1);
        beFlagAttacker(m, 2);
        iter++;
    }

}
