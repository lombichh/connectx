package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXCell;
import connectx.CXCellState;
import connectx.CXGameState;

import java.util.concurrent.TimeoutException;

import static connectx.CXGameState.*;

/**
 * Stores methods for evaluating boards.
 */
public class Evaluator {
    private static int alphaBetaCounter;

    public static int WINP1VALUE = 1000000;
    public static int WINP2VALUE = -1000000;
    public static int DRAWVALUE = 0;

    private static final int[] sequenceWeight = {50, 20, 10, 5};

    private static int gameTreeDepth;

    /**
     * Evaluate the available choices of the current board state with
     * increasing game tree depths.
     * Returns a GameChoice object representing the best choice
     * of the greatest depth it managed to evaluate before time runs out.
     */
    public static GameChoice iterativeDeepening(CXBoard board, boolean first, TimeManager timeManager) {
        // select the first available column
        GameChoice bestChoice = new GameChoice(0, board.getAvailableColumns()[0]);

        try {
            // evaluate the tree with increasing depths
            System.err.println("\n---- New move ----");

            int gameTreeMaxDepth = (board.M * board.N) - board.getMarkedCells().length;
            gameTreeDepth = 1;

            TranspositionTable transpositionTable = new TranspositionTable();
            while (gameTreeDepth <= gameTreeMaxDepth) {
                System.err.println("\n - Game tree depth: " + gameTreeDepth);
                transpositionTable.resetCache();

                alphaBetaCounter = 0;
                bestChoice = Evaluator.alphaBeta(board, first, Evaluator.WINP2VALUE,
                        Evaluator.WINP1VALUE, gameTreeDepth, transpositionTable, timeManager);

                System.err.println(" - AlphaBeta counter: " + alphaBetaCounter);
                System.err.println(" - Elapsed time: " + timeManager.getElapsedTime());

                gameTreeDepth++;
            }
        } catch (TimeoutException ex) {
            System.err.println("xxxx Exception xxxx");
        }

        return bestChoice;
    }

    /**
     * Evaluate the game tree to the given depth.
     * Returns a GameChoice object representing the best choice
     * to do with the current state of the board.
     */
    private static GameChoice alphaBeta(CXBoard board, boolean isFirstPlayerTurn,
                                        int alpha, int beta, int depth,
                                        TranspositionTable transpositionTable,
                                        TimeManager timeManager) throws TimeoutException {
        timeManager.checkTime(); // check the time left at every recursive call
        alphaBetaCounter++;

        GameChoice bestChoice = new GameChoice(0, 0);

        TranspositionTableValue transpositionTableValue = transpositionTable.getValue(board); // get value from transposition table

        if (transpositionTableValue != null && transpositionTableValue.getAlpha() == alpha
                && transpositionTableValue.getBeta() == beta) bestChoice = transpositionTableValue.getBestChoice();
        else {
            if (depth <= 0 || board.gameState() != OPEN) {
                bestChoice.setValue(evaluate(board, timeManager));
                bestChoice.setColumnIndex(board.getLastMove().j); // column index of the last move
            } else if (isFirstPlayerTurn) {
                // maximize the choice value
                Integer[] availableColumns = board.getAvailableColumns();
                int columnIndex = 0;

                bestChoice.setValue(WINP2VALUE);
                bestChoice.setColumnIndex(availableColumns[columnIndex]);

                while (columnIndex < availableColumns.length && alpha < beta) {
                    // mark column and check if the value of that choice is the best,
                    // if so change the values of bestChoice
                    board.markColumn(availableColumns[columnIndex]);

                    int currentChoiceValue = alphaBeta(
                            board,
                            false,
                            alpha,
                            beta,
                            depth - 1,
                            transpositionTable,
                            timeManager
                    ).getValue();

                    if (currentChoiceValue > bestChoice.getValue()) {
                        bestChoice.setValue(currentChoiceValue);
                        bestChoice.setColumnIndex(availableColumns[columnIndex]);

                        alpha = Math.max(currentChoiceValue, alpha);
                    }

                    board.unmarkColumn();

                    columnIndex++;
                }
            } else {
                // minimize the choice value
                Integer[] availableColumns = board.getAvailableColumns();
                int columnIndex = 0;

                bestChoice.setValue(WINP1VALUE);
                bestChoice.setColumnIndex(availableColumns[columnIndex]);

                while (columnIndex < availableColumns.length && alpha < beta) {
                    // mark column and check if the value of that choice is the best,
                    // if so change the values of bestChoice
                    board.markColumn(availableColumns[columnIndex]);

                    int currentChoiceValue = alphaBeta(
                            board,
                            true,
                            alpha,
                            beta,
                            depth - 1,
                            transpositionTable,
                            timeManager
                    ).getValue();

                    if (currentChoiceValue < bestChoice.getValue()) {
                        bestChoice.setValue(currentChoiceValue);
                        bestChoice.setColumnIndex(availableColumns[columnIndex]);

                        beta = Math.min(currentChoiceValue, beta);
                    }

                    board.unmarkColumn();

                    columnIndex++;
                }
            }

            // update transposition table
            transpositionTableValue = new TranspositionTableValue(bestChoice, alpha, beta);
            transpositionTable.insertValue(board, transpositionTableValue);
        }

        if (depth == gameTreeDepth) System.err.println("Column: " + bestChoice.getColumnIndex() + ", value: " + bestChoice.getValue());

        return bestChoice;
    }

    /**
     * Calculate and returns the value of the given board.
     */
    private static int evaluate(CXBoard board, TimeManager timeManager)
            throws TimeoutException{
        int nodeEvaluation;

        CXGameState gameState = board.gameState();
        if (gameState == WINP1) nodeEvaluation = WINP1VALUE;
        else if (gameState == WINP2) nodeEvaluation = WINP2VALUE;
        else if (gameState == DRAW) nodeEvaluation = DRAWVALUE;
        else {
            nodeEvaluation = evaluateSequences(board, timeManager);
        }

        return nodeEvaluation;
    }

    /**
     * Returns {P1SequencesValue, P2SequencesValue} based on the value
     * of the sequences of P1 and P2 in the board
     */
    private static int evaluateSequences(CXBoard board, TimeManager timeManager) throws TimeoutException {
        int value = 0;

        CXCellState[][] boardCells = board.getBoard();

        for(CXCell markedCell : board.getMarkedCells()) {
            timeManager.checkTime();

            int cellValue = 0;

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
     * Returns integer value of a sequence in a certain direction
     * starting for a certain cell.
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
                if (openBefore || openAfter) value += sequenceWeight[0];
            } else if (board.X - sequenceLength == 2) {
                if (openBefore || openAfter) value += sequenceWeight[1];
            } else if (board.X - sequenceLength == 3 && board.X > 5) {
                if (openBefore || openAfter) value += sequenceWeight[2];
            } else if (board.X - sequenceLength == 4 && board.X > 7) {
                if (openBefore || openAfter) value += sequenceWeight[3];
            }
            /*if (board.X - sequenceLength <= 4) {
                //if (openBefore || openAfter) value += sequenceWeight[board.X - sequenceLength - 1];
                //if (openAfter) value += sequenceWeight[board.X - sequenceLength - 1];
            }*/
        }

        return value;
    }

}