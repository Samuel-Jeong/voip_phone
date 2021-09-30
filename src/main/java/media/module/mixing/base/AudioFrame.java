package media.module.mixing.base;

/**
 * @class public class AudioFrame
 * @brief AudioFrame class
 */
public class AudioFrame {

    /* Sample data */
    private byte[] data;
    /* Decibel Data */
    private int[] convertedData = null;
    /* Time Stamp */
    private volatile long timestamp;
    /* Gain (volume) */
    private short gain;
    /* Sampling rate */
    private int samplingRate;
    /* Sample size */
    private int sampleSize;
    /* Channel size */
    private int channelSize;
    private boolean isDtmf;

    ////////////////////////////////////////////////////////////////////////////////

    public AudioFrame(boolean isDtmf) {
        this.isDtmf = isDtmf;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public byte[] getData(boolean isRaw) {
        if (isRaw) {
            return data;
        } else {
            if (convertedData == null) {
                return null;
            }

            int outputIndex = 0;
            byte[] dataArray = new byte[convertedData.length * 2];
            for (int outputCount = 0; outputCount < convertedData.length;) {
                dataArray[outputIndex++] = (byte) (convertedData[outputCount]);
                dataArray[outputIndex++] = (byte) (convertedData[outputCount++] >> 8);
            }

            return dataArray;
        }
    }

    public int[] getConvertedData() {
        return convertedData;
    }

    public void setConvertedData(int[] convertedData) {
        this.convertedData = convertedData;
    }

    public void setData(byte[] data, boolean isRaw) {
        int dataLen = data.length;
        /*if (dataLen % 2 != 0) {
            dataLen += 1;
        }*/

        byte[] curData = new byte[dataLen];
        System.arraycopy(data, 0, curData, 0, data.length);
        this.data = curData;

        if (!isRaw) {
            this.convertedData = new int[dataLen / 2];
            int inputIndex = 0;
            for (int inputCount = 0; inputCount < dataLen; inputCount += 2) {
                this.convertedData[inputIndex++] = (short) ((this.data[inputCount] & 0xff) | ((this.data[inputCount + 1]) << 8));
            }
        }
    }

    public double getDecibelFromSample(int sample) {
        return 20 * Math.log10(
                ((double) Math.abs(sample)) / 32768
        );
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public short getGain() {
        return gain;
    }

    public void setGain(short gain) {
        this.gain = gain;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public int getChannelSize() {
        return channelSize;
    }

    public void setChannelSize(int channelSize) {
        this.channelSize = channelSize;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    public boolean isDtmf() {
        return isDtmf;
    }

    public void setDtmf(boolean dtmf) {
        isDtmf = dtmf;
    }
}
