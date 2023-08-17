package connectx.MyPlayer;

import java.util.HashMap;

public class GameTreeCacheManager {
    private HashMap<String, Integer> gameTreeCache;

    public GameTreeCacheManager() {
        gameTreeCache = new HashMap<>();
    }

    public void resetCache() {
        gameTreeCache.clear();
    }

    public boolean containsNode(GameTreeNode gameTreeNode) {
        return gameTreeCache.containsKey(generateKey(gameTreeNode));
    }

    public void insertNode(GameTreeNode gameTreeNode, Integer value) {
        String nodeKey = generateKey(gameTreeNode);
        gameTreeCache.put(nodeKey, value);
    }

    public Integer getNodeValue(GameTreeNode gameTreeNode) {
        String nodeKey = generateKey(gameTreeNode);
        return gameTreeCache.get(nodeKey);
    }

    private String generateKey(GameTreeNode gameTreeNode) {
        StringBuilder keyBuilder = new StringBuilder();

        for (int row = 0; row < gameTreeNode.getBoard().M; row++) {
            for (int col = 0; col < gameTreeNode.getBoard().N; col++) {
                keyBuilder.append(gameTreeNode.getBoard().getBoard()[row][col].toString());
            }
        }

        return keyBuilder.toString();
    }
}
