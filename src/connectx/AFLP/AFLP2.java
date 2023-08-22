/*
 *  Copyright (C) 2022 Lamberto Colazzo
 *
 *  This file is part of the ConnectX software developed for the
 *  Intern ship of the course "Information technology", University of Bologna
 *  A.Y. 2021-2022.
 *
 *  ConnectX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This  is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details; see <https://www.gnu.org/licenses/>.
 */

package connectx.AFLP;

import connectx.*;

import java.util.concurrent.TimeoutException;

public class AFLP2 implements CXPlayer {
    private int minimaxCounter = 0;

    private CXGameState myWin;
    private CXGameState yourWin;
    private CXCellState maximizingCellState;
    private boolean isMaximizing;
    private int  TIMEOUT;
    private long START;
    private int DEPTH;

    /* Default empty constructor */
    public AFLP2() { }

    public void initPlayer(int M, int N, int X, boolean first, int timeout_in_secs) {
        myWin   = first ? CXGameState.WINP1 : CXGameState.WINP2;
        yourWin = first ? CXGameState.WINP2 : CXGameState.WINP1;
        maximizingCellState = first ? CXCellState.P1 : CXCellState.P2;
        isMaximizing = true;
        TIMEOUT = timeout_in_secs;
    }

    private void checktime() throws TimeoutException {
        if ((System.currentTimeMillis() - START) / 1000.0 >= TIMEOUT * (90.0 / 100.0))
            throw new TimeoutException();
    }

    public String playerName() {
        return "AFLP";
    }

    private int[] AlphaBeta_Pruning(CXBoard B, boolean isMax, int alpha, int beta, int depth) throws TimeoutException {
        checktime();
        minimaxCounter++;
        int[] choice = new int[2];

        if(depth == 0 || B.gameState() != CXGameState.OPEN){
            choice[0] = evaluate(B, B.getBoard());  choice[1] = B.getLastMove().j;
            return choice;
        }
        else if(isMax){
            choice[0] = -1000000;                     //eval
            choice[1] = B.getAvailableColumns()[0];

            int len = B.getAvailableColumns().length;

            for(int i = 0; i < len; i++){
                B.markColumn(B.getAvailableColumns()[i]);
                int tmp = AlphaBeta_Pruning(B, false, alpha, beta, depth-1)[0];
                if (choice[0] < tmp){
                    choice[0] = tmp;
                    choice[1] = B.getAvailableColumns()[i];
                }
                B.unmarkColumn();
                if(choice[0] > alpha)
                    alpha = choice[0];

                if(alpha >= beta){      //cutoff Beta
                    break;
                }
            }

            return choice;
        }
        else{
            choice[0] = 1000000;
            choice[1] = B.getAvailableColumns()[0];

            int len = B.getAvailableColumns().length;

            for(int i = 0; i < len; i++){
                B.markColumn(B.getAvailableColumns()[i]);
                int tmp = AlphaBeta_Pruning(B, true, alpha, beta, depth-1)[0];
                if (choice[0] > tmp){
                    choice[0] = tmp;
                    choice[1] = B.getAvailableColumns()[i];
                }
                B.unmarkColumn();
                if(choice[0] < beta)
                    beta = choice[0];

                if(alpha >= beta){      //cutoff Alpha
                    break;
                }
            }
            return choice;
        }
    }

    private int evaluate(CXBoard B, CXCellState[][] board) throws TimeoutException {
        if(B.gameState() == myWin)
            return 1000000;
        else if(B.gameState() == yourWin)
            return -1000000;
        else if(B.gameState() == CXGameState.DRAW)
            return 0;
        else{
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
                    if(board[i][j] == maximizingCellState) n1++;
                    else n1--;
                }
                else if(n == B.X - 2 && (condition1 || condition2)){
                    if(board[i][j] == maximizingCellState) n2++;
                    else n2--;
                }
                else if(n == B.X - 3 && (condition1 || condition2) && B.X > 5){
                    if(board[i][j] == maximizingCellState) n3++;
                    else n3--;
                }
                else if(n == B.X - 4 && (condition1 || condition2) && B.X > 7){
                    if(board[i][j] == maximizingCellState) n4++;
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
                    if(board[i][j] == maximizingCellState) n1++;
                    else n1--;
                }
                else if(n == B.X - 2 && (condition1 || condition2)){
                    if(board[i][j] == maximizingCellState) n2++;
                    else n2--;
                }
                else if(n == B.X - 3 && (condition1 || condition2) && B.X > 5){
                    if(board[i][j] == maximizingCellState) n3++;
                    else n3--;
                }
                else if(n == B.X - 4 && (condition1 || condition2) && B.X > 7){
                    if(board[i][j] == maximizingCellState) n4++;
                    else n4--;
                }
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
                    if(board[i][j] == maximizingCellState) n1++;
                    else n1--;
                }
                else if(n == B.X - 2 && (condition1 || condition2)){
                    if(board[i][j] == maximizingCellState) n2++;
                    else n2--;
                }
                else if(n == B.X - 3 && (condition1 || condition2) && B.X > 5){
                    if(board[i][j] == maximizingCellState) n3++;
                    else n3--;
                }
                else if(n == B.X - 4 && (condition1 || condition2) && B.X > 7){
                    if(board[i][j] == maximizingCellState) n4++;
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
                    if(board[i][j] == maximizingCellState) n1++;
                    else n1--;
                }
                else if(n == B.X - 2 && (condition1 || condition2)){
                    if(board[i][j] == maximizingCellState) n2++;
                    else n2--;
                }
                else if(n == B.X - 3 && (condition1 || condition2) && B.X > 5){
                    if(board[i][j] == maximizingCellState) n3++;
                    else n3--;
                }
                else if(n == B.X - 4 && (condition1 || condition2) && B.X > 7){
                    if(board[i][j] == maximizingCellState) n4++;
                    else n4--;
                }
            }

            eval = n1 * 50 + n2 * 20 + n3 * 10 + n4 * 5;
            return eval;
        }
    }


    private int[] iterativeDeepening(CXBoard B, boolean isMax, int depth) throws TimeoutException  {
        int[] choice = new int[2];
        int alpha = -1000000, beta = 1000000;
        try{
            for(int i = 1; i < depth; i++){
                checktime();
                minimaxCounter = 0;
                System.err.println("- Depth: " + (i + 1));
                choice = AlphaBeta_Pruning(B, isMax, alpha, beta, i);
                System.err.println("- Minimax counter: " + minimaxCounter);
                System.err.println("- Elapsed time: " + (System.currentTimeMillis() - START) / 1000.0);
            }
        }
        catch (TimeoutException e) {
            //System.err.println("Tempo quasi finito, ritorno una scelta a profondità minore!");
            return choice;
        }

        return choice;
    }

    @Override
    public int selectColumn(CXBoard B){
        START = System.currentTimeMillis(); // Save starting time
        DEPTH = 100;
        int out = 0;

        try {out = iterativeDeepening(B, isMaximizing, DEPTH)[1];}
        catch (TimeoutException e) {}
        return out;
    }
}

