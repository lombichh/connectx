package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXCell;
import connectx.CXCellState;

import java.util.concurrent.TimeoutException;

import static connectx.CXGameState.*;

/**
 * Stores methods for evaluating boards.
 */
public class Evaluator {
    public static int WINP1VALUE = 1000;
    public static int WINP2VALUE = -1000;
    public static int DRAWVALUE = 0;

    /**
     * Returns the alphaBeta value of the given board.
     */
    public static int alphaBeta(CXBoard board, boolean isFirstPlayerTurn, int alpha, int beta, int depth,
                                GameTreeCacheManager gameTreeCacheManager,
                                TimeManager timeManager) throws TimeoutException {
        timeManager.checkTime(); // Check the time left at every recursive call
        MyPlayer.alphaBetaCounter++;

        int nodeValue;

        Integer nodeValueInCache = gameTreeCacheManager.getNodeValue(board); // Check cache

        if (nodeValueInCache != null) nodeValue = nodeValueInCache;
        else {
            if (depth <= 1 || board.gameState() != OPEN) nodeValue = evaluate(board);
            else if (isFirstPlayerTurn) {
                nodeValue = WINP2VALUE;

                Integer[] availableColumns = board.getAvailableColumns();
                int availableColumnIndex = 0;
                while (availableColumnIndex < availableColumns.length && alpha < beta) {
                    board.markColumn(availableColumns[availableColumnIndex]);

                    nodeValue = Math.max(
                            nodeValue,
                            alphaBeta(
                                    board,
                                    false,
                                    alpha,
                                    beta,
                                    depth - 1,
                                    gameTreeCacheManager,
                                    timeManager
                            )
                    );
                    alpha = Math.max(nodeValue, alpha);
                    availableColumnIndex++;

                    board.unmarkColumn();
                }
            } else {
                nodeValue = WINP1VALUE;

                Integer[] availableColumns = board.getAvailableColumns();
                int availableColumnIndex = 0;
                while (availableColumnIndex < availableColumns.length && beta > alpha) {
                    board.markColumn(availableColumns[availableColumnIndex]);

                    nodeValue = Math.min(
                            nodeValue,
                            alphaBeta(
                                    board,
                                    true,
                                    alpha,
                                    beta,
                                    depth - 1,
                                    gameTreeCacheManager,
                                    timeManager
                            )
                    );
                    beta = Math.min(nodeValue, beta);
                    availableColumnIndex++;

                    board.unmarkColumn();
                }
            }

            gameTreeCacheManager.insertNode(board, nodeValue);
        }

        return nodeValue;
    }

    /**
     * Calculate and returns the value of the given board.
     */
    private static int evaluate(CXBoard board) {
        int nodeEvaluation;

        if (board.gameState() == WINP1) nodeEvaluation = WINP1VALUE;
        else if (board.gameState() == WINP2) nodeEvaluation = WINP2VALUE;
        else if (board.gameState() == DRAW) nodeEvaluation = DRAWVALUE;
        else {
            // The game is in an open state, evaluate it
            int[] playerValues = evaluateSequences(board);
            nodeEvaluation = playerValues[0] - playerValues[1]; // P1Value - P2Value
        }

        return nodeEvaluation;
    }

    /**
     * Returns {P1SequencesValue, P2SequencesValue} based on how many
     * sequences in the board for P1 and P2.
     */
    private static int[] evaluateSequences(CXBoard board) {
        int[] playerSequences = {0, 0};

        for (CXCell markedCell : board.getMarkedCells()) {
            int cellScore = 0;
            // Forward
            cellScore += evaluateDirectionSequence(board, markedCell, 0, 1); // Horizontal
            cellScore += evaluateDirectionSequence(board, markedCell, -1, 0); // Vertical
            cellScore += evaluateDirectionSequence(board, markedCell, -1, 1); // Diagonal
            cellScore += evaluateDirectionSequence(board, markedCell, -1, -1); // Anti-diagonal;
            // Backward
            cellScore += evaluateDirectionSequence(board, markedCell, 0, -1); // Horizontal
            cellScore += evaluateDirectionSequence(board, markedCell, 1, 0); // Vertical
            cellScore += evaluateDirectionSequence(board, markedCell, 1, -1); // Diagonal
            cellScore += evaluateDirectionSequence(board, markedCell, 1, 1); // Anti-diagonal

            if (markedCell.state == CXCellState.P1) playerSequences[0] += cellScore;
            else playerSequences[1] += cellScore;
        }

        return playerSequences;
    }

    /**
     * Returns integer value of a sequence in a certain direction
     * starting for a certain cell.
     */
    private static int evaluateDirectionSequence(CXBoard board, CXCell startingCell,
                                                 int rowIncrement, int colIncrement) {
        int directionSequenceValue = 0;

        int row = startingCell.i;
        int col = startingCell.j;

        while (row >= 0 && row < board.M && col >= 0 && col < board.N
                && board.getBoard()[row][col] == startingCell.state) {
            directionSequenceValue++;
            row += rowIncrement;
            col += colIncrement;
        }

        // The sequence is valuable only if the end of the sequence if free
        if (row >= 0 && row < board.M && col >= 0 && col < board.N
                && board.getBoard()[row][col] == CXCellState.FREE)
            return directionSequenceValue;
        else return 0;
    }


}
