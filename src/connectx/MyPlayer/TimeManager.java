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
        long currentTimeInMillis = System.currentTimeMillis();
        long elapsedTimeInMillis = currentTimeInMillis - timestartInMillis;
        double elapsedTimeInSec = elapsedTimeInMillis / 1000.0;

        if (elapsedTimeInSec >= timeoutInSecs * (90.0 / 100.0))
            throw new TimeoutException();
    }
}
