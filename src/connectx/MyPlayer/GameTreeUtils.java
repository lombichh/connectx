package connectx.MyPlayer;

import java.util.ArrayList;

import static connectx.CXGameState.*;

public class GameTreeUtils {
    public static GameTreeNode createGameTree(MyCXBoard B, int depth) {
        // create childNodes
        ArrayList<GameTreeNode> childNodes = new ArrayList<>();
        Integer[] availableColumns = B.getAvailableColumns();

        for (int i = 0; i < availableColumns.length; i++) {
            B.markColumn(availableColumns[i]);

            if (B.gameState() != OPEN || depth <= 0) {
                childNodes.add(new GameTreeNode(B.copy(), new ArrayList<>()));
            } else childNodes.add(createGameTree(B, depth - 1));

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
