package connectx.MyPlayer;

import connectx.AFLP.Pair;
import connectx.CXBoard;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.TimeoutException;

import static connectx.CXGameState.OPEN;

public class MiniMax {
    private static int alphaBetaCounter;

    private static PriorityQueue<Pair> preEvaluationPriorityQueue1;


    /**
     * Evaluate the available choices of the current board state with
     * increasing game tree depths.
     * Returns the index of the best column with the greatest depth that
     * it was able to evaluate before timeout.
     */
    public static int iterativeDeepening(CXBoard board, boolean first, TimeManager timeManager) {
        // initialize bestColumn with the first available column
        int bestColumn = board.getAvailableColumns()[0];

        try {
            // evaluate the tree with increasing depths
            System.err.println("\n---- New move ----");

            int gameTreeMaxDepth = (board.M * board.N) - board.getMarkedCells().length;
            int gameTreeDepth = 1;

            TranspositionTable transpositionTable = new TranspositionTable();
            preEvaluationPriorityQueue1 = new PriorityQueue<>();

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
        if (isFirstPlayerTurn) bestValue = Evaluator.WINP2VALUE;
        else bestValue = Evaluator.WINP1VALUE;
        bestColumn = availableColumns[0];

        // start evaluation
        int alpha = Evaluator.WINP2VALUE;
        int beta = Evaluator.WINP1VALUE;

        PriorityQueue<Pair> newPriorityQueue = new PriorityQueue<>(); // priority queue to be used for the next depth

        // evaluate in descending order the best evaluated columns in the previous depth
        while (!preEvaluationPriorityQueue1.isEmpty() && alpha < beta) {
            int currentColumn = preEvaluationPriorityQueue1.poll().second;
            board.markColumn(currentColumn);

            int currentChoiceValue = alphaBeta(board, !isFirstPlayerTurn, alpha, beta, depth - 1,
                    transpositionTable, timeManager);

            // minimax
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

            // add column value for the next depth
            newPriorityQueue.offer(new Pair(currentChoiceValue, currentColumn));
            columnsVisited.add(currentColumn);

            board.unmarkColumn();
        }

        // evaluate the remaining columns
        int columnIndex = 0;
        while (columnIndex < availableColumns.length && alpha < beta) {
            int currentColumn = availableColumns[columnIndex];

            if (!columnsVisited.contains(currentColumn)) {
                board.markColumn(currentColumn);

                int currentChoiceValue = alphaBeta(board, !isFirstPlayerTurn, alpha, beta, depth - 1,
                        transpositionTable, timeManager);

                // minimax
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

                // add column value for the next depth
                newPriorityQueue.offer(new Pair(currentChoiceValue, currentColumn));

                board.unmarkColumn();
            }

            columnIndex++;
        }

        preEvaluationPriorityQueue1 = newPriorityQueue;

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

        Integer bestValueInTransTable = transpositionTable.getValue(board, alpha, beta);

        if (bestValueInTransTable != null) bestValue = bestValueInTransTable; // check transposition table
        else {
            if (depth <= 0 || board.gameState() != OPEN) bestValue = Evaluator.evaluate(board, timeManager);
            else if (isFirstPlayerTurn) {
                // maximize the value
                Integer[] availableColumns = board.getAvailableColumns();

                // pre-evaluation
                PriorityQueue<Pair> preEvaluationPriorityQueue2 = new PriorityQueue<>();

                for(int availableColumn : availableColumns){
                    board.markColumn(availableColumn);
                    int eval = Evaluator.preEvaluate(board, timeManager);
                    board.unmarkColumn();

                    preEvaluationPriorityQueue2.offer(new Pair(eval, availableColumn));
                }

                // evaluation
                bestValue = Evaluator.WINP2VALUE;

                while (!preEvaluationPriorityQueue2.isEmpty() && alpha < beta) {
                    // mark column and check if the value of that choice is the best,
                    // if so change the values of bestChoice
                    int column = preEvaluationPriorityQueue2.poll().second;
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
                // minimize the value
                Integer[] availableColumns = board.getAvailableColumns();

                // pre-evaluation
                PriorityQueue<Pair> priorityQueue = new PriorityQueue<>();

                for(int availableColumn : availableColumns){
                    board.markColumn(availableColumn);
                    int eval = Evaluator.preEvaluate(board, timeManager);
                    board.unmarkColumn();

                    priorityQueue.offer(new Pair(eval, availableColumn));
                }

                // evaluation
                bestValue = Evaluator.WINP1VALUE;
                
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

}
