package connectx.LombEnis;

import connectx.CXBoard;
import connectx.CXCell;
import connectx.CXCellState;
import connectx.CXGameState;

import java.util.concurrent.TimeoutException;

import static connectx.CXGameState.*;

/**
 * Stores methods for evaluating game states.
 */
public class Evaluator {
    private static final int[] mySequenceWeight = {100, 40, 20, 10};
    private static final int[] enemySequenceWeight = {100, 40};

    public static int WINP1VALUE = 10000;
    public static int WINP2VALUE = -10000;
    public static int DRAWVALUE = 0;

    /* Evaluation methods */

    /**
     * Calculate and returns the value of the given board.
     */
    public static int evaluate(CXBoard board, TimeManager timeManager)
            throws TimeoutException {
        int value;

        CXGameState gameState = board.gameState();
        if (gameState == WINP1) value = WINP1VALUE;
        else if (gameState == WINP2) value = WINP2VALUE;
        else if (gameState == DRAW) value = DRAWVALUE;
        else value = evaluateBoard(board, timeManager);

        return value;
    }

    /**
     * Returns the value of the board evaluating all the
     * player and enemy sequences in all directions.
     */
    private static int evaluateBoard(CXBoard board, TimeManager timeManager) throws TimeoutException {
        int value = 0;

        CXCellState[][] boardCells = board.getBoard();

        // loop through all the marked cells to find sequences
        for (CXCell markedCell : board.getMarkedCells()) {
            timeManager.checkTime();

            int cellValue = 0;

            // evaluate all the possible sequence in every direction
            cellValue += evaluateDirectionSequence(board, boardCells, markedCell, 0, 1); // horizontal
            cellValue += evaluateDirectionSequence(board, boardCells, markedCell, 1, 0); // vertical

            timeManager.checkTime();

            cellValue += evaluateDirectionSequence(board, boardCells, markedCell, 1, 1); // diagonal
            cellValue += evaluateDirectionSequence(board, boardCells, markedCell, 1, -1); // anti-diagonal

            if (markedCell.state == CXCellState.P1) value += cellValue;
            else value -= cellValue;
        }

        return value;
    }

    /**
     * Returns the value of a sequence in a certain direction
     * starting from a certain cell.
     */
    private static int evaluateDirectionSequence(CXBoard board, CXCellState[][] boardCells, CXCell startingCell,
                                                 int rowIncrement, int colIncrement) {
        int value = 0;

        int row = startingCell.i;
        int col = startingCell.j;

        // check if the cell before the startingCell is inside the board
        boolean isCellBeforeInsideBoard = row - rowIncrement >= 0 && col - colIncrement >= 0
                && col - colIncrement < board.N;

        // check if the markedCell is the first of the sequence
        boolean isFirstOfSequence;
        if (isCellBeforeInsideBoard) isFirstOfSequence =
                boardCells[row - rowIncrement][col - colIncrement] != boardCells[row][col];
        else isFirstOfSequence = true;

        // if firstOfSequence evaluate the sequence, otherwise the sequence has already been evaluated
        if (isFirstOfSequence) {
            // check if there is a free cell before the sequence
            boolean openBefore;
            if (isCellBeforeInsideBoard) openBefore =
                    boardCells[row - rowIncrement][col - colIncrement] == CXCellState.FREE;
            else openBefore = false;

            // calculate the length of the sequence
            int sequenceLength = 1;
            while (row + rowIncrement < board.M && col + colIncrement < board.N && col + colIncrement >= 0
                    && boardCells[row + rowIncrement][col + colIncrement] == boardCells[row][col]) {
                sequenceLength++;
                row += rowIncrement;
                col += colIncrement;
            }

            // check if the cell after the sequence is inside the board
            boolean isCellAfterInsideBoard = row + rowIncrement < board.M && col + colIncrement < board.N
                    && col + colIncrement >= 0;

            // check if there is a free cell after the sequence
            boolean openAfter;
            if (isCellAfterInsideBoard) openAfter =
                    boardCells[row + rowIncrement][col + colIncrement] == CXCellState.FREE;
            else openAfter = false;

            // update the value if the sequence is long enough and if it is open before or open after
            if (board.X - sequenceLength == 1) {
                if (openBefore || openAfter) value += mySequenceWeight[0];
            } else if (board.X - sequenceLength == 2) {
                if (openBefore || openAfter) value += mySequenceWeight[1];
            } else if (board.X - sequenceLength == 3 && board.X > 5) {
                if (openBefore || openAfter) value += mySequenceWeight[2];
            } else if (board.X - sequenceLength == 4 && board.X > 7) {
                if (openBefore || openAfter) value += mySequenceWeight[3];
            }
        }

        return value;
    }

    /* Pre-evaluation methods */

    /**
     * Returns the value of the board evaluating the position of the last marked cell
     */
    public static int preEvaluate(CXBoard board, TimeManager timeManager) throws TimeoutException {
        int value;

        CXGameState gameState = board.gameState();
        if (gameState == WINP1 || gameState == WINP2) value = 10000;
        else if (gameState == CXGameState.DRAW) value = 0;
        else value = evaluateCell(board, timeManager);

        return value;
    }

    /**
     * Returns the value of the board evaluating
     * player and enemy sequences starting from the last marked cell.
     */
    private static int evaluateCell(CXBoard board, TimeManager timeManager) throws TimeoutException {
        int value = 0;

        CXCellState[][] boardCells = board.getBoard();
        CXCell lastMarkedCell = board.getLastMove();

        // evaluate all the possible sequence in every direction
        value += evaluateDirectionCell(board, boardCells, lastMarkedCell, 0, 1); // horizontal
        timeManager.checkTime();
        value += evaluateDirectionCell(board, boardCells, lastMarkedCell, 1, 0); // vertical
        timeManager.checkTime();
        value += evaluateDirectionCell(board, boardCells, lastMarkedCell, 1, 1); // diagonal
        timeManager.checkTime();
        value += evaluateDirectionCell(board, boardCells, lastMarkedCell, 1, -1); // antidiagonal
        timeManager.checkTime();

        // additional points if the position of the cell is central
        if (lastMarkedCell.j - (board.X - 1) >= 0
                && lastMarkedCell.j + (board.X - 1) < board.N)
            value += 5;

        return value;
    }

    /**
     * Returns the value of all the sequences in a certain direction
     * starting from a certain cell.
     */
    private static int evaluateDirectionCell(CXBoard board, CXCellState[][] boardCells, CXCell startingCell,
                                             int rowIncrement, int colIncrement) {
        int value = 0;

        int mySequenceLength;
        int enemySequenceLength;

        mySequenceLength = 1;
        mySequenceLength += getMyDirectionSequenceLength(board, boardCells, startingCell, rowIncrement, colIncrement); // forward
        mySequenceLength += getMyDirectionSequenceLength(board, boardCells, startingCell, -rowIncrement, -colIncrement); // backward
        if (mySequenceLength >= board.X - 4) value += mySequenceWeight[board.X - mySequenceLength - 1];

        enemySequenceLength = getEnemyDirectionSequenceLength(board, boardCells, startingCell, rowIncrement, colIncrement); // forward
        if (enemySequenceLength >= board.X - 2) value += enemySequenceWeight[board.X - enemySequenceLength - 1];
        enemySequenceLength = getEnemyDirectionSequenceLength(board, boardCells, startingCell, -rowIncrement, -colIncrement); // backward
        if (enemySequenceLength >= board.X - 2) value += enemySequenceWeight[board.X - enemySequenceLength - 1];

        return value;
    }

    /**
     * Returns the length of one of my sequences in a certain direction
     * starting from a certain cell.
     */
    private static int getMyDirectionSequenceLength(CXBoard board, CXCellState[][] boardCells, CXCell startingCell,
                                                    int rowIncrement, int colIncrement) {
        int sequenceLength = 0;

        int row = startingCell.i;
        int col = startingCell.j;

        while (row + rowIncrement < board.M && col + colIncrement < board.N
                && col + colIncrement >= 0 && row + rowIncrement >= 0
                && boardCells[row + rowIncrement][col + colIncrement] == boardCells[row][col]) {
            sequenceLength++;
            row += rowIncrement;
            col += colIncrement;
        }

        return sequenceLength;
    }

    /**
     * Returns the length of one of enemy sequences in a certain direction
     * starting from a certain cell.
     */
    private static int getEnemyDirectionSequenceLength(CXBoard board, CXCellState[][] boardCells, CXCell startingCell,
                                                       int rowIncrement, int colIncrement) {
        int sequenceLength = 0;

        int row = startingCell.i;
        int col = startingCell.j;

        while (row + rowIncrement < board.M && col + colIncrement < board.N
                && col + colIncrement >= 0 && row + rowIncrement >= 0
                && boardCells[row + rowIncrement][col + colIncrement] != boardCells[startingCell.i][startingCell.j]
                && boardCells[row + rowIncrement][col + colIncrement] != CXCellState.FREE) {
            sequenceLength++;
            row += rowIncrement;
            col += colIncrement;
        }

        return sequenceLength;
    }

}