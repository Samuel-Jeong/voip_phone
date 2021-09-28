package media.protocol.rtp.attribute.format;

import media.protocol.rtp.attribute.base.EncodingName;
import media.protocol.rtp.attribute.format.base.RtpAudioFormat;


/**
 * @class public class FormatFactory
 * @brief FormatFactory classa
 */
public class FormatFactory {

    /**
     * Creates new audio format descriptor.
     *
     * @param name the encoding name.
     */
    public static RtpAudioFormat createAudioFormat(EncodingName name) {
        //default format
        return new RtpAudioFormat(name);
    }

    /**
     * Creates new format descriptor
     *
     * @param name the encoding
     * @param sampleRate sample rate value in Hertz
     * @param sampleSize sample size in bits
     * @param channels number of channels
     */
    public static RtpAudioFormat createAudioFormat(String name, int sampleRate, int sampleSize, int channels) {
        RtpAudioFormat fmt = createAudioFormat(new EncodingName(name));
        fmt.setSampleRate(sampleRate);
        fmt.setSampleSize(sampleSize);
        fmt.setChannels(channels);
        return fmt;
    }

}
