package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXPlayer;

import java.util.ArrayList;

public class MyPlayer implements CXPlayer {
    private boolean first;
    private Integer[] availableColumns;

    int alphaBetaCounter = 0;

    public MyPlayer() {}

    @Override
    public void initPlayer(int M, int N, int X, boolean first, int timeout_in_secs) {
        this.first = first;
    }

    /**
     * Returns the number of the column to be selected
     */
    @Override
    public int selectColumn(CXBoard B) {
        this.availableColumns = B.getAvailableColumns();

        MyCXBoard BCopy = new MyCXBoard(B.M, B.N, B.X);
        BCopy.copyFromCXBoard(B);
        GameTreeNode gameTree = GameTreeUtils.createGameTree(BCopy, 6);

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

    public String playerName() {
        return "MyPlayer";
    }


    /**
     * Returns the alphaBeta value of the given node
     */
    int alphaBeta(GameTreeNode node, boolean isFirstPlayerTurn, int alpha, int beta) {
        alphaBetaCounter++;

        int nodeValue;

        if (GameTreeUtils.isLeaf(node)) nodeValue = Evaluator.evaluate(node);
        else if (isFirstPlayerTurn) {
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
}
