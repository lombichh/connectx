package connectx.MyPlayer;

import java.util.ArrayList;

/**
 * A node of the game tree
 */
public class GameTreeNode {
    private MyCXBoard board;
    private ArrayList<GameTreeNode> childNodes;

    public GameTreeNode(MyCXBoard board, ArrayList<GameTreeNode> childNodes) {
        this.board = board;
        this.childNodes = childNodes;
    }

    public MyCXBoard getBoard() {
        return board;
    }

    public void setBoard(MyCXBoard board) {
        this.board = board;
    }

    public ArrayList<GameTreeNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(ArrayList<GameTreeNode> childNodes) {
        this.childNodes = childNodes;
    }
}
