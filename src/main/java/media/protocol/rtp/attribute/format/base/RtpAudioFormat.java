package media.protocol.rtp.attribute.format.base;

import media.protocol.rtp.attribute.base.EncodingName;
import media.protocol.rtp.attribute.base.Format;

/**
 * @class public class AudioFormat extends Format implements Cloneable
 * @brief AudioFormat class
 */
public class RtpAudioFormat extends Format implements Cloneable {

    //sampling frequency
    private int sampleRate;
    //bits per sample
    private int sampleSize = -1;
    //number of channels
    private int channels = 1;

    /**
     * Creates new audio format descriptor.
     *
     * @param name the encoding name.
     */
    public RtpAudioFormat(EncodingName name) {
        super(name);
    }

    /**
     * Creates new format descriptor
     *
     * @param name the encoding
     * @param sampleRate sample rate value in Hertz
     * @param sampleSize sample size in bits
     * @param channels number of channels
     */
    private RtpAudioFormat(EncodingName name, int sampleRate, int sampleSize, int channels) {
        super(name);
        this.sampleRate = sampleRate;
        this.sampleSize = sampleSize;
        this.channels = channels;
    }

    /**
     * Gets the sampling rate.
     *
     * @return the sampling rate value in hertz.
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Modifies sampling rate value.
     *
     * @param sampleRate the sampling rate in hertz.
     */
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * Gets the sample size.
     *
     * @return sample size in bits.
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * Modifies sample size.
     *
     * @param sampleSize sample size in bits.
     */
    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    /**
     * Gets the number of channels.
     *
     * The default value is 1.
     *
     * @return the number of channels
     */
    public int getChannels() {
        return channels;
    }

    /**
     * Modifies number of channels.
     *
     * @param channels the number of channels.
     */
    public void setChannels(int channels) {
        this.channels = channels;
    }

    @Override
    public RtpAudioFormat clone() {
        RtpAudioFormat f = new RtpAudioFormat(getName().clone(), sampleRate, sampleSize, channels);
        f.setOptions(this.getOptions());
        return f;
    }

    @Override
    public boolean matches(Format other) {
        if (!super.matches(other)) return false;

        RtpAudioFormat f = (RtpAudioFormat) other;

        if (f.sampleRate != this.sampleRate) return false;
        // XXX dirty patch for issue #7 - https://github.com/Mobicents/mediaserver/issues/7
//        if (f.sampleSize != this.sampleSize) return false;
        if (f.channels != this.channels) return false;

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AudioFormat[");
        builder.append(getName().toString());

        builder.append(",");
        builder.append(sampleRate);

        if (sampleSize > 0) {
            builder.append(",");
            builder.append(sampleSize);
        }

        if (channels == 1) {
            builder.append(",mono");
        } else if (channels == 2) {
            builder.append(",stereo");
        } else {
            builder.append(",");
            builder.append(channels);
        }

        builder.append("]");
        return builder.toString();
    }

}
