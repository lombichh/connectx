package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXPlayer;

import java.util.ArrayList;

import static connectx.CXGameState.*;
import static connectx.MyPlayer.GameTreeUtils.createGameTree;

public class MyPlayer implements CXPlayer {
    private boolean first;
    private Integer[] availableColumns;

    int alphaBetaCounter = 0;

    /* Default empty constructor */
    public MyPlayer() {}

    @Override
    public void initPlayer(int M, int N, int X, boolean first, int timeout_in_secs) {
        this.first = first;
    }

    /* Returns the number of the best choice column */
    @Override
    public int selectColumn(CXBoard B) {
        this.availableColumns = B.getAvailableColumns();

        MyCXBoard BCopy = new MyCXBoard(B.M, B.N, B.X);
        BCopy.copyFromCXBoard(B);
        GameTreeNode gameTree = createGameTree(BCopy, 1000);
        System.err.println("Game tree nodes number: " + GameTreeUtils.getGameTreeNodesNumber(gameTree));
        System.err.println("First: " + first);

        // Initialize maxValue with the value of the first available column
        ArrayList<GameTreeNode> childNodes = gameTree.getChildNodes();
        int colValue = alphaBeta(childNodes.get(0), !first, Evaluator.WINP2VALUE, Evaluator.WINP1VALUE);
        int columnNumber = availableColumns[0];

        // Get the column number of the best choice by calling minimax on every available column
        for (int i = 1; i < childNodes.size(); i++) {
            int nodeValue = alphaBeta(childNodes.get(i), !first, Evaluator.WINP2VALUE, Evaluator.WINP1VALUE);

            // If first player maximize, otherwise minimize
            if (first) {
                if (nodeValue > colValue) {
                    colValue = nodeValue;
                    columnNumber = availableColumns[i];
                }
            } else {
                if (nodeValue < colValue) {
                    colValue = nodeValue;
                    columnNumber = availableColumns[i];
                }
            }
        }

        System.err.println("Minimax counter: " + alphaBetaCounter);
        return columnNumber;
    }

    /* Returns the alphaBeta value of the node */
    int alphaBeta(GameTreeNode node, boolean isFirstPlayerTurn, int alpha, int beta) {
        alphaBetaCounter++;

        int nodeValue;

        if (GameTreeUtils.isLeaf(node)) {
            nodeValue = evaluate(node);
            System.err.println("Node value: " + nodeValue);
        } else if (isFirstPlayerTurn) {
            ArrayList<GameTreeNode> childNodes = node.getChildNodes();

            nodeValue = alphaBeta(childNodes.get(0), false, alpha, beta);
            alpha = Math.max(nodeValue, alpha);

            int childIndex = 1;
            while (childIndex < childNodes.size() && alpha < beta) {
                nodeValue = Math.max(
                        nodeValue,
                        alphaBeta(childNodes.get(childIndex), false, alpha, beta)
                );
                alpha = Math.max(nodeValue, alpha);
                childIndex++;
            }
        } else {
            ArrayList<GameTreeNode> childNodes = node.getChildNodes();

            nodeValue = alphaBeta(childNodes.get(0), true, alpha, beta);
            beta = Math.min(nodeValue, beta);

            int childIndex = 1;
            while (childIndex < childNodes.size() && beta > alpha) {
                nodeValue = Math.min(
                        nodeValue,
                        alphaBeta(childNodes.get(childIndex), true, alpha, beta)
                );
                beta = Math.min(nodeValue, beta);
                childIndex++;
            }
        }

        return nodeValue;
    }

    /* Returns the value of the node based on his game state */
    int evaluate(GameTreeNode node) {
        int nodeEvaluation;

        if (node.getBoard().gameState() == WINP1) nodeEvaluation = Evaluator.WINP1VALUE;
        else if (node.getBoard().gameState() == WINP2) nodeEvaluation = Evaluator.WINP2VALUE;
        else if (node.getBoard().gameState() == DRAW) nodeEvaluation = Evaluator.DRAWVALUE;
        else {
            System.err.println("Game is open");
            int[] playerValues = Evaluator.evaluateSequences(node.getBoard());
            System.err.println("Player 1 value: " + playerValues[0]);
            System.err.println("Player 2 value: " + playerValues[1]);
            nodeEvaluation = playerValues[0] - playerValues[1]; // P1Value - P2Value
        }

        return nodeEvaluation;

        /*switch (node.getGameState()) {
            case WINP1:
                if (first) return WIN;
                else return LOSE;
            case WINP2:
                if (first) return LOSE;
                else return WIN;
            default:
                return DRAW;
        }*/
    }

    public String playerName() {
        return "MyPlayer";
    }
}
