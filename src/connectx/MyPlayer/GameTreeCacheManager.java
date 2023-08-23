package connectx.MyPlayer;

import connectx.CXBoard;

import java.util.Arrays;
import java.util.Hashtable;

/**
 * A manager of the game decision tree cache.
 * It stores the game decision tree cache, an hashtable
 * with board.toString() as key and a GameChoice object
 * as value representing the best choice to make from
 * that board state.
 */
public class GameTreeCacheManager {
    private Hashtable<String, GameChoice> gameTreeCache;

    public GameTreeCacheManager() {
        gameTreeCache = new Hashtable<>();
    }

    public void resetCache() {
        gameTreeCache.clear();
    }

    public void insertBestChoice(CXBoard board, GameChoice gameChoice) {
        gameTreeCache.put(Arrays.deepToString(board.getBoard()), gameChoice);
    }

    public GameChoice getBestChoice(CXBoard board) {
        return gameTreeCache.get(Arrays.deepToString(board.getBoard()));
    }

    private String generateKey(CXBoard board) {
        StringBuilder keyBuilder = new StringBuilder();

        for (int row = 0; row < board.M; row++) {
            for (int col = 0; col < board.N; col++) {
                keyBuilder.append(board.getBoard()[row][col].toString());
            }
        }

        return keyBuilder.toString();
    }
}
