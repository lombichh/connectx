package connectx.MyPlayer;

import java.util.ArrayList;

public class GameTreeNode {
    private CXBoardCopy currentBoard;
    private ArrayList<GameTreeNode> childNodes;

    public GameTreeNode(CXBoardCopy currentBoard, ArrayList<GameTreeNode> childNodes) {
        this.currentBoard = currentBoard;
        this.childNodes = childNodes;
    }

    public CXBoardCopy getCurrentBoard() {
        return currentBoard;
    }

    public void setCurrentBoard(CXBoardCopy currentBoard) {
        this.currentBoard = currentBoard;
    }

    public ArrayList<GameTreeNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(ArrayList<GameTreeNode> childNodes) {
        this.childNodes = childNodes;
    }
}
