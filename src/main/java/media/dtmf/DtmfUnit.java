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

    /*public static final int DIGIT_0 = 0x0000;
    public static final int DIGIT_1 = 0x0001;
    public static final int DIGIT_2 = 0x0010;
    public static final int DIGIT_3 = 0x0011;
    public static final int DIGIT_4 = 0x0100;
    public static final int DIGIT_5 = 0x0101;
    public static final int DIGIT_6 = 0x0110;
    public static final int DIGIT_7 = 0x0111;
    public static final int DIGIT_8 = 0x1000;
    public static final int DIGIT_9 = 0x1001;
    public static final int DIGIT_10 = 0x1010;
    public static final int DIGIT_11 = 0x1011;*/

    public static final int DIGIT_0 = 0;
    public static final int DIGIT_1 = 1;
    public static final int DIGIT_2 = 2;
    public static final int DIGIT_3 = 3;
    public static final int DIGIT_4 = 4;
    public static final int DIGIT_5 = 5;
    public static final int DIGIT_6 = 6;
    public static final int DIGIT_7 = 7;
    public static final int DIGIT_8 = 8;
    public static final int DIGIT_9 = 9;
    public static final int DIGIT_10 = 10;
    public static final int DIGIT_11 = 11;

    // 4 bytes
    private byte[] data = null;

    // 2 bytes
    private int digit = 0; // 1 byte
    private boolean isEndOfEvent = false; // 1 bit
    private boolean isReserved = false; // 1 bit
    private int volume = 0; // 6 bits

    // 2 bytes
    private int eventDuration = 0;

    ////////////////////////////////////////////////////////////////////////////////

    // Packing object
    public DtmfUnit(byte[] data) {
        if (data != null && data.length == 4) {
            this.data = data;

            this.digit = data[0];

            byte temp = data[1];
            isEndOfEvent = (temp & 0x80) != 0;
            volume = temp & 0x7f;

            eventDuration = (data[2] & 0xff) << 8 | (data[3] & 0xff);
        }
    }

    // Unpacking object
    public DtmfUnit(int digit, boolean isEndOfEvent, boolean isReserved, int volume, int eventDuration) {
        byte[] newData = new byte[4];

        //newData[0] = (byte) (digit >> 8);
        //newData[0] |= (byte) (digit);
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

    public int getDigit() {
        return digit;
    }

    public void setDigit(int digit) {
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

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
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
