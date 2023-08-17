package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXPlayer;

import java.util.ArrayList;
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
            System.err.println("---- New move ----");

            int gameTreeMaxDepth = GameTreeUtils.getGameTreeMaxDepth(B);
            int gameTreeDepth = 2;
            GameTreeNode gameTree;

            while (gameTreeDepth <= gameTreeMaxDepth) {
                gameTreeCacheManager.resetCache();
                gameTree = GameTreeUtils
                        .createGameTree(B.copy(), gameTreeDepth, gameTreeCacheManager, timeManager);
                System.err.println(" - Game tree depth: " + GameTreeUtils.getGameTreeDepth(gameTree));
                System.err.println(" - Game tree nodes number: " + GameTreeUtils.getGameTreeNodesNumber(gameTree));
                columnIndex = getBestColumnIndex(gameTree);

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

    /**
     * Returns the index of the column with the best value.
     */
    private int getBestColumnIndex(GameTreeNode gameTree) throws TimeoutException {
        alphaBetaCounter = 0;

        ArrayList<GameTreeNode> childNodes = gameTree.getChildNodes();

        // Initialize maxValue with the value of the first available column
        int colValue = Evaluator.alphaBeta(
                childNodes.get(0),
                !first,
                Evaluator.WINP2VALUE,
                Evaluator.WINP1VALUE,
                GameTreeUtils.getGameTreeDepth(gameTree) - 1,
                gameTreeCacheManager,
                timeManager
        );
        int columnIndex = gameTree.getBoard().getAvailableColumns()[0];

        // Get the index of the column with the best value by calling minimax on every available column
        for (int i = 1; i < childNodes.size(); i++) {
            int nodeValue = Evaluator.alphaBeta(
                    childNodes.get(i),
                    !first,
                    Evaluator.WINP2VALUE,
                    Evaluator.WINP1VALUE,
                    GameTreeUtils.getGameTreeDepth(gameTree) - 1,
                    gameTreeCacheManager,
                    timeManager
            );

            // If first player maximize, otherwise minimize
            if (first) {
                if (nodeValue > colValue) {
                    colValue = nodeValue;
                    columnIndex = gameTree.getBoard().getAvailableColumns()[i];
                }
            } else {
                if (nodeValue < colValue) {
                    colValue = nodeValue;
                    columnIndex = gameTree.getBoard().getAvailableColumns()[i];
                }
            }
        }

        System.err.println(" - Minimax counter: " + alphaBetaCounter);

        return columnIndex;
    }
}
