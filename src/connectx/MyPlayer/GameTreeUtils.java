package connectx.MyPlayer;

import java.util.ArrayList;

public class GameTreeUtils {
    public static GameTreeNode createGameTree(CXBoardCopy B) {
        // create childNodes
        ArrayList<GameTreeNode> childNodes = new ArrayList<>();
        Integer[] availableColumns = B.getAvailableColumns();

        for (int i = 0; i < availableColumns.length; i++) {
            B.markColumn(availableColumns[i]);
            childNodes.add(createGameTree(B));
            B.unmarkColumn();
        }

        // create and return game tree
        return new GameTreeNode(B, childNodes);
    }

    public static int getGameTreeNodesNumber(GameTreeNode gameTreeNode) {
        int nodesNumber = 1;

        for (GameTreeNode childNode : gameTreeNode.getChildNodes())
            nodesNumber += getGameTreeNodesNumber(childNode);

        return nodesNumber;
    }

    public static boolean isLeaf(GameTreeNode node) {
        return node.getChildNodes().size() == 0;
    }
}
