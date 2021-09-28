package media.protocol.rtp.base;

import java.util.concurrent.TimeUnit;

/**
 * @class public class Clock
 * @brief Clock class
 */
public class Clock {

    public Clock() {
        // Nothing
    }

    public long getTime(TimeUnit timeUnit) {
        return timeUnit.toMillis(System.currentTimeMillis());
    }

}
