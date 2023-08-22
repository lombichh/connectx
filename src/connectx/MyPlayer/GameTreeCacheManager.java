package connectx.MyPlayer;

import connectx.CXBoard;

import java.util.Hashtable;

public class GameTreeCacheManager {
    private Hashtable<String, Integer> gameTreeCache;

    public GameTreeCacheManager() {
        gameTreeCache = new Hashtable<>();
    }

    public void resetCache() {
        gameTreeCache.clear();
    }

    public void insertNode(CXBoard board, Integer value) {
        gameTreeCache.put(generateKey(board), value);
    }

    public Integer getNodeValue(CXBoard board) {
        return gameTreeCache.get(generateKey(board));
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
