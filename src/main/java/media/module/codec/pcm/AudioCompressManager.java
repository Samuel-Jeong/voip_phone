package media.module.codec.pcm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @class public class AudioCompressManager
 * @brief AudioCompressManager class
 */
public class AudioCompressManager {

    private static final Logger logger = LoggerFactory.getLogger(AudioCompressManager.class);

    /* Left shift for segment number. */
    private static final int SEG_SHIFT = 4;

    /* Segment field mask. */
    private static final int SEG_MASK = 0x70;

    /* Sign bit for a A-law byte. */
    private static final int SIGN_BIT = 0x80;

    /* Quantization field mask */
    private static final int QUANT_MASK = 0xf;

    /* Divide into 8 uneven segments, count negative numbers, and a total of 16 segments */
    private static final int[] segAEnd = {0xFF, 0x1FF, 0x3FF, 0x7FF, 0xFFF, 0x1FFF, 0x3FFF, 0x7FFF};
    //private static final short[] segAEnd = {0x1F, 0x3F, 0x7F, 0xFF, 0x1FF, 0x3FF, 0x7FF, 0xFFF};
    //private static final short[] segUEnd = {0x3F, 0x7F, 0xFF, 0x1FF, 0x3FF, 0x7FF, 0xFFF, 0x1FFF};

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public static int pcmToALaw (int pcmValue)
     * @brief PCM to ALAW Conversion
     * @param pcmValue PCM value
     * @return ALAW value
     */
    public static int pcmToALaw(int pcmValue) {
        int mask;
        int segment;
        int aLawValue;

        if (pcmValue >= 0) {
            mask = 0xD5; /* sign 7th bit = 1 */
        } else {
            mask = 0x55; /* sign bit = 0 */
            //pcmValue = -pcmValue - 1;
            pcmValue = -pcmValue - 8;
        }

        /* Convert the scaled magnitude to segment number. */
        segment = search(pcmValue);

        /* Combine the sign, segment, and quantization bits. */
        if (segment >= 8) {
            /* out of range, return maximum value. */
            return (0x7F ^ mask);
        } else {
            /* aLawValue is the offset of each segment, the data after segment quantization needs to add the offset (aLawValue) */
            aLawValue = segment << SEG_SHIFT;

            if (segment < 2) {
                /* The slopes of the 0 and 1 segments are the same */
                aLawValue |= (pcmValue >> 4) & QUANT_MASK;
            } else {
                /* Exclusive OR (XOR) 0x55, the purpose is to try to avoid continuous 0 or continuous 1 to improve the reliability of the transmission process */
                aLawValue |= (pcmValue >> (segment + 3)) & QUANT_MASK;
            }

            return aLawValue ^ mask;
        }
    }

    /**
     * @fn public static int aLawToPcm (int aLawValue)
     * @brief ALAW to PCM Conversion
     * @param aLawValue ALAW value
     * @return PCM value
     */
    public static int aLawToPcm(int aLawValue) {
        int targetValue;
        int segment;

        aLawValue ^= 0x55;

        targetValue = (aLawValue & QUANT_MASK) << 4;
        segment = (aLawValue & SEG_MASK) >> SEG_SHIFT;

        switch (segment) {
            case 0:
                targetValue += 8;
                break;
            case 1:
                targetValue += 0x108;
                break;
            default:
                targetValue += 0x108;
                targetValue <<= segment - 1;
        }

        return (((aLawValue & SIGN_BIT) != 0) ? targetValue : -targetValue);
    }

    ////////////////////////////////////////////////////////////////////////////////

    static int search(int val) {
        for (int i = 0; i < AudioCompressManager.segAEnd.length; i++) {
            if (val <= AudioCompressManager.segAEnd[i]) {
                return i;
            }
        }
        return AudioCompressManager.segAEnd.length;
    }


    ////////////////////////////////////////////////////////////////////////////////

    public static int[] convertPcmToALaw (long timestamp, int[] pcmData) {
        if (pcmData.length == 0) { return null; }

        int[] aLawData = new int[pcmData.length];

        for (int pcmIndex = 0; pcmIndex < pcmData.length; pcmIndex++) {
            //logger.debug("[COMPRESS {}] prev: {}", timestamp, pcmData[pcmIndex]);

            aLawData[pcmIndex] = pcmToALaw(pcmData[pcmIndex]);

            //logger.debug("[COMPRESS {}] after: {}", timestamp, aLawData[pcmIndex]);
        }

        return aLawData;
    }

    public static int[] convertALawToPcm (long timestamp, int[] aLawData) {
        if (aLawData.length == 0) { return null; }

        int[] pcmData = new int[aLawData.length];

        for (int aLawIndex = 0; aLawIndex < aLawData.length; aLawIndex++) {
            //logger.debug("[DECOMPRESS {}] prev: {}", timestamp, aLawData[aLawIndex]);

            pcmData[aLawIndex] = aLawToPcm(aLawData[aLawIndex]);

            //logger.debug("[DECOMPRESS {}] after: {}", timestamp, pcmData[aLawIndex]);
        }

        return pcmData;
    }

}
