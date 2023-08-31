package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXCellState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/**
 * A manager of the game decision tree cache.
 * It stores the game decision tree cache, an hashtable
 * with board.toString() as key and a TranspositionTableValue
 * object as value
 */
public class TranspositionTable {
    private HashMap<String, TranspositionTableValue> transpositionTable;

    public TranspositionTable() {
        transpositionTable = new HashMap<>();
    }

    public void reset() {
        transpositionTable.clear();
    }

    public void insertValue(CXBoard board, TranspositionTableValue value) {
        transpositionTable.put(generateKey(board.getBoard()), value);
    }

    public TranspositionTableValue getValue(CXBoard board) {
        return transpositionTable.get(generateKey(board.getBoard()));
    }

    private static String generateKey(CXCellState[][] board) {
        StringBuilder stringBuilder = new StringBuilder();

        for (CXCellState[] row : board) {
            for (CXCellState cell : row) {
                stringBuilder.append(cell);
            }
        }

        return stringBuilder.toString();
    }
}
