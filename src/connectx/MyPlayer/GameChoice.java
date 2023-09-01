package connectx.MyPlayer;

public class GameChoice implements Comparable {
    private int value, column;

    public GameChoice(int value, int column) {
        this.value = value;
        this.column = column;
    }

    @Override
    public int compareTo(Object o) {
        return ((GameChoice) o).value - value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
