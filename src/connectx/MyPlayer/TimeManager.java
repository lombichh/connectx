package connectx.MyPlayer;

import java.util.concurrent.TimeoutException;

public class TimeManager {
    private long timestartInMillis;
    private int timeoutInSecs;

    public TimeManager(int timeoutInSecs) {
        timestartInMillis = System.currentTimeMillis();
        this.timeoutInSecs = timeoutInSecs;
    }

    public void checkTime() throws TimeoutException {
        if ((System.currentTimeMillis() - timestartInMillis) / 1000.0 >= timeoutInSecs * (99.0 / 100.0))
            throw new TimeoutException();
    }
}
