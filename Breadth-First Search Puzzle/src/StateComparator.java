import java.util.Comparator;

class StateComparator implements Comparator<GameState>
{
    //Adapted from class code. Modified length to support variable number of pieces
    public int compare(GameState a, GameState b)
    {
        for(int i = 0; i < a.getState().length; i++)
        {
            if(a.getState()[i] < b.getState()[i])
                return -1;
            else if(a.getState()[i] > b.getState()[i])
                return 1;
        }
        return 0;
    }
}  