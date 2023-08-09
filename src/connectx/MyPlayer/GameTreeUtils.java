package connectx.MyPlayer;

import java.util.ArrayList;

import static connectx.CXGameState.*;

/**
 * Stores methods for creating and managing the game decision tree
 */
public class GameTreeUtils {
    /**
     * Create a game tree with a certain depth
     * starting from a particular state of the board
     */
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

    /**
     * Returns the number of nodes in a game tree
     */
    public static int getGameTreeNodesNumber(GameTreeNode gameTreeNode) {
        int nodesNumber = 1;

        for (GameTreeNode childNode : gameTreeNode.getChildNodes())
            nodesNumber += getGameTreeNodesNumber(childNode);

        return nodesNumber;
    }

    /**
     * Returns true if the given node is a leaf, false otherwise
     */
    public static boolean isLeaf(GameTreeNode node) {
        return node.getChildNodes().size() == 0;
    }
}
