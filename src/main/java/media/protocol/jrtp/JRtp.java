package media.protocol.jrtp;

import media.protocol.base.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.StringTokenizer;

public class JRtp {

    private static final Logger logger = LoggerFactory.getLogger(JRtp.class);

    private String ip = null;
    private int port = -1;
    private int samplingRate = 0;
    private int sampleSize = 0;
    private int channelSize = 0;
    private short gain = 0;
    private byte[] rtpData = null;

    ////////////////////////////////////////////////////////////////////////////////

    public JRtp() {
        // Nothing
    }

    public JRtp(byte[] data) {
        if (data.length >= 22) {
            int offset = 0;

            byte[] ipBytes = new byte[4];
            System.arraycopy(data, offset, ipBytes, 0, ipBytes.length);
            this.ip = getIpAddress(ipBytes);
            offset += ipBytes.length;

            byte[] portBytes = new byte[4];
            System.arraycopy(data, offset, portBytes, 0, portBytes.length);
            this.port = ByteUtil.byteArrayToInteger(portBytes, true);
            offset += portBytes.length;

            byte[] samplingRateBytes = new byte[4];
            System.arraycopy(data, offset, samplingRateBytes, 0, samplingRateBytes.length);
            this.samplingRate = ByteUtil.byteArrayToInteger(samplingRateBytes, true);
            offset += samplingRateBytes.length;

            byte[] sampleSizeBytes = new byte[4];
            System.arraycopy(data, offset, sampleSizeBytes, 0, sampleSizeBytes.length);
            this.sampleSize = ByteUtil.byteArrayToInteger(sampleSizeBytes, true);
            offset += sampleSizeBytes.length;

            byte[] channelSizeBytes = new byte[4];
            System.arraycopy(data, offset, channelSizeBytes, 0, channelSizeBytes.length);
            this.channelSize = ByteUtil.byteArrayToInteger(channelSizeBytes, true);
            offset += channelSizeBytes.length;

            byte[] gainBytes = new byte[2];
            System.arraycopy(data, offset, gainBytes, 0, gainBytes.length);
            this.gain = ByteUtil.bytesToShort(gainBytes, true);
            offset += gainBytes.length;

            byte[] rtpBytes = new byte[data.length - offset];
            System.arraycopy(data, offset, rtpBytes, 0, rtpBytes.length);
            this.rtpData = rtpBytes;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void setData(String ip, int port, int samplingRate, int sampleSize, int channelSize, short gain, byte[] rtpData) {
        this.ip = ip;
        this.port = port;
        this.samplingRate = samplingRate;
        this.sampleSize = sampleSize;
        this.channelSize = channelSize;
        this.gain = gain;
        this.rtpData = rtpData;
    }

    public byte[] getData() {
        try {
            byte[] ipBytes = InetAddress.getByName(ip).getAddress();
            byte[] portBytes = ByteUtil.integerToByteArray(port, true);
            byte[] samplingRateBytes = ByteUtil.integerToByteArray(samplingRate, true);
            byte[] sampleSizeBytes = ByteUtil.integerToByteArray(sampleSize, true);
            byte[] channelSizeBytes = ByteUtil.integerToByteArray(channelSize, true);
            byte[] gainBytes = ByteUtil.shortToBytes(gain, true);

            int offset = 0;
            byte[] totalBytes = new byte[
                    ipBytes.length +
                            portBytes.length +
                            samplingRateBytes.length +
                            sampleSizeBytes.length +
                            channelSizeBytes.length +
                            gainBytes.length +
                            rtpData.length
                    ];

            System.arraycopy(ipBytes, 0, totalBytes, offset, ipBytes.length);
            offset += ipBytes.length;

            System.arraycopy(portBytes, 0, totalBytes, offset, portBytes.length);
            offset += portBytes.length;

            System.arraycopy(samplingRateBytes, 0, totalBytes, offset, samplingRateBytes.length);
            offset += samplingRateBytes.length;

            System.arraycopy(sampleSizeBytes, 0, totalBytes, offset, sampleSizeBytes.length);
            offset += sampleSizeBytes.length;

            System.arraycopy(channelSizeBytes, 0, totalBytes, offset, channelSizeBytes.length);
            offset += channelSizeBytes.length;

            System.arraycopy(gainBytes, 0, totalBytes, offset, gainBytes.length);
            offset += gainBytes.length;

            System.arraycopy(rtpData, 0, totalBytes, offset, rtpData.length);
            return totalBytes;
        } catch (Exception e) {
            logger.warn("Fail to get the jrtp data.");
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public int getChannelSize() {
        return channelSize;
    }

    public short getGain() {
        return gain;
    }

    public byte[] getRtpData() {
        return rtpData;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private static String getIpAddress(byte[] rawBytes) {
        int i = 4;
        StringBuilder ipAddress = new StringBuilder();

        for (byte rawByte : rawBytes) {
            ipAddress.append(rawByte & 0xFF);
            if (--i > 0) {
                ipAddress.append(".");
            }
        }

        return ipAddress.toString();
    }

    public static byte[] ipStringToByteArray(String ip) {
        int ipInt = parseNumericAddress(ip);
        if (ipInt == 0) {
            return null;
        }

        // Convert to bytes
        byte[] ipBytes = new byte[4];

        ipBytes[3] = (byte) (ipInt & 0xFF);
        ipBytes[2] = (byte) ((ipInt >> 8) & 0xFF);
        ipBytes[1] = (byte) ((ipInt >> 16) & 0xFF);
        ipBytes[0] = (byte) ((ipInt >> 24) & 0xFF);

        return ipBytes;
    }

    public static int parseNumericAddress(String ip) {
        if (ip == null || ip.length() < 7 || ip.length() > 15) {
            return 0;
        }

        StringTokenizer token = new StringTokenizer(ip, ".");
        if (token.countTokens() != 4) {
            return 0;
        }

        int ipInt = 0;

        while (token.hasMoreTokens()) {
            String ipNum = token.nextToken();

            try {
                int ipVal = Integer.parseInt(ipNum);
                if (ipVal < 0 || ipVal > 255) {
                    return 0;
                }

                ipInt = (ipInt << 8) + ipVal;
            } catch (NumberFormatException ex) {
                return 0;
            }
        }

        return ipInt;
    }

    @Override
    public String toString() {
        return "JRtp{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", samplingRate=" + samplingRate +
                ", sampleSize=" + sampleSize +
                ", channelSize=" + channelSize +
                ", gain=" + gain +
                '}';
    }
}
