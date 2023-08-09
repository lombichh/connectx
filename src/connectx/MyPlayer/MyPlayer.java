package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXPlayer;

import java.util.ArrayList;

public class MyPlayer implements CXPlayer {
    private boolean first;
    private Integer[] availableColumns;

    public static int alphaBetaCounter;

    public MyPlayer() {}

    @Override
    public void initPlayer(int M, int N, int X, boolean first, int timeout_in_secs) {
        this.first = first;
    }

    /**
     * Returns the index of the column to be selected.
     */
    @Override
    public int selectColumn(CXBoard B) {
        this.availableColumns = B.getAvailableColumns();

        // Create the game decision tree
        GameTreeNode gameTree = GameTreeUtils.createGameTreeCaller(B, 1000);
        System.err.println("Game decision tree nodes number: " + GameTreeUtils.getGameTreeNodesNumber(gameTree));

        return getBestColumnIndex(gameTree);
    }

    public String playerName() {
        return "MyPlayer";
    }

    /**
     * Returns the index of the column with the best value.
     */
    private int getBestColumnIndex(GameTreeNode gameTree) {
        alphaBetaCounter = 0;

        // Initialize maxValue with the value of the first available column
        ArrayList<GameTreeNode> childNodes = gameTree.getChildNodes();
        int colValue = Evaluator.alphaBeta(childNodes.get(0), !first, Evaluator.WINP2VALUE, Evaluator.WINP1VALUE, 1000);
        int columnIndex = availableColumns[0];

        // Get the index of the column with the best value by calling minimax on every available column
        for (int i = 1; i < childNodes.size(); i++) {
            int nodeValue = Evaluator.alphaBeta(childNodes.get(i), !first, Evaluator.WINP2VALUE, Evaluator.WINP1VALUE, 1000);

            // If first player maximize, otherwise minimize
            if (first) {
                if (nodeValue > colValue) {
                    colValue = nodeValue;
                    columnIndex = availableColumns[i];
                }
            } else {
                if (nodeValue < colValue) {
                    colValue = nodeValue;
                    columnIndex = availableColumns[i];
                }
            }
        }

        System.err.println("Minimax counter: " + alphaBetaCounter);

        return columnIndex;
    }
}
