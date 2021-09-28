package media.protocol.rtp.base;

import media.protocol.rtp.attribute.FormatParameterAttribute;
import media.protocol.rtp.attribute.base.Format;

/**
 * @class public class RTPFormat implements Cloneable
 * @brief RTPFormat class
 */
public class RtpFormat implements Cloneable {

    //payload id
    private int id;

    //format descriptor
    private Format format;

    // Gain
    private double gain;

    //RTP clock rate measured in Hertz.
    private int clockRate;

    private FormatParameterAttribute parameters;

    private String ip;
    private int port;

    //////////////////////////////////////////////////////////////////////

    /**
     * Creates new format descriptor.
     *
     * @param id     the payload number
     * @param format format descriptor
     */
    public RtpFormat(int id, Format format) {
        this.id = id;
        this.format = format;
    }

    /**
     * Creates new descriptor.
     *
     * @param id        payload number
     * @param format    formats descriptor
     * @param clockRate RTP clock rate
     */
    public RtpFormat(int id, Format format, double gain, int clockRate, String ip, int port) {
        this.id = id;
        this.format = format;
        this.gain = gain;
        this.clockRate = clockRate;
        this.ip = ip;
        this.port = port;
    }

    //////////////////////////////////////////////////////////////////////

    /**
     * Gets the payload number
     *
     * @return payload number
     */
    public int getID ( ) {
        return id;
    }

    /**
     * Modifies payload number.
     *
     * @param id the new payload number.
     */
    protected void setID (int id) {
        this.id = id;
    }

    /**
     * Gets the rtp clock rate.
     *
     * @return the rtp clock rate in Hertz
     */
    public int getClockRate ( ) {
        return clockRate;
    }

    /**
     * Modify rtp clock rate.
     *
     * @param clockRate the new value in Hertz.
     */
    public void setClockRate (int clockRate) {
        this.clockRate = clockRate;
    }

    /**
     * Gets format.
     *
     * @return format descriptor.
     */
    public Format getFormat ( ) {
        return format;
    }

    /**
     * Modifies format.
     *
     * @param format the new format descriptor.
     */
    public void setFormat (Format format) {
        this.format = format;
    }

    public double getGain() {
        return gain;
    }

    public void setGain(double gain) {
        this.gain = gain;
    }

    public FormatParameterAttribute getParameters ( ) {
        return parameters;
    }

    public void setParameters (FormatParameterAttribute parameters) {
        this.parameters = parameters;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    //////////////////////////////////////////////////////////////////////

    @Override
    public RtpFormat clone ( ) {
        Format f = format.clone();
        return new RtpFormat(id, f, gain, clockRate, ip, port);
    }

    @Override
    public String toString ( ) {
        return id + " " + format;
    }

}
