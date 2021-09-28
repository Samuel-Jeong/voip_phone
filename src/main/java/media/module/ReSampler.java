package media.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @class public class ReSampler
 * @brief ReSampler class
 */
public class ReSampler {

    private static final Logger logger = LoggerFactory.getLogger(ReSampler.class);

    public ReSampler() {
        // Nothing
    }

    /**
     * Do resampling. Currently the amplitude is stored by short such that maximum bitsPerSample is 16 (bytePerSample is 2)
     * stereo only
     * @param sourceData	The source data in bytes
     * @param bitPerSample	How many bits represents one sample
     * @param sourceRate	Sample rate of the source data
     * @param targetRate	Sample rate of the target data
     * @param channelSize   Channel Size
     * @param gain Gain (0 이하면 적용 안함)
     * @return re-sampled data
     */
    public static byte[] reSample(byte[] sourceData, int bitPerSample, int sourceRate, int targetRate, int channelSize, double gain) {
        if (bitPerSample < 8) {
            return null;
        }

        // make the bytes to amplitudes first
        int bytePerSample = bitPerSample / 8;
        if (channelSize == 1) {
            bytePerSample /= 2;
        }

        int numSamples = sourceData.length / bytePerSample;
        //logger.debug("ReSampler: [{}]>[{}] [sourceData.length:{}] [numSamples:{}]", sourceRate, targetRate, sourceData.length, numSamples);
        short[] amplitudes = new short[numSamples]; // 16 bit, use a short to store

        int pointer = 0;
        for (int i = 0; i < numSamples; i++) {
            short amplitude = 0;
            for (int byteNumber = 0; byteNumber < bytePerSample; byteNumber++) {
                // little endian
                amplitude |= (sourceData[pointer++] & 0xFF) << (byteNumber * 8);
            }

            if (gain > 0) {
                amplitudes[i] = (short) (Math.round(amplitude * gain));
            } else {
                amplitudes[i] = amplitude;
            }
        }
        // end make the amplitudes

        // do interpolation
        short[] targetSample = interpolate(sourceRate, targetRate, amplitudes);
        int targetLength = targetSample.length;
        // end do interpolation
        //logger.debug("ReSampler: [{}]>[{}] [targetLength:{}]", sourceRate, targetRate, targetLength);

        // Remove the high frequency signals with a digital filter,
        // leaving a signal containing only half-sample-rated frequency information,
        // but still sampled at a rate of target sample rate. Usually FIR is used

        // convert the amplitude to bytes
        byte[] bytes;
        if (bytePerSample == 1) {
            bytes = new byte[targetLength];
            for (int i = 0; i < targetLength; i++) {
                bytes[i] = (byte) targetSample[i];
            }
        }
        else if (bytePerSample == 2) {
            bytes = new byte[targetLength * 2];
            for (int i = 0; i < targetSample.length; i++) {
                // little endian
                bytes[i * 2] = (byte) (targetSample[i] & 0xff);
                bytes[i * 2 + 1] = (byte) ((targetSample[i] >> 8) & 0xff);
            }
        } else {
            return null;
        }

        /*else if (bytePerSample == 3) {
            bytes = new byte[targetLength * 3];
            for (int i = 0; i < targetSample.length; i++) {
                // little endian
                bytes[i * 3] = (byte) (targetSample[i] & 0xff);
                bytes[i * 3 + 1] = (byte) ((targetSample[i] >> 8) & 0xff);
                bytes[i * 3 + 2] = (byte) ((targetSample[i] >> 16) & 0xff);
            }
        } else {
            bytes = new byte[targetLength * 4];
            for (int i = 0; i < targetSample.length; i++) {
                // little endian
                bytes[i * 4] = (byte) (targetSample[i] & 0xff);
                bytes[i * 4 + 1] = (byte) ((targetSample[i] >> 8) & 0xff);
                bytes[i * 4 + 2] = (byte) ((targetSample[i] >> 16) & 0xff);
                bytes[i * 4 + 3] = (byte) ((targetSample[i] >> 24) & 0xff);
            }
        }*/

        //logger.debug("ReSampler: [{}]>[{}] [bytes.length:{}]", sourceRate, targetRate, bytes.length);
        // end convert the amplitude to bytes

        return bytes;
    }

    private static short[] interpolate(int oldSampleRate, int newSampleRate, short[] samples) {
        if (oldSampleRate == newSampleRate) {
            return samples;
        }

        int newLength = Math.round(((float) samples.length / oldSampleRate * newSampleRate));
        float lengthMultiplier = (float) newLength / samples.length;
        short[] interpolatedSamples = new short[newLength];

        // interpolate the value by the linear equation y=mx+c
        for (int i = 0; i < newLength; i++) {
            // get the nearest positions for the interpolated point
            float currentPosition = i / lengthMultiplier;
            int nearestLeftPosition = (int) currentPosition;
            int nearestRightPosition = nearestLeftPosition + 1;
            if (nearestRightPosition >= samples.length) {
                nearestRightPosition = samples.length - 1;
            }

            float slope = samples[nearestRightPosition] - samples[nearestLeftPosition]; // delta x is 1
            float positionFromLeft = currentPosition - nearestLeftPosition;

            interpolatedSamples[i] = (short) Math.round(slope * positionFromLeft + samples[nearestLeftPosition]); // y=mx+c
        }

        return interpolatedSamples;
    }

}
