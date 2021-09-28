package media.protocol.rtp.util;

import client.VoipClient;
import media.MediaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * @class public class RtpUtil
 * @brief RtpUtil class
 */
public class RtpUtil {

    private static final Logger logger = LoggerFactory.getLogger(RtpUtil.class);

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public static byte[] changeByteOrder(byte[] value)
     * @brief Byte order 를 변환하는 함수
     * @param data Sampled data
     * @return Byte order 가 변환된 Data 반환
     */
    public static byte[] changeByteOrder(byte[] data) {
        int dataLength = data.length;
        byte[] convertedData = new byte[dataLength];

        for (int i = 0; i < dataLength; i++) {
            convertedData[i] = data[dataLength - (i + 1)];
        }

        return convertedData;
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public static byte[] upSample(byte[] data)
     * @brief Sampling rate 를 두 배 올리는 함수
     * @param data Sampled data
     * @return Up-sampled data 반환
     */
    public static byte[] upSamplingRateDouble(byte[] data) {
        byte[] resampledData = new byte[data.length * 2];

        for (int i = 0; i < data.length; i += 2) {
            resampledData[i * 2] = data[i];
            resampledData[i * 2 + 1] = data[i + 1];
            resampledData[i * 2 + 2] = data[i];
            resampledData[i * 2 + 3] = data[i + 1];
        }

        return resampledData;
    }

    /**
     * @fn public static byte[] downSample(byte[] data)
     * @brief Sampling rate 를 두 배 낮추는 함수
     * @param data Sampled data
     * @return Down-sampled data 반환
     */
    public static byte[] downSamplingRateDouble(byte[] data) {
        byte[] resampledData = new byte[data.length / 2];

        for (int i = 0; i < resampledData.length; i++) {
            resampledData[i] = i % 2 == 0 ? data[i * 2] : data[i * 2 + 1];
        }

        return resampledData;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public static byte[] downSampleSize(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat = new AudioFormat (
                voipClient.getTargetAudioFormat().getEncoding(),
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize(),
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize(),
                voipClient.getTargetFrameRate(),
                false
        );

        AudioFormat targetFormat = new AudioFormat (
                voipClient.getTargetAudioFormat().getEncoding(),
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize() / 2,
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize() / 2,
                voipClient.getTargetFrameRate(),
                false
        );

        return convertFormat(sourceFormat, targetFormat, data);
    }

    public static byte[] upSampleSize(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat = new AudioFormat (
                voipClient.getSourceAudioFormat().getEncoding(),
                Float.parseFloat(voipClient.getSourceSamplingRate()),
                voipClient.getSourceSampleSize() / 2,
                voipClient.getSourceChannelSize(),
                voipClient.getSourceFrameSize() / 2,
                voipClient.getSourceFrameRate(),
                false
        );

        AudioFormat targetFormat = new AudioFormat (
                voipClient.getSourceAudioFormat().getEncoding(),
                Float.parseFloat(voipClient.getSourceSamplingRate()),
                voipClient.getSourceSampleSize(),
                voipClient.getSourceChannelSize(),
                voipClient.getSourceFrameSize(),
                voipClient.getSourceFrameRate(),
                false
        );

        return convertFormat(sourceFormat, targetFormat, data);
    }

    /**
     * @fn public static byte[] encodeTargetCodec(byte[] data)
     * @param data Rtp Payload Data
     * @return 성공 시 인코딩된 미디어 데이터, 실패 시 인코딩 전 미디어 데이터 또는 null 반환
     */
    public static byte[] encodeTargetCodec(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat = new AudioFormat (
                voipClient.getTargetAudioFormat().getEncoding(),
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize(),
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize(),
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        AudioFormat.Encoding[] targetEncodings = AudioSystem.getTargetEncodings(sourceFormat);
        List<AudioFormat.Encoding> targetEncodingList = Arrays.asList(targetEncodings);

        // ALAW 로 인코딩 > 조건에 불일치하면 데이터 그대로 반환
        if (voipClient.getTargetAudioFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
            if (AudioFormat.Encoding.ALAW.toString().equals(MediaManager.getInstance().getPriorityCodec())) {
                if (targetEncodingList.contains(AudioFormat.Encoding.ALAW)) {
                    return convertPcmSignedToALaw(data);
                }
            } else if (AudioFormat.Encoding.ULAW.toString().equals(MediaManager.getInstance().getPriorityCodec())) {
                if (targetEncodingList.contains(AudioFormat.Encoding.ULAW)) {
                    return convertPcmSignedToULaw(data);
                }
            }
        } else if (voipClient.getTargetAudioFormat().getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
            if (AudioFormat.Encoding.ALAW.toString().equals(MediaManager.getInstance().getPriorityCodec())) {
                if (targetEncodingList.contains(AudioFormat.Encoding.ALAW)) {
                    return convertPcmUnsignedToALaw(data);
                }
            } else if (AudioFormat.Encoding.ULAW.toString().equals(MediaManager.getInstance().getPriorityCodec())) {
                if (targetEncodingList.contains(AudioFormat.Encoding.ULAW)) {
                    return convertPcmUnsignedToULaw(data);
                }
            }
        }

        return data;
    }

    /**
     * @fn public static byte[] decodeTargetCodec(byte[] data)
     * @param data Rtp Payload Data
     * @return 성공 시 디코딩된 미디어 데이터, 실패 시 디코딩 전 미디어 데이터 반환
     */
    public static byte[] decodeTargetCodec(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat;

        if (AudioFormat.Encoding.ALAW.toString().equals(MediaManager.getInstance().getPriorityCodec())) {
            sourceFormat = new AudioFormat(
                    AudioFormat.Encoding.ALAW,
                    Float.parseFloat(voipClient.getSourceSamplingRate()),
                    voipClient.getSourceSampleSize() / 2,
                    voipClient.getSourceChannelSize(),
                    voipClient.getSourceFrameSize() / 2,
                    voipClient.getSourceFrameRate(),
                    voipClient.isSourceBigEndian()
            );
        } else if (AudioFormat.Encoding.ULAW.toString().equals(MediaManager.getInstance().getPriorityCodec())) {
            sourceFormat = new AudioFormat(
                    AudioFormat.Encoding.ULAW,
                    Float.parseFloat(voipClient.getSourceSamplingRate()),
                    voipClient.getSourceSampleSize() / 2,
                    voipClient.getSourceChannelSize(),
                    voipClient.getSourceFrameSize() / 2,
                    voipClient.getSourceFrameRate(),
                    voipClient.isSourceBigEndian()
            );
        } else {
            sourceFormat = new AudioFormat(
                    VoipClient.getInstance().getSourceAudioFormat().getEncoding(),
                    Float.parseFloat(voipClient.getSourceSamplingRate()),
                    voipClient.getSourceSampleSize() / 2,
                    voipClient.getSourceChannelSize(),
                    voipClient.getSourceFrameSize() / 2,
                    voipClient.getSourceFrameRate(),
                    voipClient.isSourceBigEndian()
            );
        }

        AudioFormat.Encoding[] targetEncodings = AudioSystem.getTargetEncodings(sourceFormat);
        List<AudioFormat.Encoding> targetEncodingList = Arrays.asList(targetEncodings);

        // Source Format 에서 PCM_SIGNED 또는 PCM_UNSIGNED 로 디코딩 > 조건에 불일치하면 데이터 그대로 반환
        if (voipClient.getSourceAudioFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
            if (targetEncodingList.contains(AudioFormat.Encoding.PCM_SIGNED)) {
                if (AudioFormat.Encoding.ALAW.toString().equals(MediaManager.getInstance().getPriorityCodec())) {
                    return convertALawToPcmSigned(data);
                } else if (AudioFormat.Encoding.ULAW.toString().equals(MediaManager.getInstance().getPriorityCodec())) {
                    return convertULawToPcmSigned(data);
                }
            }
        } else if (voipClient.getSourceAudioFormat().getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
            if (targetEncodingList.contains(AudioFormat.Encoding.PCM_UNSIGNED)) {
                if (AudioFormat.Encoding.ALAW.toString().equals(MediaManager.getInstance().getPriorityCodec())) {
                    return convertALawToPcmUnsigned(data);
                } else if (AudioFormat.Encoding.ULAW.toString().equals(MediaManager.getInstance().getPriorityCodec())) {
                    return convertULawToPcmUnsigned(data);
                }
            }
        }

        return data;
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn private static byte[] convertPcmSignedToALaw(A byte[] data)
     * @param data Rtp payload byte array data, Linear-PCM signed
     * @return Rtp payload byte array data, alaw
     */
    private static byte[] convertPcmSignedToALaw(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat = new AudioFormat (
                AudioFormat.Encoding.PCM_SIGNED,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize(),
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize(),
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        //logger.debug("({}) TargetFormat: {}", AudioFormat.Encoding.PCM_SIGNED, AudioSystem.getTargetEncodings(sourceFormat));

        AudioFormat targetFormat = new AudioFormat (
                AudioFormat.Encoding.ALAW,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize() / 2,
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize() / 2,
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        return convertFormat(sourceFormat, targetFormat, data);
    }

    /**
     * @fn private static byte[] convertPcmSignedToULaw(A byte[] data)
     * @param data Rtp payload byte array data, Linear-PCM signed
     * @return Rtp payload byte array data, ulaw
     */
    private static byte[] convertPcmSignedToULaw(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat = new AudioFormat (
                AudioFormat.Encoding.PCM_SIGNED,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize(),
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize(),
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        //logger.debug("({}) TargetFormat: {}", AudioFormat.Encoding.PCM_SIGNED, AudioSystem.getTargetEncodings(sourceFormat));

        AudioFormat targetFormat = new AudioFormat (
                AudioFormat.Encoding.ULAW,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize() / 2,
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize() / 2,
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        return convertFormat(sourceFormat, targetFormat, data);
    }

    /**
     * @fn private static byte[] convertPcmUnsignedToALaw(byte[] data)
     * @param data Rtp payload byte array data, Linear-PCM unsigned
     * @return Rtp payload byte array data, alaw
     */
    private static byte[] convertPcmUnsignedToALaw(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat = new AudioFormat (
                AudioFormat.Encoding.PCM_UNSIGNED,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize(),
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize(),
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        //logger.debug("({}) TargetFormat: {}", AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.getTargetEncodings(sourceFormat));

        AudioFormat targetFormat = new AudioFormat (
                AudioFormat.Encoding.ALAW,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize() / 2,
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize() / 2,
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        return convertFormat(sourceFormat, targetFormat, data);
    }

    /**
     * @fn private static byte[] convertPcmUnSignedToULaw(A byte[] data)
     * @param data Rtp payload byte array data, Linear-PCM unsigned
     * @return Rtp payload byte array data, ulaw
     */
    private static byte[] convertPcmUnsignedToULaw(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat = new AudioFormat (
                AudioFormat.Encoding.PCM_UNSIGNED,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize(),
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize(),
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        //logger.debug("({}) TargetFormat: {}", AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.getTargetEncodings(sourceFormat));

        AudioFormat targetFormat = new AudioFormat (
                AudioFormat.Encoding.ULAW,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize() / 2,
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize() / 2,
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        return convertFormat(sourceFormat, targetFormat, data);
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn private static byte[] convertALawToPcmSigned(byte[] data)
     * @param data Rtp payload byte array data, alaw
     * @return Rtp payload byte array data, Linear-PCM signed
     */
    private static byte[] convertALawToPcmSigned(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat = new AudioFormat (
                AudioFormat.Encoding.ALAW,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize() / 2,
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize() / 2,
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        //logger.debug("({}) TargetFormat: {}", AudioFormat.Encoding.ALAW, AudioSystem.getTargetEncodings(sourceFormat));

        AudioFormat targetFormat = new AudioFormat (
                AudioFormat.Encoding.PCM_SIGNED,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize(),
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize(),
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        return convertFormat(sourceFormat, targetFormat, data);
    }

    /**
     * @fn private static byte[] convertULawToPcmSigned(byte[] data)
     * @param data Rtp payload byte array data, ulaw
     * @return Rtp payload byte array data, Linear-PCM signed
     */
    private static byte[] convertULawToPcmSigned(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat = new AudioFormat (
                AudioFormat.Encoding.ULAW,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize() / 2,
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize() / 2,
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        //logger.debug("({}) TargetFormat: {}", AudioFormat.Encoding.ULAW, AudioSystem.getTargetEncodings(sourceFormat));

        AudioFormat targetFormat = new AudioFormat (
                AudioFormat.Encoding.PCM_SIGNED,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize(),
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize(),
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        return convertFormat(sourceFormat, targetFormat, data);
    }

    /**
     * @fn private static byte[] convertALawToPcmUnsigned(byte[] data)
     * @param data Rtp payload byte array data, alaw
     * @return Rtp payload byte array data, Linear-PCM unsigned
     */
    private static byte[] convertALawToPcmUnsigned(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat = new AudioFormat (
                AudioFormat.Encoding.ALAW,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize() / 2,
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize() / 2,
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        //logger.debug("({}) TargetFormat: {}", AudioFormat.Encoding.ALAW, AudioSystem.getTargetEncodings(sourceFormat));

        AudioFormat targetFormat = new AudioFormat (
                AudioFormat.Encoding.PCM_UNSIGNED,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize(),
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize(),
                voipClient.getTargetFrameRate(),
                voipClient.isSourceBigEndian()
        );

        return convertFormat(sourceFormat, targetFormat, data);
    }

    /**
     * @fn private static byte[] convertULawToPcmUnsigned(byte[] data)
     * @param data Rtp payload byte array data, ulaw
     * @return Rtp payload byte array data, Linear-PCM unsigned
     */
    private static byte[] convertULawToPcmUnsigned(byte[] data) {
        VoipClient voipClient = VoipClient.getInstance();

        AudioFormat sourceFormat = new AudioFormat (
                AudioFormat.Encoding.ULAW,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize() / 2,
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize() / 2,
                voipClient.getTargetFrameRate(),
                voipClient.isTargetBigEndian()
        );

        //logger.debug("({}) TargetFormat: {}", AudioFormat.Encoding.ALAW, AudioSystem.getTargetEncodings(sourceFormat));

        AudioFormat targetFormat = new AudioFormat (
                AudioFormat.Encoding.PCM_UNSIGNED,
                Float.parseFloat(voipClient.getTargetSamplingRate()),
                voipClient.getTargetSampleSize(),
                voipClient.getTargetChannelSize(),
                voipClient.getTargetFrameSize(),
                voipClient.getTargetFrameRate(),
                voipClient.isSourceBigEndian()
        );

        return convertFormat(sourceFormat, targetFormat, data);
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn private static byte[] convertFormat(AudioFormat sourceFormat, AudioFormat targetFormat, byte[] sourceData)
     * @brief SourceData 를 SourceFormat 오디오에서 TargetFormat 오디오 데이터로 변환하는 함수
     * @param sourceFormat 변경 전 AudioFormat
     * @param targetFormat 변경 후 AudioFormat
     * @param sourceData 원본 Data
     * @return 성공 시 변경된 Data, 실패 시 null 반환
     */
    private static byte[] convertFormat(AudioFormat sourceFormat, AudioFormat targetFormat, byte[] sourceData) {
        AudioInputStream audioInputStream = new AudioInputStream (
                new ByteArrayInputStream(sourceData),
                sourceFormat,
                sourceData.length
        );

        audioInputStream = AudioSystem.getAudioInputStream(
                targetFormat,
                audioInputStream
        );

        return getBytesFromInputStream(
                audioInputStream
        );
    }

    public static byte[] getDataByPcmSignedFormat(byte[] data, boolean isEncoding) {
        VoipClient voipClient = VoipClient.getInstance();
        AudioFormat format;

        if (isEncoding) {
            format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    8000,
                    voipClient.getSourceSampleSize(),
                    voipClient.getSourceChannelSize(),
                    voipClient.getSourceFrameSize(),
                    8000,
                    false
            );
        } else {
            format = new AudioFormat (
                    voipClient.getSourceAudioFormat().getEncoding(),
                    Float.parseFloat(voipClient.getSourceSamplingRate()),
                    voipClient.getSourceSampleSize(),
                    voipClient.getSourceChannelSize(),
                    voipClient.getSourceFrameSize(),
                    voipClient.getSourceFrameRate(),
                    voipClient.isSourceBigEndian()
            );
        }

        return getDataByFormat(format, data);
    }

    private static byte[] getDataByFormat(AudioFormat format, byte[] data) {
        AudioInputStream audioInputStream = new AudioInputStream (
                new ByteArrayInputStream(data),
                format,
                data.length
        );

        return getBytesFromInputStream(
                audioInputStream
        );
    }

    /**
     * @fn private static byte[] getBytesFromInputStream(InputStream inputStream)
     * @brief 지정한 InputStream 에서 Byte Array Data 를 읽는 함수
     * @param inputStream InputStream
     * @return 성공 시 Byte Array Data, 실패 시 null 반환
     */
    public static byte[] getBytesFromInputStream(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            while (true) {
                byte[] buffer = new byte[100];
                int readBytes = inputStream.read(buffer);
                if (readBytes == -1) {
                    break;
                } else {
                    byteArrayOutputStream.write(
                            buffer,
                            0,
                            readBytes
                    );
                }
            }

            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
        } catch (Exception e) {
            logger.warn("ChannelHandler.getBytesFromInputStream.Exception", e);
            return null;
        }

        return byteArrayOutputStream.toByteArray();
    }

    ////////////////////////////////////////////////////////////////////////////////

    public static byte[] shortToByte(short[] shortData, boolean isBigEndian) {
        byte[] data = new byte[shortData.length * 2];
        int size = shortData.length;
        if (!isBigEndian) {
            for (int i = 0; i < size; i++) {
                data[i * 2] = (byte) shortData[i];
                data[i * 2 + 1] = (byte) (shortData[i] >> 8);
            }
        } else {
            // big Endian
            for (int i = 0; i < size; i++) {
                data[i * 2] = (byte) (shortData[i] >> 8);
                data[i * 2 + 1] = (byte) shortData[i];
            }
        }
        return data;
    }

    public static short[] byteToShort(byte[] byteData, boolean isBigEndian) {
        short[] data = new short[byteData.length / 2];
        int size = data.length;
        byte lb, hb;
        if (!isBigEndian) {
            for (int i = 0; i < size; i++) {
                lb = byteData[i * 2];
                hb = byteData[i * 2 + 1];
                data[i] = (short) (((short) hb << 8) | lb & 0xff);
            }
        } else {
            for (int i = 0; i < size; i++) {
                lb = byteData[i * 2];
                hb = byteData[i * 2 + 1];
                data[i] = (short) (((short) lb << 8) | hb & 0xff);
            }

        }
        return data;
    }

    public static int[] byteToInt(byte[] src, boolean isBigEndian) {
        int dstLength = src.length >>> 2;
        int[] dst = new int[dstLength];

        for (int i = 0; i < dstLength; i++) {
            int j = i << 2;
            int x = 0;

            if (isBigEndian) {
                // big endian
                x += (src[j++] & 0xff) << 24;
                x += (src[j++] & 0xff) << 16;
                x += (src[j++] & 0xff) << 8;
                x += (src[j] & 0xff);
            } else {
                // little endian
                x += (src[j++] & 0xff);
                x += (src[j++] & 0xff) << 8;
                x += (src[j++] & 0xff) << 16;
                x += (src[j] & 0xff) << 24;
            }

            dst[i] = x;
        }

        return dst;
    }

    public static byte[] intToByte(int[] src, boolean isBigEndian) {
        int srcLength = src.length;
        byte[] dst = new byte[srcLength << 2];

        for (int i = 0; i < srcLength; i++) {
            int x = src[i];
            int j = i << 2;

            if (isBigEndian) {
                dst[j++] = (byte) ((x >>> 24) & 0xff);
                dst[j++] = (byte) ((x >>> 16) & 0xff);
                dst[j++] = (byte) ((x >>> 8) & 0xff);
                dst[j] = (byte) ((x) & 0xff);
            } else {
                dst[j++] = (byte) ((x) & 0xff);
                dst[j++] = (byte) ((x >>> 8) & 0xff);
                dst[j++] = (byte) ((x >>> 16) & 0xff);
                dst[j] = (byte) ((x >>> 24) & 0xff);
            }
        }

        return dst;
    }

    public static double[] byteToDouble (byte[] data) {
        ByteBuffer prevByteBuffer = ByteBuffer.wrap(data);

        double[] doubles = new double[data.length / Double.BYTES];
        for(int i = 0; i < doubles.length; i++) {
            doubles[i] = prevByteBuffer.getDouble();
        }

        return doubles;
    }

    public static byte[] doubleToByte (double[] data) {
        ByteBuffer afterByteBuffer = ByteBuffer.allocate(data.length * Double.BYTES);

        for(double datum : data) {
            afterByteBuffer.putDouble(datum);
        }

        return afterByteBuffer.array();
    }

    public static byte[] floatToByte (float[] data) {
        ByteBuffer afterByteBuffer = ByteBuffer.allocate(data.length * Float.BYTES);

        for(float datum : data) {
            afterByteBuffer.putFloat(datum);
        }

        return afterByteBuffer.array();
    }

}
