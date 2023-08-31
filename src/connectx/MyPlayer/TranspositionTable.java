package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXCell;
import connectx.CXCellState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * A manager of the game decision tree cache.
 * It stores the game decision tree cache, an hashtable
 * with board.toString() as key and a Integer[] array containing
 * {bestChoiceValue, bestChoiceColumnIndex, alpha, beta} as value
 */
public class TranspositionTable {
    private HashMap<String, Integer[]> gameTreeCache;

    public TranspositionTable() {
        gameTreeCache = new HashMap<>();
    }

    public void resetCache() {
        gameTreeCache.clear();
    }

    public void insertValue(CXBoard board, GameChoice gameChoice, int alpha, int beta) {
        Integer[] valueArray = {gameChoice.getValue(), gameChoice.getColumnIndex(), alpha, beta};
        gameTreeCache.put(Arrays.deepToString(board.getBoard()), valueArray);
    }

    public Integer[] getValue(CXBoard board) {
        return gameTreeCache.get(Arrays.deepToString(board.getBoard()));
    }
}
