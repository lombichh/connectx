package connectx.MyPlayer;

import connectx.CXGameState;

import java.util.ArrayList;

public class GameTreeNode {
    private int evaluation;
    private ArrayList<GameTreeNode> childNodes;

    public GameTreeNode(int evaluation, ArrayList<GameTreeNode> childNodes) {
        this.evaluation = evaluation;
        this.childNodes = childNodes;
    }

    public int getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(int evaluation) {
        this.evaluation = evaluation;
    }

    public ArrayList<GameTreeNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(ArrayList<GameTreeNode> childNodes) {
        this.childNodes = childNodes;
    }
}
