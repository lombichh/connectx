package connectx.MyPlayer;

import java.util.concurrent.TimeoutException;

public class TimeManager {
    private long timestartInMillis;
    private int timeoutInSecs;

    public TimeManager(int timeoutInSecs) {
        this.timestartInMillis = System.currentTimeMillis();
        this.timeoutInSecs = timeoutInSecs;
    }

    public void resetTime() {
        this.timestartInMillis = System.currentTimeMillis();
    }

    public void checkTime() throws TimeoutException {
        if ((System.currentTimeMillis() - timestartInMillis) / 1000.0
                >= timeoutInSecs * (99.0 / 100.0))
            throw new TimeoutException();
    }

    public double getElapsedTime() {
        return (System.currentTimeMillis() - timestartInMillis) / 1000.0;
    }
}
