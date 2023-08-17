package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXCell;
import connectx.CXCellState;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static connectx.CXGameState.*;

/**
 * Stores methods for evaluating game decision tree nodes.
 */
public class Evaluator {
    public static int WINP1VALUE = 1000;
    public static int WINP2VALUE = -1000;
    public static int DRAWVALUE = 0;

    /**
     * Returns the alphaBeta value of the given node.
     */
    public static int alphaBeta(GameTreeNode node, boolean isFirstPlayerTurn, int alpha, int beta, int depth,
                                GameTreeCacheManager gameTreeCacheManager,
                                TimeManager timeManager) throws TimeoutException {
        timeManager.checkTime(); // Check the time left at every recursive call
        MyPlayer.alphaBetaCounter++;

        int nodeValue;

        Integer nodeValueInCache = gameTreeCacheManager.getNodeValue(node); // Check cache

        if (nodeValueInCache != null) nodeValue = nodeValueInCache;
        else {
            if (depth <= 0 || GameTreeUtils.isLeaf(node)) nodeValue = Evaluator.evaluate(node);
            else if (isFirstPlayerTurn) {
                ArrayList<GameTreeNode> childNodes = node.getChildNodes();

                nodeValue = WINP2VALUE;

                int childIndex = 0;
                while (childIndex < childNodes.size() && alpha < beta) {
                    nodeValue = Math.max(
                            nodeValue,
                            alphaBeta(
                                    childNodes.get(childIndex),
                                    false,
                                    alpha,
                                    beta,
                                    depth - 1,
                                    gameTreeCacheManager,
                                    timeManager
                            )
                    );
                    alpha = Math.max(nodeValue, alpha);
                    childIndex++;
                }
            } else {
                ArrayList<GameTreeNode> childNodes = node.getChildNodes();

                nodeValue = WINP1VALUE;

                int childIndex = 0;
                while (childIndex < childNodes.size() && beta > alpha) {
                    nodeValue = Math.min(
                            nodeValue,
                            alphaBeta(
                                    childNodes.get(childIndex),
                                    true,
                                    alpha,
                                    beta,
                                    depth - 1,
                                    gameTreeCacheManager,
                                    timeManager
                            )
                    );
                    beta = Math.min(nodeValue, beta);
                    childIndex++;
                }
            }

            gameTreeCacheManager.insertNode(node, nodeValue);
        }

        return nodeValue;
    }

    /**
     * Calculate and returns the value of the given node.
     */
    private static int evaluate(GameTreeNode node) {
        int nodeEvaluation;

        if (node.getBoard().gameState() == WINP1) nodeEvaluation = WINP1VALUE;
        else if (node.getBoard().gameState() == WINP2) nodeEvaluation = WINP2VALUE;
        else if (node.getBoard().gameState() == DRAW) nodeEvaluation = DRAWVALUE;
        else {
            // The game is in an open state, evaluate it
            int[] playerValues = evaluateSequences(node.getBoard());
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
