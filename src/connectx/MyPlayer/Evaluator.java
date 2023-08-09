package connectx.MyPlayer;

import connectx.CXCell;
import connectx.CXCellState;

import static connectx.CXGameState.*;

/**
 * Stores methods for evaluating game decision tree nodes
 */
public class Evaluator {
    public static int WINP1VALUE = 1000;
    public static int WINP2VALUE = -1000;
    public static int DRAWVALUE = 0;

    /**
     * Calculate and returns the value of the given node
     */
    public static int evaluate(GameTreeNode node) {
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
     * sequences in the board for P1 and P2
     */
    private static int[] evaluateSequences(MyCXBoard board) {
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
     * starting for a certain cell
     */
    private static int evaluateDirectionSequence(MyCXBoard board, CXCell startingCell,
                                                 int rowIncrement, int colIncrement) {
        int directionValue = 0;

        int row = startingCell.i;
        int col = startingCell.j;

        while (row >= 0 && row < board.M && col >= 0 && col < board.N
                && board.getBoard()[row][col] == startingCell.state) {
            directionValue++;
            row += rowIncrement;
            col += colIncrement;
        }

        // The sequence is valuable only if the end of the sequence if free
        if (row >= 0 && row < board.M && col >= 0 && col < board.N
                && board.getBoard()[row][col] == CXCellState.FREE)
            return directionValue;
        else return 0;
    }
}
