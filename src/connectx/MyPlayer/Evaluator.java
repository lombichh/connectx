package connectx.MyPlayer;

import connectx.AFLP.Pair;
import connectx.CXBoard;
import connectx.CXCell;
import connectx.CXCellState;
import connectx.CXGameState;

import java.util.ArrayList;
import java.util.PriorityQueue;
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

    private static final int[] mySequenceWeight = {50, 20, 10, 5};
    private static final int[] enemySequenceWeight = {50, 20};

    private static int gameTreeMaxDepth;
    private static int gameTreeDepth;
    private static PriorityQueue<Pair> priorityQueue;

    /**
     * Evaluate the available choices of the current board state with
     * increasing game tree depths.
     * Returns a GameChoice object representing the best choice
     * of the greatest depth it managed to evaluate before time runs out.
     */
    public static int iterativeDeepening(CXBoard board, boolean first, TimeManager timeManager) {
        // initialize bestColumn with the first available column
        int bestColumn = board.getAvailableColumns()[0];

        try {
            // evaluate the tree with increasing depths
            System.err.println("\n---- New move ----");

            gameTreeMaxDepth = (board.M * board.N) - board.getMarkedCells().length;
            gameTreeDepth = 1;

            TranspositionTable transpositionTable = new TranspositionTable();
            priorityQueue = new PriorityQueue<>();
            while (gameTreeDepth <= gameTreeMaxDepth) {
                System.err.println("\n - Game tree depth: " + gameTreeDepth);
                transpositionTable.reset();

                alphaBetaCounter = 0;
                bestColumn = getBestColumn(board, first, gameTreeDepth, transpositionTable, timeManager);

                System.err.println(" - AlphaBeta counter: " + alphaBetaCounter);
                System.err.println(" - Elapsed time: " + timeManager.getElapsedTime());

                gameTreeDepth++;
            }
        } catch (TimeoutException ex) {
            System.err.println("xxxx Exception xxxx");
        }

        return bestColumn;
    }

    private static int getBestColumn(CXBoard board, boolean isFirstPlayerTurn, int depth,
                                            TranspositionTable transpositionTable, TimeManager timeManager) throws TimeoutException{
        int bestValue;
        int bestColumn;

        // columns variables
        Integer[] availableColumns = board.getAvailableColumns();
        ArrayList<Integer> columnsVisited = new ArrayList<>();

        // initialize bestValue and bestColumn
        if (isFirstPlayerTurn) bestValue = WINP2VALUE;
        else bestValue = WINP1VALUE;
        bestColumn = availableColumns[0];

        // start evaluation
        int alpha = WINP2VALUE;
        int beta = WINP1VALUE;

        PriorityQueue<Pair> newPriorityQueue = new PriorityQueue<>(); // priority queue to be used for the next depth

        // evaluate the best evaluated columns in the previous depth before
        while (!priorityQueue.isEmpty() && alpha < beta) {
            int currentColumn = priorityQueue.poll().second;
            board.markColumn(currentColumn);

            int currentChoiceValue = alphaBeta(board, !isFirstPlayerTurn, alpha, beta, depth - 1,
                    transpositionTable, timeManager);

            if (isFirstPlayerTurn) {
                if (currentChoiceValue > bestValue) {
                    bestValue = currentChoiceValue;
                    bestColumn = currentColumn;

                    alpha = Math.max(currentChoiceValue, alpha);
                }
            } else {
                if (currentChoiceValue < bestValue) {
                    bestValue = currentChoiceValue;
                    bestColumn = currentColumn;

                    beta = Math.min(currentChoiceValue, beta);
                }
            }

            newPriorityQueue.offer(new Pair(currentChoiceValue, currentColumn));
            columnsVisited.add(currentColumn);

            board.unmarkColumn();
        }

        // evaluate the remaining column
        int columnIndex = 0;
        while (columnIndex < availableColumns.length && alpha < beta) {
            int currentColumn = availableColumns[columnIndex];

            if (!columnsVisited.contains(currentColumn)) {
                board.markColumn(currentColumn);

                int currentChoiceValue = alphaBeta(board, !isFirstPlayerTurn, alpha, beta, depth - 1,
                        transpositionTable, timeManager);

                if (isFirstPlayerTurn) {
                    if (currentChoiceValue > bestValue) {
                        bestValue = currentChoiceValue;
                        bestColumn = currentColumn;

                        alpha = Math.max(currentChoiceValue, alpha);
                    }
                } else {
                    if (currentChoiceValue < bestValue) {
                        bestValue = currentChoiceValue;
                        bestColumn = currentColumn;

                        beta = Math.min(currentChoiceValue, beta);
                    }
                }

                System.err.println("Column: " + currentColumn);
                newPriorityQueue.offer(new Pair(currentChoiceValue, currentColumn));

                board.unmarkColumn();
            }

            columnIndex++;
        }

        priorityQueue = newPriorityQueue;

        return bestColumn;
    }

    /**
     * Evaluate the game tree to the given depth.
     * Returns a GameChoice object representing the best choice
     * to do with the current state of the board.
     */
    private static int alphaBeta(CXBoard board, boolean isFirstPlayerTurn,
                                        int alpha, int beta, int depth,
                                        TranspositionTable transpositionTable,
                                        TimeManager timeManager) throws TimeoutException {
        timeManager.checkTime(); // check the time left at every recursive call
        alphaBetaCounter++;

        int bestValue;

        Integer bestChoiceInTransTable = transpositionTable.getValue(board, alpha, beta);

        if (bestChoiceInTransTable != null) bestValue = bestChoiceInTransTable; // check transposition table
        else {
            if (depth <= 0 || board.gameState() != OPEN) bestValue = evaluate(board, timeManager);
            else if (isFirstPlayerTurn) {
                // maximize the choice value
                Integer[] availableColumns = board.getAvailableColumns();

                bestValue = WINP2VALUE;

                PriorityQueue<Pair> priorityQueue = new PriorityQueue<>();

                for(int availableColumn : availableColumns){
                    board.markColumn(availableColumn);
                    int eval = evalMove(board, timeManager);
                    board.unmarkColumn();
                    priorityQueue.offer(new Pair(eval, availableColumn));
                }

                while (!priorityQueue.isEmpty() && alpha < beta) {
                    // mark column and check if the value of that choice is the best,
                    // if so change the values of bestChoice
                    int column = priorityQueue.poll().second;
                    board.markColumn(column);

                    int currentChoiceValue = alphaBeta(
                            board,
                            false,
                            alpha,
                            beta,
                            depth - 1,
                            transpositionTable,
                            timeManager
                    );

                    if (currentChoiceValue > bestValue) {
                        bestValue = currentChoiceValue;
                        alpha = Math.max(currentChoiceValue, alpha);
                    }

                    board.unmarkColumn();
                }
            } else {
                // minimize the choice value
                Integer[] availableColumns = board.getAvailableColumns();

                bestValue = WINP1VALUE;

                PriorityQueue<Pair> priorityQueue = new PriorityQueue<>();

                for(int availableColumn : availableColumns){
                    board.markColumn(availableColumn);
                    int eval = evalMove(board, timeManager);
                    board.unmarkColumn();
                    priorityQueue.offer(new Pair(eval, availableColumn));
                }

                while (!priorityQueue.isEmpty() && alpha < beta) {
                    // mark column and check if the value of that choice is the best,
                    // if so change the values of bestChoice
                    int column = priorityQueue.poll().second;
                    board.markColumn(column);

                    int currentChoiceValue = alphaBeta(
                            board,
                            true,
                            alpha,
                            beta,
                            depth - 1,
                            transpositionTable,
                            timeManager
                    );

                    if (currentChoiceValue < bestValue) {
                        bestValue = currentChoiceValue;
                        beta = Math.min(currentChoiceValue, beta);
                    }

                    board.unmarkColumn();
                }
            }

            // update transposition table
            transpositionTable.insertValue(board, alpha, beta, bestValue);
        }

        return bestValue;
    }

    private static int evalMove(CXBoard board, TimeManager timeManager) throws TimeoutException {
        int value = 0;

        CXGameState gameState = board.gameState();
        if(gameState == WINP1 || gameState == WINP2) value = 1000;
        else if(gameState == CXGameState.DRAW) value = 0;
        else {
            CXCellState[][] boardCells = board.getBoard();
            CXCell lastSelectedCell = board.getLastMove();

            int row = lastSelectedCell.i;
            int col = lastSelectedCell.j;

            //controllo orizzontale
            int mySequenceLength = 1;
            int enemySequenceLength = 0;
            int n1 = 1, n2 = 1, n3 = 1, n4 = 1, b1 = 1, b2 = 1;
            int k, plus = 0, eval = 0;

            mySequenceLength += getMyDirectionSequenceLength(board, boardCells, lastSelectedCell, 0, 1); // forward
            mySequenceLength += getMyDirectionSequenceLength(board, boardCells, lastSelectedCell, 0, -1); // backward
            if (mySequenceLength >= board.X - 4) value += mySequenceWeight[board.X - mySequenceLength - 1];

            timeManager.checkTime();

            enemySequenceLength = getEnemyDirectionSequenceLength(board, boardCells, lastSelectedCell, 0, 1); // forward
            if (enemySequenceLength >= board.X - 2) value += enemySequenceWeight[board.X - enemySequenceLength - 1];
            enemySequenceLength = getEnemyDirectionSequenceLength(board, boardCells, lastSelectedCell, 0, -1); // backward
            if (enemySequenceLength >= board.X - 2) value += enemySequenceWeight[board.X - enemySequenceLength - 1];

            //controllo verticale
            mySequenceLength = 1;
            enemySequenceLength = 0;

            mySequenceLength += getMyDirectionSequenceLength(board, boardCells, lastSelectedCell, 1, 0); // forward
            mySequenceLength += getMyDirectionSequenceLength(board, boardCells, lastSelectedCell, -1, 0); // backward
            if (mySequenceLength >= board.X - 4) value += mySequenceWeight[board.X - mySequenceLength - 1];

            timeManager.checkTime();

            enemySequenceLength = getEnemyDirectionSequenceLength(board, boardCells, lastSelectedCell, 1, 0); // forward
            if (enemySequenceLength >= board.X - 2) value += enemySequenceWeight[board.X - enemySequenceLength - 1];
            enemySequenceLength = getEnemyDirectionSequenceLength(board, boardCells, lastSelectedCell, -1, 0); // backward
            if (enemySequenceLength >= board.X - 2) value += enemySequenceWeight[board.X - enemySequenceLength - 1];

            //controllo diagonale
            mySequenceLength = 1; enemySequenceLength = 0;

            mySequenceLength += getMyDirectionSequenceLength(board, boardCells, lastSelectedCell, 1, 1); // forward
            mySequenceLength += getMyDirectionSequenceLength(board, boardCells, lastSelectedCell, -1, -1); // backward
            if (mySequenceLength >= board.X - 4) value += mySequenceWeight[board.X - mySequenceLength - 1];

            timeManager.checkTime();

            enemySequenceLength = getEnemyDirectionSequenceLength(board, boardCells, lastSelectedCell, 1, 1); // forward
            if (enemySequenceLength >= board.X - 2) value += enemySequenceWeight[board.X - enemySequenceLength - 1];
            enemySequenceLength = getEnemyDirectionSequenceLength(board, boardCells, lastSelectedCell, -1, -1); // backward
            if (enemySequenceLength >= board.X - 2) value += enemySequenceWeight[board.X - enemySequenceLength - 1];

            //controllo anti-diagonale
            mySequenceLength = 1; enemySequenceLength = 0;

            mySequenceLength += getMyDirectionSequenceLength(board, boardCells, lastSelectedCell, 1, -1); // forward
            mySequenceLength += getMyDirectionSequenceLength(board, boardCells, lastSelectedCell, -1, 1); // backward
            if (mySequenceLength >= board.X - 4) value += mySequenceWeight[board.X - mySequenceLength - 1];

            timeManager.checkTime();

            enemySequenceLength = getEnemyDirectionSequenceLength(board, boardCells, lastSelectedCell, 1, -1); // forward
            if (enemySequenceLength >= board.X - 2) value += enemySequenceWeight[board.X - enemySequenceLength - 1];
            enemySequenceLength = getEnemyDirectionSequenceLength(board, boardCells, lastSelectedCell, -1, 1); // backward
            if (enemySequenceLength >= board.X - 2) value += enemySequenceWeight[board.X - enemySequenceLength - 1];

            if (col - (board.X - 1) >= 0 && col + (board.X - 1) < board.N) value += 5; // add value for center moves
        }

        return value;
    }

    /**
     * Calculate and returns the value of the given board.
     */
    private static int evaluate(CXBoard board, TimeManager timeManager)
            throws TimeoutException{
        int value;

        CXGameState gameState = board.gameState();
        if (gameState == WINP1) value = WINP1VALUE;
        else if (gameState == WINP2) value = WINP2VALUE;
        else if (gameState == DRAW) value = DRAWVALUE;
        else {
            value = evaluateSequences(board, timeManager);
        }

        return value;
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
                if (openBefore || openAfter) value += mySequenceWeight[0];
            } else if (board.X - sequenceLength == 2) {
                if (openBefore || openAfter) value += mySequenceWeight[1];
            } else if (board.X - sequenceLength == 3 && board.X > 5) {
                if (openBefore || openAfter) value += mySequenceWeight[2];
            } else if (board.X - sequenceLength == 4 && board.X > 7) {
                if (openBefore || openAfter) value += mySequenceWeight[3];
            }
            /*if (board.X - sequenceLength <= 4) {
                //if (openBefore || openAfter) value += sequenceWeight[board.X - sequenceLength - 1];
                //if (openAfter) value += sequenceWeight[board.X - sequenceLength - 1];
            }*/
        }

        return value;
    }

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