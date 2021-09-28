package media.protocol.rtp.base;

import java.util.concurrent.TimeUnit;

/**
 * @class public class RtpClock
 * @brief RtpClock class
 */
public class RtpClock {

    //the difference between media time measured by local and remote clock
    protected long drift;

    //absolute time clock
    private final Clock wallClock;

    //the clock rate measured in Hertz.
    private int clockRate;

    private int scale;

    //the flag indicating the state of relation between local and remote clocks
    //the flag value is true if relation established
    private boolean isSynchronized;

    /**
     * Creates new instance of clock.
     *
     * @param wallClock time clock.
     */
    public RtpClock (Clock wallClock) {
        this.wallClock = wallClock;
    }

    public Clock getWallClock ( ) {
        return wallClock;
    }

    /**
     * Gets the clock rate.
     *
     * @return the value in Hertz
     */
    public int getClockRate ( ) {
        return clockRate;
    }

    /**
     * Modifies clock rate.
     *
     * @param clockRate the new value of clock rate in Hertz.
     */
    public void setClockRate (int clockRate) {
        this.clockRate = clockRate;
        this.scale = clockRate / 1000;
    }

    /**
     * Synchronizes this clock with remote clock
     *
     * @param remote the time on remote clock.
     */
    public void synchronize (long remote) {
        this.drift = remote - getLocalRtpTime();
        this.isSynchronized = true;
    }

    /**
     * The state of the relation between remote and local clock.
     *
     * @return true if time is same on both clocks.
     */
    public boolean isSynchronized ( ) {
        return this.isSynchronized;
    }

    /**
     * Resets clocks.
     */
    public void reset ( ) {
        this.drift = 0;
        this.clockRate = 0;
        this.isSynchronized = false;
    }

    /**
     * Time in RTP timestamps.
     */
    public long getLocalRtpTime () {
        return scale * wallClock.getTime(TimeUnit.MILLISECONDS) + drift;
    }

    /**
     * Returns the time in milliseconds
     *
     * @param timestamp the rtp timestamp
     * @return the time in milliseconds
     */
    public long convertToAbsoluteTime (long timestamp, int clockRate) {
        if (this.clockRate <= 0) {
            return timestamp * 1000 / clockRate;
        } else {
            return timestamp * 1000 / this.clockRate;
        }
    }

    /**
     * Calculates RTP timestamp
     *
     * @param time the time in milliseconds
     * @return rtp timestamp.
     */
    public long convertToRtpTime (long time) {
        return time * clockRate / 1000;
    }

}
