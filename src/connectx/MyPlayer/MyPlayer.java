package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXCellState;
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
        // Reset the time and the cache
        timeManager.resetTime();
        gameTreeCacheManager.resetCache();

        // Select the first available column
        int columnIndex = B.getAvailableColumns()[0];

        // IterativeDeepening
        try {
            System.err.println("---- New move ----");

            int gameTreeDepth = 10;
            GameTreeNode gameTree = GameTreeUtils.createGameTreeCaller(B, gameTreeDepth, gameTreeCacheManager, timeManager);
            System.err.println(" - Game tree nodes: " + GameTreeUtils.getGameTreeNodesNumber(gameTree));
            columnIndex = getBestColumnIndex(gameTree);

            /*while (gameTreeDepth < GameTreeUtils.getGameTreeMaxDepth(B)) {
                GameTreeUtils.incrementGameTreeDepth(gameTree, timeManager);
                System.err.println(" - Game tree depth: " + GameTreeUtils.getGameTreeDepth(gameTree));
                System.err.println(" - Game tree nodes number: " + GameTreeUtils.getGameTreeNodesNumber(gameTree));
                columnIndex = getBestColumnIndex(gameTree);

                gameTreeDepth++;
            }*/
        } catch (TimeoutException ex) {
            System.err.println("xxxx Exception xxxx");
            return columnIndex;
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
