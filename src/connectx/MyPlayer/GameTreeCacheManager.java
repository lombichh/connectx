package connectx.MyPlayer;

import connectx.CXBoard;

import java.util.HashMap;

public class GameTreeCacheManager {
    private HashMap<String, Integer> gameTreeCache;

    public GameTreeCacheManager() {
        gameTreeCache = new HashMap<>();
    }

    public void resetCache() {
        gameTreeCache.clear();
    }

    public void insertNode(CXBoard board, Integer value) {
        String nodeKey = generateKey(board);
        gameTreeCache.put(nodeKey, value);
    }

    public Integer getNodeValue(CXBoard board) {
        String nodeKey = generateKey(board);
        return gameTreeCache.get(nodeKey);
    }

    private String generateKey(CXBoard board) {
        StringBuilder keyBuilder = new StringBuilder();

        for (int row = 0; row < board.M; row++) {
            for (int col = 0; col < board.N; col++) {
                keyBuilder.append(board.getBoard()[row][col].toString());
            }
        }

        return keyBuilder.toString();
    }
}
