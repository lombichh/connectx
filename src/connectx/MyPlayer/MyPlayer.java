package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXPlayer;

import java.util.ArrayList;

import static connectx.MyPlayer.GameTreeUtils.createGameTree;

public class MyPlayer implements CXPlayer {
    private boolean first;
    private Integer[] availableColumns;

    int miniMaxCounter = 0;

    private static int WIN = 1;
    private static int LOSE = -1;
    private static int DRAW = 0;

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

        System.err.println("Minimax counter: " + miniMaxCounter);
        return columnNumber;
    }

    /* Returns the miniMax value of the node */
    int miniMax(GameTreeNode node, boolean isMyPlayerTurn) {
        miniMaxCounter++;

        int nodeValue;

        if (GameTreeUtils.isLeaf(node)) nodeValue = evaluate(node);
        else if (isMyPlayerTurn) {
            ArrayList<GameTreeNode> childNodes = node.getChildNodes();

            nodeValue = miniMax(childNodes.get(0), false);

            int childNumber = 1;
            while (childNumber < childNodes.size() && nodeValue < WIN) {
                nodeValue = Math.max(
                        nodeValue,
                        miniMax(childNodes.get(childNumber), false)
                );
                childNumber++;
            }
        } else {
            ArrayList<GameTreeNode> childNodes = node.getChildNodes();

            nodeValue = miniMax(childNodes.get(0), true);

            int childNumber = 1;
            while (childNumber < childNodes.size() && nodeValue > LOSE) {
                nodeValue = Math.min(
                        nodeValue,
                        miniMax(childNodes.get(childNumber), true)
                );
                childNumber++;
            }
        }

        return nodeValue;
    }

    /* Returns the value of the node based on his game state */
    int evaluate(GameTreeNode node) {
        switch (node.getGameState()) {
            case WINP1:
                if (first) return WIN;
                else return LOSE;
            case WINP2:
                if (first) return LOSE;
                else return WIN;
            default:
                return DRAW;
        }
    }

    public String playerName() {
        return "MyPlayer";
    }
}
