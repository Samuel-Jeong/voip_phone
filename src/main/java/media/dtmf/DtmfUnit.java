package media.dtmf;

import media.protocol.base.ByteUtil;

/**
 * @class public class DtmfUnit
 * @brief DtmfUnit class
 *
 * @ Out-of-band : send the dtmf to signaling tunnel.
 * - Cannot present the dtmf duration.
 * - 송신자는 누른 시간만큼 삐 소리를 듣지만, 수신자는 한 번만 들린다.
 *
 * @ In-of-band : send the dtmf to media tunnel.
 * - Can present the dtmf duration.
 * - RTP 를 사용하는 모든 프로토콜에서 사용
 * - Bypass 와 RFC-2833 방식
 * - Bypass : dtmf 를 RTP 가 사용하는 압축코덱으로 음성과 같이 송신 > 변형되거나 손실 가능
 * - RFC-2833 : RTP 패킷에 DTMF 의 번호와 볼륨, 시간을 명시해서 송신
 *
 *
 * 2.3.  Payload Format
 *
 *    The payload format for named telephone events is shown in Figure 1.
 *
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |     event     |E|R| volume    |          duration             |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * @ DATA (12 digits)
 * - 0 : 0000 0000
 * - 1 : 0000 0001
 * - 2 : 0000 0010
 * - 3 : 0000 0011
 * - 4 : 0000 0100
 * - 5 : 0000 0101
 * - 6 : 0000 0110
 * - 7 : 0000 0111
 * - 8 : 0000 1000
 * - 9 : 0000 1001
 * - * : 0000 1010
 * - # : 0000 1011
 */
public class DtmfUnit {

    public static final int DTMF_TYPE = 101;

    public static final char DIGIT_0 = 0x00000000;
    public static final char DIGIT_1 = 0x00000001;
    public static final char DIGIT_2 = 0x00000010;
    public static final char DIGIT_3 = 0x00000011;
    public static final char DIGIT_4 = 0x00000100;
    public static final char DIGIT_5 = 0x00000101;
    public static final char DIGIT_6 = 0x00000110;
    public static final char DIGIT_7 = 0x00000111;
    public static final char DIGIT_8 = 0x00001000;
    public static final char DIGIT_9 = 0x00001001;
    public static final char DIGIT_10 = 0x00001010;
    public static final char DIGIT_11 = 0x00001011;

    // 4 bytes
    private byte[] data = null;

    // 2 bytes
    private char digit = 0; // 1 byte
    private boolean isEndOfEvent = false; // 1 bit
    private boolean isReserved = false; // 1 bit
    private short volume = 0; // 6 bits

    // 2 bytes
    private int eventDuration = 0;

    ////////////////////////////////////////////////////////////////////////////////

    // Packing object
    public DtmfUnit(byte[] data) {
        if (data != null && data.length == 4) {
            this.data = data;

            this.digit = (char) data[0];

            this.isEndOfEvent = (data[1] & 0x10000000) == 0x10000000;
            this.isReserved = (data[1] & 0x01000000) == 0x01000000;

            byte[] volumeData = {(byte) (data[1] & 0x00111111)};
            this.volume = ByteUtil.bytesToShort(volumeData, false);

            byte[] durationData = new byte[2];
            durationData[0] = data[2];
            durationData[1] = data[3];
            this.eventDuration = ByteUtil.bytesToInt(durationData);
        }
    }

    // Unpacking object
    public DtmfUnit(char digit, boolean isEndOfEvent, boolean isReserved, short volume, int eventDuration) {
        byte[] newData = new byte[4];

        newData[0] = (byte) digit;
        this.digit = digit;

        //byte[] volumeData = ByteUtil.shortToBytes(volume, false);
        newData[1] = isEndOfEvent? (byte) (volume | 0x80) : (byte) (volume | 0x7f);
        this.volume = volume;

        newData[2] = (byte) (eventDuration >> 8);
        newData[3] = (byte) (eventDuration);
        this.eventDuration = eventDuration;

        this.data = newData;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public char getDigit() {
        return digit;
    }

    public void setDigit(char digit) {
        this.digit = digit;
    }

    public boolean isEndOfEvent() {
        return isEndOfEvent;
    }

    public void setEndOfEvent(boolean endOfEvent) {
        isEndOfEvent = endOfEvent;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }

    public short getVolume() {
        return volume;
    }

    public void setVolume(short volume) {
        this.volume = volume;
    }

    public int getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(int eventDuration) {
        this.eventDuration = eventDuration;
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "DtmfUnit{" +
                "digit=" + digit +
                ", isEndOfEvent=" + isEndOfEvent +
                ", isReserved=" + isReserved +
                ", volume=" + volume +
                ", eventDuration=" + eventDuration +
                '}';
    }
}
