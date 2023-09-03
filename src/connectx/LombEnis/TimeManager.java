package connectx.LombEnis;

import java.util.concurrent.TimeoutException;

/**
 * A manager of the game time.
 * It allows to check the remaining time
 * and throws an exception of the time is running out.
 */
public class TimeManager {
    private long timestartInMillis;
    private final int timeoutInSecs;

    public TimeManager(int timeoutInSecs) {
        this.timestartInMillis = System.currentTimeMillis();
        this.timeoutInSecs = timeoutInSecs;
    }

    public void resetTime() {
        this.timestartInMillis = System.currentTimeMillis();
    }

    public void checkTime() throws TimeoutException {
        if ((System.currentTimeMillis() - timestartInMillis) / 1000.0
                >= timeoutInSecs * (95.0 / 100.0))
            throw new TimeoutException();
    }

    public double getElapsedTime() {
        return (System.currentTimeMillis() - timestartInMillis) / 1000.0;
    }
}
