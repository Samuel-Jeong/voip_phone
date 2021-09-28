package media.protocol.rtp.attribute.format;

import media.MediaManager;

import javax.sound.sampled.AudioFormat;

/**
 * @class public class AudioProfile
 * @brief AudioProfile class
 */
public class AudioProfile {

    private static final String PCMA = AudioFormat.Encoding.ALAW.toString();
    private static final String PCMU = AudioFormat.Encoding.ULAW.toString();

    public static String getCodecNameFromID(int id) {
        switch (id) {
            case 8:
                return PCMA;
            case 0:
                return PCMU;
            case 110:
                return MediaManager.EVS;
            case 111:
                return MediaManager.AMR_NB;
            case 112:
                return MediaManager.AMR_WB;
            default:
                return null;
        }
    }

}
