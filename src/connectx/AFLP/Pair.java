package connectx.AFLP;

public class Pair implements Comparable {
    public int first, second;

    public Pair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int compareTo(Object o) {
        return second - ((Pair) o).second;
    }
}
