package connectx.MyPlayer;

import connectx.CXGameState;

import java.util.ArrayList;

public class GameTreeNode {
    private CXGameState gameState;
    private ArrayList<GameTreeNode> childNodes;

    public GameTreeNode(CXGameState gameState, ArrayList<GameTreeNode> childNodes) {
        this.gameState = gameState;
        this.childNodes = childNodes;
    }

    public CXGameState getGameState() {
        return gameState;
    }

    public void setGameState(CXGameState gameState) {
        this.gameState = gameState;
    }

    public ArrayList<GameTreeNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(ArrayList<GameTreeNode> childNodes) {
        this.childNodes = childNodes;
    }
}
