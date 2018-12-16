public class ThreeDefender extends Mixed  {

    @Override
    public void update(Model m){
        beDefender(m, 0);
        beDefender(m, 1);
        beDefender(m, 2);
        iter++;
    }
}
