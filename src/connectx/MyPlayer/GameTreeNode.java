package connectx.MyPlayer;

import connectx.CXBoard;

import java.util.ArrayList;

/**
 * A node of the game tree
 */
public class GameTreeNode {
    private CXBoard board;
    private ArrayList<GameTreeNode> childNodes;

    public GameTreeNode(CXBoard board, ArrayList<GameTreeNode> childNodes) {
        this.board = board;
        this.childNodes = childNodes;
    }

    public CXBoard getBoard() {
        return board;
    }

    public void setBoard(CXBoard board) {
        this.board = board;
    }

    public ArrayList<GameTreeNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(ArrayList<GameTreeNode> childNodes) {
        this.childNodes = childNodes;
    }
}
