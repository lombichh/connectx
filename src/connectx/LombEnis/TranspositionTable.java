package connectx.LombEnis;

import connectx.CXBoard;
import connectx.CXCellState;

import java.util.HashMap;

/**
 * A manager of the game decision tree cache.
 * It stores the game decision tree cache, an hashtable
 * with board.toString() as key and a TranspositionTableValue
 * object as value
 */
public class TranspositionTable {
    private final HashMap<String, Integer> transpositionTable;

    public TranspositionTable() {
        transpositionTable = new HashMap<>();
    }

    private static String generateKey(CXCellState[][] board, int alpha, int beta) {
        StringBuilder stringBuilder = new StringBuilder();

        for (CXCellState[] row : board) {
            for (CXCellState cell : row) {
                stringBuilder.append(cell);
            }
        }

        stringBuilder.append(alpha);
        stringBuilder.append(beta);

        return stringBuilder.toString();
    }

    public void reset() {
        transpositionTable.clear();
    }

    public void insertValue(CXBoard board, int alpha, int beta, Integer value) {
        transpositionTable.put(generateKey(board.getBoard(), alpha, beta), value);
    }

    public Integer getValue(CXBoard board, int alpha, int beta) {
        return transpositionTable.get(generateKey(board.getBoard(), alpha, beta));
    }
}
