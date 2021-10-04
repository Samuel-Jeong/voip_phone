package media.protocol.base;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtil {

    /** The maximum number of bytes in a UDP packet. */
    public static final int MAX_UDP_PACKET_SIZE = 65537;

    /** Number of bytes in a Java short. */
    public static final int NUM_BYTES_IN_SHORT = 2;

    /** Number of bytes in a Java int. */
    public static final int NUM_BYTES_IN_INT = 4;

    /** Number of bytes in a Java long. */
    public static final int NUM_BYTES_IN_LONG = 8;

    private static final long[] maxValueCache = new long[64];

    static {
        for (int i = 1; i < 64; i++) {
            maxValueCache[i] = ((long) 1 << i) - 1;
        }
    }

    public static byte[] shortToBytes(short s, boolean isBigEndian) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(NUM_BYTES_IN_SHORT);

        if (isBigEndian) {
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
        } else {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        byteBuffer.putShort(s);
        return byteBuffer.array();
    }

    public static short bytesToShort(byte[] bytes, boolean isBigEndian) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        if (isBigEndian) {
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
        } else {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        return byteBuffer.getShort();
    }

    public static String shortToHex(short s) {
        return Integer.toHexString(s);
    }

    public static short hexToShort(String s) {
        return Short.parseShort(s, 16);
    }

    public static byte[]  intToBytes(int i) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(NUM_BYTES_IN_INT);
        byteBuffer.putInt(i);
        return byteBuffer.array();
    }

    public static int bytesToInt(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getInt();
    }

    public static String intToHex(int i) {
        return Integer.toHexString(i);
    }

    public static int hexToInt(String s) {
        return Integer.parseInt(s, 16);
    }

    public static byte[] longToBytes(long l) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(NUM_BYTES_IN_LONG);
        byteBuffer.putLong(l);
        return byteBuffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getLong();
    }

    public static String longToHex(long l) {
        return Long.toHexString(l);
    }

    public static long hexToLong(String s) {
        return Long.parseLong(s, 16);
    }

    public static String writeBytes(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            // New line every 4 bytes
            if (i % 4 == 0) {
                stringBuffer.append("\n");
            }
            stringBuffer.append(writeBits(bytes[i])).append(" ");
        }
        return stringBuffer.toString();

    }

    public static String writeBytes(byte[] bytes, int packetLength) {
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < packetLength; i++) {
            // New line every 4 bytes
            if (i % 4 == 0) {
                stringBuffer.append("\n");
            }
            stringBuffer.append(writeBits(bytes[i])).append(" ");
        }
        return stringBuffer.toString();
    }

    public static String writeBits(byte b) {
        StringBuilder stringBuffer = new StringBuilder();
        int bit;
        for (int i = 7; i >= 0; i--) {
            bit = (b >>> i) & 0x01;
            stringBuffer.append(bit);
        }
        return stringBuffer.toString();
    }

    public static int getMaxIntValueForNumBits(int i) {
        if (i >= 32) {
            throw new RuntimeException("Number of bits exceeds Java int.");
        } else {
            return (int) maxValueCache[i];
        }
    }

    public static long getMaxLongValueForNumBits(int i) {
        if (i >= 64) {
            throw new RuntimeException("Number of bits exceeds Java long.");
        } else {
            return maxValueCache[i];
        }
    }

    public static byte[] integerToByteArray(int value, boolean isBigEndian) {
        if (isBigEndian) {
            return new byte[]{
                    (byte) ((value >> 24) & 0xff),
                    (byte) ((value >> 16) & 0xff),
                    (byte) ((value >> 8) & 0xff),
                    (byte) (value & 0xff)
            };
        } else {
            return new byte[]{
                    (byte) (value & 0xff),
                    (byte) ((value >> 8) & 0xff),
                    (byte) ((value >> 16) & 0xff),
                    (byte) ((value >> 24) & 0xff)
            };
        }
    }

    public static int byteArrayToInteger(byte [] byteArray, boolean isBigEndian) {
        if (isBigEndian) {
            return (byteArray[0] << 24) +
                    ((byteArray[1] & 0xFF) << 16) +
                    ((byteArray[2] & 0xFF) << 8) +
                    (byteArray[3] & 0xFF);
        } else {
            return (byteArray[0] & 0xFF) +
                    ((byteArray[1] & 0xFF) << 8) +
                    ((byteArray[2] & 0xFF) << 16) +
                    (byteArray[3] << 24);
        }
    }

    public static byte [] convertDoubleToByteArray(double number) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
        byteBuffer.putDouble(number);
        return byteBuffer.array();
    }

    public static byte[] convertDoubleArrayToByteArray(double[] data) {
        if (data == null) {
            return null;
        }

        byte[] bytes = new byte[data.length * Double.BYTES];
        for (int i = 0; i < data.length; i++) {
            System.arraycopy(
                    convertDoubleToByteArray(data[i]), 0,
                    bytes, i * Double.BYTES,
                    Double.BYTES
            );
        }
        return bytes;
    }

}
