package connectx.MyPlayer;

import connectx.*;

import java.util.concurrent.TimeoutException;

public class MyPlayer implements CXPlayer {
    private boolean first;
    private TimeManager timeManager;
    private GameTreeCacheManager gameTreeCacheManager;

    public static int alphaBetaCounter;

    public MyPlayer() {}

    @Override
    public void initPlayer(int M, int N, int X, boolean first, int timeout_in_secs) {
        this.first = first;
        this.timeManager = new TimeManager(timeout_in_secs);
        this.gameTreeCacheManager = new GameTreeCacheManager();
    }

    /**
     * Returns the index of the column to be selected.
     */
    @Override
    public int selectColumn(CXBoard B) {
        // Reset the time and select the first available column
        timeManager.resetTime();
        int columnIndex = B.getAvailableColumns()[0];

        // IterativeDeepening
        try {
            System.err.println("\n---- New move ----");

            int gameTreeMaxDepth = (B.M * B.N) - B.getMarkedCells().length + 1;
            int gameTreeDepth = 2;

            while (gameTreeDepth <= gameTreeMaxDepth) {
                gameTreeCacheManager.resetCache();

                System.err.println("\n - Game tree depth: " + gameTreeDepth);

                alphaBetaCounter = 0;
                columnIndex = Evaluator.alphaBeta(B, first, Evaluator.WINP2VALUE,
                        Evaluator.WINP1VALUE, gameTreeDepth, timeManager)[1];

                System.err.println(" - AlphaBeta counter: " + alphaBetaCounter);
                System.err.println(" - Elapsed time: " + timeManager.getElapsedTime());

                gameTreeDepth++;
            }
        } catch (TimeoutException ex) {
            System.err.println("xxxx Exception xxxx");
        }

        return columnIndex;
    }

    public String playerName() {
        return "MyPlayer";
    }
}
