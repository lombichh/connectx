package connectx.MyPlayer;

import connectx.CXBoard;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A manager of the game decision tree cache.
 * It stores the game decision tree cache, an hashtable
 * with board.toString() as key and a TranspositionTableValue
 * object as value
 */
public class TranspositionTable {
    private HashMap<String, TranspositionTableValue> gameTreeCache;

    public TranspositionTable() {
        gameTreeCache = new HashMap<>();
    }

    public void resetCache() {
        gameTreeCache.clear();
    }

    public void insertValue(CXBoard board, TranspositionTableValue value) {
        gameTreeCache.put(Arrays.deepToString(board.getBoard()), value);
    }

    public TranspositionTableValue getValue(CXBoard board) {
        return gameTreeCache.get(Arrays.deepToString(board.getBoard()));
    }
}
