package connectx.MyPlayer;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import connectx.CXBoard;
import static connectx.CXGameState.*;

/**
 * Stores methods for creating and managing the game decision tree.
 */
public class GameTreeUtils {

    /**
     * Create a game tree with a certain depth
     * starting from a particular state of the board.
     * Returns the root node of the game tree.
     */
    public static GameTreeNode createGameTree(CXBoard board, int depth,
                                              GameTreeCacheManager gameTreeCacheManager,
                                              TimeManager timeManager) throws TimeoutException {
        timeManager.checkTime(); // Check the time left at every recursive call

        GameTreeNode gameTreeNode = new GameTreeNode(board, null); // Create the root node

        // Create child nodes
        ArrayList<GameTreeNode> childNodes = new ArrayList<>();
        if (!gameTreeCacheManager.containsNode(gameTreeNode)) { // Check cache
            gameTreeCacheManager.insertNode(gameTreeNode, null);

            // Add childNodes
            if (depth > 1) {
                Integer[] availableColumns = board.getAvailableColumns();

                for (int i = 0; i < availableColumns.length; i++) {
                    board.markColumn(availableColumns[i]);

                    GameTreeNode childNode;
                    if (board.gameState() != OPEN) {
                        // Game is closed -> create leaf node
                        childNode = new GameTreeNode(board.copy(), new ArrayList<>());
                        gameTreeCacheManager.insertNode(childNode, null); // Parent not in cache -> child not in cache
                    } else {
                        // Game is not closed -> create node through recursive call
                        childNode = createGameTree(board.copy(), depth - 1, gameTreeCacheManager, timeManager);
                    }
                    childNodes.add(childNode);

                    board.unmarkColumn();
                }
            }
        }

        gameTreeNode.setChildNodes(childNodes);
        return gameTreeNode;
    }

    /**
     * Add a level of leaves to the game tree.
     */
    public static void incrementGameTreeDepth(GameTreeNode gameTreeNode,
                                              GameTreeCacheManager gameTreeCacheManager,
                                              TimeManager timeManager) throws TimeoutException {
        timeManager.checkTime(); // check the time left at every recursive call

        if (isLeaf(gameTreeNode)) {
            // Add new childNodes to current node
            if (gameTreeNode.getBoard().gameState() == OPEN) {
                // Create childNodes
                ArrayList<GameTreeNode> childNodes = new ArrayList<>();

                CXBoard board = gameTreeNode.getBoard();
                Integer[] availableColumns = board.getAvailableColumns();

                for (int i = 0; i < availableColumns.length; i++) {
                    board.markColumn(availableColumns[i]);

                    GameTreeNode childNode = new GameTreeNode(board.copy(), new ArrayList<>());
                    if (!gameTreeCacheManager.containsNode(childNode)) { // Check cache
                        childNodes.add(childNode);
                        gameTreeCacheManager.insertNode(childNode, null);
                    }

                    board.unmarkColumn();
                }

                // Add childNodes to current node
                gameTreeNode.setChildNodes(childNodes);
            }
        } else {
            // Call function on childNodes
            for (GameTreeNode childNode : gameTreeNode.getChildNodes()) {
                incrementGameTreeDepth(childNode, gameTreeCacheManager, timeManager);
            }
        }
    }

    /**
     * Returns the max depth of the game tree given the current board
     */
    public static int getGameTreeMaxDepth(CXBoard board) {
        return (board.M * board.N) - board.getMarkedCells().length + 1;
    }

    /**
     * Returns the number of nodes in a game tree.
     */
    public static int getGameTreeNodesNumber(GameTreeNode gameTreeNode) {
        int nodesNumber = 1;

        for (GameTreeNode childNode : gameTreeNode.getChildNodes())
            nodesNumber += getGameTreeNodesNumber(childNode);

        return nodesNumber;
    }

    /**
     * Returns the depth of the game tree.
     */
    public static int getGameTreeDepth(GameTreeNode gameTreeNode) {
        int maxChildNodesDepth = 0;

        for (GameTreeNode childNode : gameTreeNode.getChildNodes()) {
            int childNodeDepth = getGameTreeDepth(childNode);
            if (childNodeDepth > maxChildNodesDepth) maxChildNodesDepth = childNodeDepth;
        }

        return maxChildNodesDepth + 1;
    }

    /**
     * Returns true if the given node is a leaf, false otherwise.
     */
    public static boolean isLeaf(GameTreeNode node) {
        return node.getChildNodes().size() == 0;
    }
}
