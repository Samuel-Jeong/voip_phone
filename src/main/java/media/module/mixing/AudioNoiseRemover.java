package media.module.mixing;

import media.module.mixing.base.FastFourierTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioNoiseRemover {

    private static final Logger logger = LoggerFactory.getLogger(AudioNoiseRemover.class);

    private final int samplingRate;

    ////////////////////////////////////////////////////////////////////////////////

    public AudioNoiseRemover(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private float[] convertIntegerToFloat(int[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        float[] convertedData = new float[data.length];
        float tempFloatDatum;

        for (int i = 0; i < data.length; i++) {
            tempFloatDatum = (float) data[i] / 0x8000;
            if (tempFloatDatum > 1.0f) {
                tempFloatDatum = 1.0f;
            } else if (tempFloatDatum < -1.0f) {
                tempFloatDatum = -1.0f;
            }
            convertedData[i] = tempFloatDatum;
        }

        return convertedData;
    }

    private int[] convertFloatToInteger(float[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        int[] convertedData = new int[data.length];

        for (int i = 0; i < data.length; i++) {
            convertedData[i] = Math.round(data[i] * 0x8000);
        }

        return convertedData;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public int[] removeNoise(int[] srcData) {
        if (srcData == null || srcData.length == 0) {
            return null;
        }

        // 1. Convert the time domain to the frequency domain
        float[] floatSrcData = convertIntegerToFloat(srcData);
        FastFourierTransform fastFourierTransform = new FastFourierTransform(floatSrcData.length, samplingRate);
        fastFourierTransform.forward(floatSrcData);

        // 2. Remove Noise in the frequency domain
        float[] spectrumArray = fastFourierTransform.getSpectrum();

        // 3. Convert the frequency domain to the time domain and apply the low pass filter
        spectrumArray = fastFourierTransform.inverse(spectrumArray);

        return convertFloatToInteger(spectrumArray);
    }

}
