package connectx.MyPlayer;

import connectx.CXBoard;

import java.util.ArrayList;

import static connectx.CXGameState.*;

/**
 * Stores methods for creating and managing the game decision tree.
 */
public class GameTreeUtils {

    /**
     * Caller for createGameTree,
     * creates a copy of the board before calling the recursive method
     * in order not to work in the actual board.
     */
    public static GameTreeNode createGameTreeCaller(CXBoard board, int depth) {
        MyCXBoard boardCopy = new MyCXBoard(board.M, board.N, board.X);
        boardCopy.copyFromCXBoard(board);

        return GameTreeUtils.createGameTree(boardCopy, depth);
    }

    /**
     * Create a game tree with a certain depth
     * starting from a particular state of the board.
     * Returns the root node of the game tree.
     */
    public static GameTreeNode createGameTree(MyCXBoard board, int depth) {
        // create childNodes
        ArrayList<GameTreeNode> childNodes = new ArrayList<>();
        Integer[] availableColumns = board.getAvailableColumns();

        for (int i = 0; i < availableColumns.length; i++) {
            board.markColumn(availableColumns[i]);

            if (board.gameState() != OPEN || depth <= 0) {
                childNodes.add(new GameTreeNode(board.copy(), new ArrayList<>()));
            } else childNodes.add(createGameTree(board, depth - 1));

            board.unmarkColumn();
        }

        // create and return game tree
        return new GameTreeNode(board, childNodes);
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
     * Returns true if the given node is a leaf, false otherwise.
     */
    public static boolean isLeaf(GameTreeNode node) {
        return node.getChildNodes().size() == 0;
    }
}
