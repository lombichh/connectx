package connectx.MyPlayer;

/**
 * A game choice.
 * Stores the value of that choice and the index of the column picked.
 */
public class GameChoice {
    private int value;
    private int columnIndex;

    public GameChoice(int value, int columnIndex) {
        this.value = value;
        this.columnIndex = columnIndex;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

}
