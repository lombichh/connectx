package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXPlayer;

import java.util.ArrayList;

import static connectx.MyPlayer.GameTreeUtils.createGameTree;

public class MyPlayer implements CXPlayer {
    private boolean first;
    private Integer[] availableColumns;

    /* Default empty constructor */
    public MyPlayer() {
    }

    @Override
    public void initPlayer(int M, int N, int X, boolean first, int timeout_in_secs) {
        this.first = first;
    }

    /* Returns the number of the best choice column */
    @Override
    public int selectColumn(CXBoard B) {
        this.availableColumns = B.getAvailableColumns();

        CXBoardCopy BCopy = new CXBoardCopy(B.M, B.N, B.X);
        BCopy.copyFromCXBoard(B);
        GameTreeNode gameTree = createGameTree(BCopy);

        // Initialize maxValue with the value of the first available column
        ArrayList<GameTreeNode> childNodes = gameTree.getChildNodes();
        int maxValue = miniMax(childNodes.get(0), false);
        int columnNumber = availableColumns[0];

        // Get the column number of the best choice by calling minimax on every available column
        for (int i = 1; i < childNodes.size(); i++) {
            int nodeValue = miniMax(childNodes.get(i), false);
            if (nodeValue > maxValue) {
                maxValue = nodeValue;
                columnNumber = availableColumns[i];
            }
        }

        return columnNumber;
    }

    /* Returns the miniMax value of the node */
    int miniMax(GameTreeNode node, boolean isMyPlayerTurn) {
        int nodeValue;

        if (GameTreeUtils.isLeaf(node)) nodeValue = evaluate(node);
        else if (isMyPlayerTurn) {
            ArrayList<GameTreeNode> childNodes = node.getChildNodes();

            nodeValue = miniMax(childNodes.get(0), false);
            for (int i = 1; i < childNodes.size(); i++) {
                nodeValue = Math.max(
                        nodeValue,
                        miniMax(childNodes.get(i), false)
                );
            }
        } else {
            ArrayList<GameTreeNode> childNodes = node.getChildNodes();

            nodeValue = miniMax(childNodes.get(0), true);
            for (int i = 1; i < childNodes.size(); i++) {
                nodeValue = Math.min(
                        nodeValue,
                        miniMax(childNodes.get(i), true)
                );
            }
        }

        return nodeValue;
    }

    /* Returns the value of the node based on his game state */
    int evaluate(GameTreeNode node) {
        switch (node.getGameState()) {
            case WINP1:
                if (first) return 1;
                else return -1;
            case WINP2:
                if (first) return -1;
                else return 1;
            default:
                return 0;
        }
    }

    public String playerName() {
        return "MyPlayer";
    }
}
