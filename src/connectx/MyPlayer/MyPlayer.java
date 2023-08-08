package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXPlayer;

import java.util.ArrayList;

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
        GameTreeNode gameTree = createGameTree(BCopy, 0);
        System.err.println("Game tree nodes number: " + GameTreeUtils.getGameTreeNodesNumber(gameTree));

        // Initialize maxValue with the value of the first available column
        ArrayList<GameTreeNode> childNodes = gameTree.getChildNodes();
        int maxValue = alphaBeta(childNodes.get(0), !first, Evaluator.WINP2VALUE, Evaluator.WINP1VALUE);
        int columnNumber = availableColumns[0];

        // Get the column number of the best choice by calling minimax on every available column
        for (int i = 1; i < childNodes.size(); i++) {
            int nodeValue = alphaBeta(childNodes.get(i), !first, Evaluator.WINP2VALUE, Evaluator.WINP1VALUE);
            if (nodeValue > maxValue) {
                maxValue = nodeValue;
                columnNumber = availableColumns[i];
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

            int childNumber = 1;
            while (childNumber < childNodes.size() && alpha < beta) {
                nodeValue = Math.max(
                        nodeValue,
                        alphaBeta(childNodes.get(childNumber), false, alpha, beta)
                );
                alpha = Math.max(nodeValue, alpha);
                childNumber++;
            }
        } else {
            ArrayList<GameTreeNode> childNodes = node.getChildNodes();

            nodeValue = alphaBeta(childNodes.get(0), true, alpha, beta);
            beta = Math.min(nodeValue, beta);

            int childNumber = 1;
            while (childNumber < childNodes.size() && beta > alpha) {
                nodeValue = Math.min(
                        nodeValue,
                        alphaBeta(childNodes.get(childNumber), true, alpha, beta)
                );
                beta = Math.min(nodeValue, beta);
                childNumber++;
            }
        }

        return nodeValue;
    }

    /* Returns the value of the node based on his game state */
    int evaluate(GameTreeNode node) {
        return node.getEvaluation();
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
