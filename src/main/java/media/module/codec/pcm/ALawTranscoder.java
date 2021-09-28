package media.module.codec.pcm;

/**
 * @class public class ALawTranscoder
 * @brief ALawTranscoder class
 */
public class ALawTranscoder {

    private static final int cClip = 32635;

    private static final short[] aLawDecompressTable = new short[]{
            -5504, -5248, -6016, -5760, -4480, -4224, -4992, -4736,
            -7552, -7296, -8064, -7808, -6528, -6272, -7040, -6784,
            -2752, -2624, -3008, -2880, -2240, -2112, -2496, -2368,
            -3776, -3648, -4032, -3904, -3264, -3136, -3520, -3392,
            -22016, -20992, -24064, -23040, -17920, -16896, -19968, -18944,
            -30208, -29184, -32256, -31232, -26112, -25088, -28160, -27136,
            -11008, -10496, -12032, -11520, -8960, -8448, -9984, -9472,
            -15104, -14592, -16128, -15616, -13056, -12544, -14080, -13568,
            -344, -328, -376, -360, -280, -264, -312, -296,
            -472, -456, -504, -488, -408, -392, -440, -424,
            -88, -72, -120, -104, -24, -8, -56, -40,
            -216, -200, -248, -232, -152, -136, -184, -168,
            -1376, -1312, -1504, -1440, -1120, -1056, -1248, -1184,
            -1888, -1824, -2016, -1952, -1632, -1568, -1760, -1696,
            -688, -656, -752, -720, -560, -528, -624, -592,
            -944, -912, -1008, -976, -816, -784, -880, -848,
            5504, 5248, 6016, 5760, 4480, 4224, 4992, 4736,
            7552, 7296, 8064, 7808, 6528, 6272, 7040, 6784,
            2752, 2624, 3008, 2880, 2240, 2112, 2496, 2368,
            3776, 3648, 4032, 3904, 3264, 3136, 3520, 3392,
            22016, 20992, 24064, 23040, 17920, 16896, 19968, 18944,
            30208, 29184, 32256, 31232, 26112, 25088, 28160, 27136,
            11008, 10496, 12032, 11520, 8960, 8448, 9984, 9472,
            15104, 14592, 16128, 15616, 13056, 12544, 14080, 13568,
            344, 328, 376, 360, 280, 264, 312, 296,
            472, 456, 504, 488, 408, 392, 440, 424,
            88, 72, 120, 104, 24, 8, 56, 40,
            216, 200, 248, 232, 152, 136, 184, 168,
            1376, 1312, 1504, 1440, 1120, 1056, 1248, 1184,
            1888, 1824, 2016, 1952, 1632, 1568, 1760, 1696,
            688, 656, 752, 720, 560, 528, 624, 592,
            944, 912, 1008, 976, 816, 784, 880, 848
    };

    private static final byte[] aLawCompressTable = new byte[] { 1, 1, 2, 2, 3, 3, 3,
            3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 };

    ////////////////////////////////////////////////////////////////////////////////

    public static byte[] decode(byte[] data) {
        int j = 0;
        byte[] res = new byte[data.length * 2];

        for (byte b : data) {
            short s = aLawDecompressTable[b & 0xff];
            res[j++] = (byte) s;
            res[j++] = (byte) (s >> 8);
        }

        return res;
    }

    public static int decode(byte[] src, int offset, int len, byte[] res) {
        int j = 0;

        for (int i = 0; i < len; i++) {
            short s = aLawDecompressTable[src[i + offset] & 0xff];
            res[j++] = (byte) s;
            res[j++] = (byte) (s >> 8);
        }

        return j;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public static byte[] encode(byte[] data) {
        byte[] res = new byte[data.length/2];

        encode(data, 0, data.length, res);

        return res;
    }

    public static int encode(byte[] src, int offset, int len, byte[] res) {
        int j = offset;
        int count = len / 2;
        short sample;

        for (int i = 0; i < count; i++) {
            sample = (short) ((src[j++] & 0xff) | (src[j++]) << 8);
            res[i] = linearToALaw(sample);
        }

        return count;
    }

    private static byte linearToALaw(short sample) {
        int sign;
        int exponent;
        int mantissa;
        int s;

        sign = ((~sample) >> 8) & 0x80;

        if (sign != 0x80) {
            sample = (short) -sample;
        }

        if (sample > cClip) {
            sample = cClip;
        }

        if (sample >= 256) {
            exponent = aLawCompressTable[(sample >> 8) & 0x7F];
            mantissa = (sample >> (exponent + 3)) & 0x0F;
            s = (exponent << 4) | mantissa;
        } else {
            s = sample >> 4;
        }

        s ^= (sign ^ 0x55);

        return (byte) s;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private static final int MAX = 32635; //32767 (max 15-bit integer) minus BIAS

    public static byte[] encodeByPcm(byte[] data) {
        int size = data.length / 2;
        byte[] encoded = new byte[size];

        for (int i = 0; i < size; i++) {
            encoded[i] = encodeByPcm((data[2 * i + 1] << 8) | data[2 * i]);
        }

        return encoded;
    }

    private static byte encodeByPcm(int pcm) {
        //Get the sign bit. Shift it for later use
        //without further modification
        int sign = (pcm & 0x8000) >> 8;

        //If the number is negative,
        //make it positive (now it's a magnitude)
        if (sign != 0) {
            pcm = -pcm;
        }

        //The magnitude must fit in 15 bits to avoid overflow
        if (pcm > MAX) {
            pcm = MAX;
        }

        /* Finding the "exponent"
         * Bits:
         * 1 2 3 4 5 6 7 8 9 A B C D E F G
         * S 7 6 5 4 3 2 1 0 0 0 0 0 0 0 0
         * We want to find where the first 1 after the sign bit is.
         * We take the corresponding value
         * from the second row as the exponent value.
         * (i.e. if first 1 at position 7 -> exponent = 2)
         * The exponent is 0 if the 1 is not found in bits 2 through 8.
         * This means the exponent is 0 even if the "first 1" doesn't exist.
         */
        int exponent = 7;

        //Move to the right and decrement exponent
        //until we hit the 1 or the exponent hits 0
        int expMask = 0x4000;
        while ((pcm & expMask) == 0
                && exponent > 0) {
            exponent--;
            expMask >>= 1;
        }

        /* The last part - the "mantissa"
         * We need to take the four bits after the 1 we just found.
         * To get it, we shift 0x0f :
         * 1 2 3 4 5 6 7 8 9 A B C D E F G
         * S 0 0 0 0 0 1 . . . . . . . . . (say that exponent is 2)
         * . . . . . . . . . . . . 1 1 1 1
         * We shift it 5 times for an exponent of two, meaning
         * we will shift our four bits (exponent + 3) bits.
         * For convenience, we will actually just
         * shift the number, then AND with 0x0f.
         *
         * NOTE: If the exponent is 0:
         * 1 2 3 4 5 6 7 8 9 A B C D E F G
         * S 0 0 0 0 0 0 0 Z Y X W V U T S (we know nothing about bit 9)
         * . . . . . . . . . . . . 1 1 1 1
         * We want to get ZYXW, which means a shift of 4 instead of 3
         */
        int mantissa = (pcm >> ((exponent == 0) ? 4 : (exponent + 3))) & 0x0f;

        //The a-law byte bit arrangement is SEEEMMMM
        //(Sign, Exponent, and Mantissa.)
        byte alaw = (byte) (sign | exponent << 4 | mantissa);

        //Last is to flip every other bit, and the sign bit (0xD5 = 1101 0101)
        return (byte) (alaw ^ 0xD5);
    }

    ////////////////////////////////////////////////////////////////////////////////

    public static short[] decodeToPcm(byte[] data) {
        short[] decoded = new short[data.length];

        for (int i = 0; i < data.length; i++) {
            decoded[i] = decodeToPcm(data[i]);
        }

        return decoded;
    }

    private static short decodeToPcm(byte alaw) {
        //Invert every other bit,
        //and the sign bit (0xD5 = 1101 0101)
        alaw ^= 0xD5;

        //Pull out the value of the sign bit
        int sign = alaw & 0x80;

        //Pull out and shift over the value of the exponent
        int exponent = (alaw & 0x70) >> 4;

        //Pull out the four bits of data
        int data = alaw & 0x0f;

        //Shift the data four bits to the left
        data <<= 4;

        //Add 8 to put the result in the middle
        //of the range (like adding a half)
        data += 8;

        //If the exponent is not 0, then we know the four bits followed a 1,
        //and can thus add this implicit 1 with 0x100.
        if (exponent != 0) {
            data += 0x100;
        }

        /* Shift the bits to where they need to be: left (exponent - 1) places
         * Why (exponent - 1) ?
         * 1 2 3 4 5 6 7 8 9 A B C D E F G
         * . 7 6 5 4 3 2 1 . . . . . . . . <-- starting bit (based on exponent)
         * . . . . . . . Z x x x x 1 0 0 0 <-- our data (Z is 0 only when <BR>     * exponent is 0)
         * We need to move the one under the value of the exponent,
         * which means it must move (exponent - 1) times
         * It also means shifting is unnecessary if exponent is 0 or 1.
         */
        if (exponent > 1) {
            data <<= (exponent - 1);
        }

        return (short) (sign == 0 ? data : -data);
    }

}
