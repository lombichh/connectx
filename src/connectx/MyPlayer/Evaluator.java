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
    public static int WINP1VALUE = 1000000;
    public static int WINP2VALUE = -1000000;
    public static int DRAWVALUE = 0;

    /**
     * Returns {nodeValue, colIndex} of the best choice
     */
    public static int[] alphaBeta(CXBoard board, boolean isFirstPlayerTurn, int alpha, int beta, int depth,
                                  TimeManager timeManager) throws TimeoutException {
        timeManager.checkTime(); // Check the time left at every recursive call
        MyPlayer.alphaBetaCounter++;

        int[] bestChoice = new int[2];

        if (depth <= 1 || board.gameState() != OPEN) {
            bestChoice[0] = evaluate(board, board.getBoard(), timeManager);
            bestChoice[1] = board.getLastMove().j; // col index of the last move
        } else if (isFirstPlayerTurn) {
            // maximize the choiceValue
            Integer[] availableColumns = board.getAvailableColumns();
            int columnIndex = 0;

            bestChoice[0] = WINP2VALUE;
            bestChoice[1] = availableColumns[columnIndex];

            while (columnIndex < availableColumns.length && alpha < beta) {
                // mark column and check if the value of that choice is the best,
                // if so change the bestChoice array
                board.markColumn(availableColumns[columnIndex]);

                int choiceValue = alphaBeta(
                        board,
                        false,
                        alpha,
                        beta,
                        depth - 1,
                        timeManager
                )[0];

                if (choiceValue > bestChoice[0]) {
                    bestChoice[0] = choiceValue;
                    bestChoice[1] = availableColumns[columnIndex];

                    alpha = Math.max(choiceValue, alpha);
                }

                board.unmarkColumn();

                columnIndex++;
            }

        } else {
            // minimize the choiceValue
            Integer[] availableColumns = board.getAvailableColumns();
            int columnIndex = 0;

            bestChoice[0] = WINP1VALUE;
            bestChoice[1] = availableColumns[columnIndex];

            while (columnIndex < availableColumns.length && alpha < beta) {
                // mark column and check if the value of that choice is the best,
                // if so change the bestChoice array
                board.markColumn(availableColumns[columnIndex]);

                int choiceValue = alphaBeta(
                        board,
                        true,
                        alpha,
                        beta,
                        depth - 1,
                        timeManager
                )[0];

                if (choiceValue < bestChoice[0]) {
                    bestChoice[0] = choiceValue;
                    bestChoice[1] = availableColumns[columnIndex];

                    beta = Math.min(choiceValue, beta);
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
            cellScore += evaluateDirectionSequence(board, markedCell, -1, -1); // Anti-diagonal
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



    private static int evaluate(CXBoard B, CXCellState[][] board, TimeManager timeManager) throws TimeoutException {
        if(B.gameState() == CXGameState.WINP1) return WINP1VALUE;
        else if(B.gameState() == CXGameState.WINP2) return WINP2VALUE;
        else if(B.gameState() == CXGameState.DRAW) return DRAWVALUE;
        else {
            // valutazione euristica di una situazione di gioco non finale
            // nell'eval assegno punteggi positivi per le sequenze di pedine del player massimizzante
            // e negativi per il minimizzante
            int n = 0, eval;
            int n1 = 0,n2 = 0, n3 = 0, n4 = 0; //n1 = numero di sequenze di lunghezza X-1, X-2 per n2, X-3 per n3, X-4 per n4

            CXCell[] markedCells = B.getMarkedCells();
            int i, j, k;
            boolean enter_check = true, condition1, condition2;

            for(CXCell c : markedCells)
            {
                timeManager.checkTime();
                i = c.i; j = c.j; n = 1;
                enter_check = true; condition1 = false; condition2 = false;

                if(j-1 >= 0){
                    enter_check = (board[i][j-1] != board[i][j]);
                    condition1 = (board[i][j-1] == CXCellState.FREE);
                }
                else condition1 = false;
                for(k = 1; enter_check && j+k < B.N && board[i][j+k] == board[i][j]; k++) n++;
                if(j+k >= B.N){
                    condition2 = false;
                }
                else condition2 = (board[i][j+k] == CXCellState.FREE);
                if(n == B.X - 1 && (condition1 || condition2)){
                    if(board[i][j] == CXCellState.P1) n1++;
                    else n1--;
                }
                else if(n == B.X - 2 && (condition1 || condition2)){
                    if(board[i][j] == CXCellState.P1) n2++;
                    else n2--;
                }
                else if(n == B.X - 3 && (condition1 || condition2) && B.X > 5){
                    if(board[i][j] == CXCellState.P1) n3++;
                    else n3--;
                }
                else if(n == B.X - 4 && (condition1 || condition2) && B.X > 7){
                    if(board[i][j] == CXCellState.P1) n4++;
                    else n4--;
                }

                //controllo verticale
                enter_check = true; condition1 = false; condition2 = false;
                n = 1;
                if(i-1 >= 0){
                    enter_check = (board[i-1][j] != board[i][j]);
                    condition1 = (board[i-1][j] == CXCellState.FREE);
                }
                else condition1 = false;
                for(k = 1; enter_check && i+k < B.M && board[i+k][j] == board[i][j]; k++) n++;
                if(i+k >= B.M) {
                    condition2 = false;
                }
                else condition2 = (board[i+k][j] == CXCellState.FREE);
                if(n == B.X - 1 && (condition1 || condition2)){
                    if(board[i][j] == CXCellState.P1) n1++;
                    else n1--;
                }
                else if(n == B.X - 2 && (condition1 || condition2)){
                    if(board[i][j] == CXCellState.P1) n2++;
                    else n2--;
                }
                else if(n == B.X - 3 && (condition1 || condition2) && B.X > 5){
                    if(board[i][j] == CXCellState.P1) n3++;
                    else n3--;
                }
                else if(n == B.X - 4 && (condition1 || condition2) && B.X > 7){
                    if(board[i][j] == CXCellState.P1) n4++;
                    else n4--;
                }
                timeManager.checkTime();
                //controllo diagonale
                enter_check = true;
                condition1 = false;
                condition2 = false;
                n = 1;
                if(i-1 >= 0 && j-1 >= 0){
                    enter_check = (board[i-1][j-1] != board[i][j]);
                    condition1 = (board[i-1][j-1] == CXCellState.FREE);
                }
                else condition1 = false;
                for(k = 1; enter_check && (i+k < B.M  && j+k < B.N ) && board[i+k][j+k] == board[i][j]; k++) n++;
                if(i+k >= B.M || j+k >= B.N) {
                    condition2 = false;
                }
                else condition2 = (board[i+k][j+k] == CXCellState.FREE);
                if(n == B.X - 1 && (condition1 || condition2)){
                    if(board[i][j] == CXCellState.P1) n1++;
                    else n1--;
                }
                else if(n == B.X - 2 && (condition1 || condition2)){
                    if(board[i][j] == CXCellState.P1) n2++;
                    else n2--;
                }
                else if(n == B.X - 3 && (condition1 || condition2) && B.X > 5){
                    if(board[i][j] == CXCellState.P1) n3++;
                    else n3--;
                }
                else if(n == B.X - 4 && (condition1 || condition2) && B.X > 7){
                    if(board[i][j] == CXCellState.P1) n4++;
                    else n4--;
                }
                //controllo anti-diagonale
                enter_check = true;
                condition1 = false;
                condition2 = false;
                n = 1;
                if(i-1 >= 0 && j+1 < B.N){
                    enter_check = (board[i-1][j+1] != board[i][j]);
                    condition1 = (board[i-1][j+1] == CXCellState.FREE);
                }
                else condition1 = false;
                for(k = 1; enter_check && (i+k < B.M  && j-k >= 0) && board[i+k][j-k] == board[i][j]; k++) n++;
                if(i+k >= B.M || j-k < 0) {
                    condition2 = false;
                }
                else condition2 = (board[i+k][j-k] == CXCellState.FREE);
                if(n == B.X - 1 && (condition1 || condition2)){
                    if(board[i][j] == CXCellState.P1) n1++;
                    else n1--;
                }
                else if(n == B.X - 2 && (condition1 || condition2)){
                    if(board[i][j] == CXCellState.P1) n2++;
                    else n2--;
                }
                else if(n == B.X - 3 && (condition1 || condition2) && B.X > 5){
                    if(board[i][j] == CXCellState.P1) n3++;
                    else n3--;
                }
                else if(n == B.X - 4 && (condition1 || condition2) && B.X > 7){
                    if(board[i][j] == CXCellState.P1) n4++;
                    else n4--;
                }
            }

            eval = n1 * 50 + n2 * 20 + n3 * 10 + n4 * 5;
            return eval;
        }
    }


}
