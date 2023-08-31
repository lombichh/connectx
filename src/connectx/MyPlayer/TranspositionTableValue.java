package connectx.MyPlayer;

/**
 * A value to be stored inside a transposition table.
 */
public class TranspositionTableValue {
    private GameChoice bestChoice;
    private int alpha, beta;

    public TranspositionTableValue(GameChoice bestChoice, int alpha, int beta) {
        this.bestChoice = bestChoice;
        this.alpha = alpha;
        this.beta = beta;
    }

    public GameChoice getBestChoice() {
        return bestChoice;
    }

    public void setBestChoice(GameChoice bestChoice) {
        this.bestChoice = bestChoice;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getBeta() {
        return beta;
    }

    public void setBeta(int beta) {
        this.beta = beta;
    }

}
