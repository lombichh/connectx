package connectx.MyPlayer;

import connectx.CXBoard;

/**
 * Stores methods for creating and managing the game decision tree.
 */
public class GameTreeUtils {
    /**
     * Returns the max depth of the game tree given the current board
     */
    public static int getGameTreeMaxDepth(CXBoard board) {
        return (board.M * board.N) - board.getMarkedCells().length + 1;
    }
}
