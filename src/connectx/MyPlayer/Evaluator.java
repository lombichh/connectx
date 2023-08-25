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

    public static int WINP1VALUE = 10000;
    public static int WINP2VALUE = -10000;
    public static int DRAWVALUE = 0;

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
            int gameTreeDepth = 1;

            while (gameTreeDepth <= gameTreeMaxDepth) {
                System.err.println("\n - Game tree depth: " + gameTreeDepth);

                alphaBetaCounter = 0;
                bestChoice = Evaluator.alphaBeta(board, first, Evaluator.WINP2VALUE,
                        Evaluator.WINP1VALUE, gameTreeDepth, timeManager);

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
                                        TimeManager timeManager) throws TimeoutException {
        timeManager.checkTime(); // check the time left at every recursive call
        alphaBetaCounter++;

        GameChoice bestChoice = new GameChoice(0, 0);

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
            int[] playerValues = evaluateSequences(board, timeManager);
            nodeEvaluation = playerValues[0] - playerValues[1]; // P1Value - P2Value
        }

        return nodeEvaluation;
    }

    /**
     * Returns {P1SequencesValue, P2SequencesValue} based on the value
     * of the sequences of P1 and P2 in the board
     */
    private static int[] evaluateSequences(CXBoard board, TimeManager timeManager) throws TimeoutException {
        int[] playerSequencesValues = {0, 0};

        CXCellState[][] boardCells = board.getBoard();

        for(CXCell markedCell : board.getMarkedCells()) {
            timeManager.checkTime();

            int cellScore = 0;

            cellScore += evaluateDirectionSequence(board, boardCells, markedCell, 0, 1); // horizontal
            cellScore += evaluateDirectionSequence(board, boardCells, markedCell, 1, 0); // vertical
            timeManager.checkTime();
            cellScore += evaluateDirectionSequence(board, boardCells, markedCell, 1, 1); // diagonal
            cellScore += evaluateDirectionSequence(board, boardCells, markedCell, 1, -1); // anti-diagonal

            if (markedCell.state == CXCellState.P1) playerSequencesValues[0] += cellScore;
            else playerSequencesValues[1] += cellScore;
        }

        return playerSequencesValues;
    }

    /**
     * Returns integer value of a sequence in a certain direction
     * starting for a certain cell.
     */
    private static int evaluateDirectionSequence(CXBoard board, CXCellState[][] boardCells, CXCell startingCell,
                                                 int rowIncrement, int colIncrement) {
        int sequenceValue = 0;

        int row = startingCell.i;
        int col = startingCell.j;

        // check if the cell before the startingCell is inside the board
        boolean isCellBeforeInsideBoard = row - rowIncrement >= 0 && col - colIncrement >= 0
                && col - colIncrement < board.N;

        // check if the markedCell is the first of the sequence
        boolean firstOfSequence;
        if (isCellBeforeInsideBoard) firstOfSequence =
                boardCells[row - rowIncrement][col - colIncrement] != boardCells[row][col];
        else firstOfSequence = true;

        // if firstOfSequence evaluate the sequence, otherwise the sequence has already been evaluated
        if (firstOfSequence) {
            // check if there is a free cell before the sequence
            boolean openBefore;
            if (isCellBeforeInsideBoard) openBefore =
                    boardCells[row - rowIncrement][col - colIncrement] == CXCellState.FREE;
            else openBefore = false;

            // evaluate the sequence
            int value = 5;
            while (row + rowIncrement < board.M && col + colIncrement < board.N && col + colIncrement >= 0
                    && boardCells[row + rowIncrement][col + colIncrement] == boardCells[row][col]) {
                value *= 3;
                row += rowIncrement;
                col += colIncrement;
            }

            // check if there is a free cell after the sequence
            boolean openAfter;
            if (row < board.M && col < board.N && col >= 0) openAfter = boardCells[row][col] == CXCellState.FREE;
            else openAfter = false;

            // update the sequenceValue based on openBefore and openAfter
            if (openBefore) sequenceValue += value;
            if (openAfter) sequenceValue += value;
        }

        return sequenceValue;
    }

}
