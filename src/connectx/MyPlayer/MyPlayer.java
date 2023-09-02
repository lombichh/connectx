package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXPlayer;

public class MyPlayer implements CXPlayer {
    private boolean first;
    private TimeManager timeManager;

    public MyPlayer() {
    }

    @Override
    public void initPlayer(int M, int N, int X, boolean first, int timeout_in_secs) {
        this.first = first;
        this.timeManager = new TimeManager(timeout_in_secs);
    }

    /**
     * Returns the index of the column to be selected.
     */
    @Override
    public int selectColumn(CXBoard B) {
        timeManager.resetTime();

        return MiniMax.iterativeDeepening(B, first, timeManager);
    }

    public String playerName() {
        return "MyPlayer";
    }
}
