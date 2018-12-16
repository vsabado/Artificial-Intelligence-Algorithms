public class BattleResult {
    private int battleResult;
    private long battleTimer;

    public BattleResult(){
        this.battleResult = 0;
        this.battleTimer = 0;
    }

    public BattleResult(int battleResult, long battleTimer) {
        this.battleResult = battleResult;
        this.battleTimer = battleTimer;
    }

    public int getBattleResult() {
        return battleResult;
    }

    public void setBattleResult(int battleResult) {
        this.battleResult = battleResult;
    }

    public long getBattleTimer() {
        return battleTimer;
    }

    public void setBattleTimer(long battleTimer) {
        this.battleTimer = battleTimer;
    }
}
