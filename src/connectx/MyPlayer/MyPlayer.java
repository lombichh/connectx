package connectx.MyPlayer;

import connectx.CXBoard;
import connectx.CXPlayer;

import static connectx.MyPlayer.GameTreeUtils.createGameTree;
import static connectx.MyPlayer.GameTreeUtils.getGameTreeNodesNumber;

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

    /* Selects a random column */
    @Override
    public int selectColumn(CXBoard B) {
        this.availableColumns = B.getAvailableColumns();

        CXBoardCopy BCopy = new CXBoardCopy(B.M, B.N, B.X);
        BCopy.copyFromCXBoard(B);
        GameTreeNode gameTree = createGameTree(BCopy);
        System.err.println("tree nodes number: " + getGameTreeNodesNumber(gameTree));

        // get column number checking max miniMax of the childNodes

        /*ArrayList<GameTreeNode> childNodes = gameTree.getChildNodes();
        int maxValue = miniMax(childNodes.get(0));
        int columnNumber = availableColumns[0];

        for (int i = 1; i < childNodes.size(); i++) {
            int nodeValue = miniMax(childNodes.get(i), false);
            if (nodeValue > maxValue) {
                maxValue = nodeValue;
                columnNumber = availableColumns[i];
            }
        }*/

        return 0;
    }

    /*int miniMax(GameTreeNode node, boolean isMyPlayerTurn) {
        int nodeValue;

        if (isLeaf(node)) nodeValue = evaluate(node);
        else if (isMyPlayerTurn) {
            ArrayList<GameTreeNode> childNodes = node.getChildNodes();

            nodeValue = miniMax(childNodes.get(0), false);
            for (int i = 1; i < childNodes.size(); i++) {
                nodeValue = max(
                        nodeValue,
                        miniMax(childNodes.get(i), false)
                );
            }
        } else {
            ArrayList<GameTreeNode> childNodes = node.getChildNodes();

            nodeValue = miniMax(childNodes.get(0), true);
            for (int i = 1; i < childNodes.size(); i++) {
                nodeValue = min(
                        nodeValue,
                        miniMax(childNodes.get(i), true)
                );
            }
        }

        return nodeValue;
    }

    int evaluate(GameTreeNode node) {
        switch (node.getCurrentBoard().gameState()) {
            case WINP1:
                if (first) return 1;
                else return -1;
            case WINP2:
                if (first) return -1;
                else return 1;
            default:
                return 0;
        }
    }*/

    public String playerName() {
        return "MyPlayer";
    }
}
