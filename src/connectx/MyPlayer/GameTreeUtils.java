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
                int nodeEvalutation;

                if (B.gameState() == WINP1) nodeEvalutation = Evaluator.WINP1VALUE;
                else if (B.gameState() == WINP2) nodeEvalutation = Evaluator.WINP2VALUE;
                else if (B.gameState() == DRAW) nodeEvalutation = Evaluator.DRAWVALUE;
                else {
                    int[] playerValues = Evaluator.evaluateSequences(B);
                    System.err.println("Game is open");
                    System.err.println("Player 1 value: " + playerValues[0]);
                    System.err.println("Player 2 value: " + playerValues[1]);
                    nodeEvalutation = playerValues[0] - playerValues[1]; // P1Value - P2Value
                }

                childNodes.add(new GameTreeNode(
                        nodeEvalutation,
                        new ArrayList<>()
                ));
            } else childNodes.add(createGameTree(B, depth - 1));

            B.unmarkColumn();
        }

        // create and return game tree
        int[] playerValues = Evaluator.evaluateSequences(B);
        return new GameTreeNode(playerValues[0] - playerValues[1], childNodes);
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
