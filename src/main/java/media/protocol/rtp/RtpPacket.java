package media.protocol.rtp;

import media.protocol.base.ByteUtil;
import media.protocol.rtp.util.RtpException;

import java.io.Serializable;

/**
 * @class public class RtpPacket implements Serializable
 * @brief RtpPacket class
 * Reference: https://datatracker.ietf.org/doc/html/rfc3550
 *
 *    The RTP header has the following format:
 *
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |V=2|P|X|  CC   |M|     PT      |       sequence number         |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                           timestamp                           |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |           synchronization source (SSRC) identifier            |
 *    +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
 *    |            contributing source (CSRC) identifiers             |
 *    |                             ....                              |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class RtpPacket implements Serializable {

    private static final long serialVersionUID = 0;

    public static final int FIXED_HEADER_LENGTH = 12;
    public static final int MAX_PAYLOAD_BUFFER_SIZE = 1446; // 42 + 12 + 1446 = 1500

    /** Version number: 2 bits
     * The version defined by
     *       this specification is two.
     * The value 1 is used by the first
     *       draft version of RTP and the value 0 is used by the protocol
     *       initially implemented in the "vat" audio tool.
     */
    private int version = 0;

    /** Padding: 1 bit
     * Padding may be needed by some encryption algorithms with fixed block sizes
     *       or for carrying several RTP packets in a lower-layer protocol data
     *       unit.
     */
    private int padding = 0;

    /** Header extension: 1 bit
     * If the extension bit is set, the fixed header MUST be followed by
     *       exactly one header extension, with a format defined in Section 5.3.1.
     */
    private int extension = 0;

    /** CSRC(contributing source) count: 4 bits
     * The CSRC count contains
     *       the number of CSRC identifiers that follow the fixed header.
     */
    private int csrc = 0;

    /** Marker: 1 bit
     * It is intended to allow significant events
     *       such as frame boundaries to be marked in the packet stream.
     */
    private int marker = 0;

    /** Payload type: 7 bits
     * This field identifies the format of the RTP payload and determines
     *       its interpretation by the application.
     */
    private int payloadType = 0;

    /** Sequence number: 16 bits
     * The sequence number increments by one for each RTP data packet
     *       sent, and may be used by the receiver to detect packet loss and to
     *       restore packet sequence.  The initial value of the sequence number
     *       SHOULD be random (unpredictable) to make known-plaintext attacks
     *       on encryption more difficult, even if the source itself does not
     *       encrypt according to the method in Section 9.1, because the
     *       packets may flow through a translator that does.
     */
    protected int seqNum = 0;

    /** Time stamp: 32 bits
     * The timestamp reflects the sampling instant of the first octet in
     *       the RTP data packet.  The sampling instant MUST be derived from a
     *       clock that increments monotonically and linearly in time to allow
     *       synchronization and jitter calculations (see Section 6.4.1).
     * If an audio application reads blocks covering 160 sampling periods from the input device,
     *       the timestamp would be increased by 160 for each such block,
     *       regardless of whether the block is transmitted in a packet or dropped as silent.
     * The initial value of the timestamp SHOULD be random, as for the sequence number.
     */
    private long timeStamp = 0;

    /** Synchronization source: 32 bits
     * The SSRC field identifies the synchronization source.
     *       This identifier SHOULD be chosen randomly,
     *       with the intent that no two synchronization sources
     *       within the same RTP session will have the same SSRC identifier.
     */
    private long ssrc = 0;

    /** CSRC list: 0 to 15 items, 32 bits each
     * The CSRC list identifies the contributing sources for the payload
     *       contained in this packet.  The number of identifiers is given by
     *       the CC field.  If there are more than 15 contributing sources,
     *       only 15 can be identified.  CSRC identifiers are inserted by
     *       mixers (see Section 7.1), using the SSRC identifiers of
     *       contributing sources.  For example, for audio packets the SSRC
     *       identifiers of all sources that were mixed together to create a
     *       packet are listed, allowing correct talker indication at the
     *       receiver.
     */
    private long[] csrcList = null;

    /** The payload.
     * Using for application/sdp
     */
    private byte[] payload = null;

    /** The length of the payload.
     * Using for application/sdp
     */
    private int payloadLength = 0;

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public RtpPacket()
     * @brief RtpPacket 생성자 함수
     */
    public RtpPacket() {
        // Nothing
    }

    /**
     * @fn public RtpPacket(byte[] data, int dataLength)
     * @brief 지정한 Byte Array Data 을 Rtp Packet 으로 Unpacking 하는 생성자 함수
     * @param data Rtp Byte Array Data
     * @param dataLength Rtp Byte Array Length
     */
    public RtpPacket(byte[] data, int dataLength) {
        /* 1) Extract Version to Sequence Number (32 bits) */
        int vToSnLength = 4; // # bytes
        int offset = vToSnLength;
        byte[] vToSnBytes = new byte[vToSnLength];
        System.arraycopy(data, 0, vToSnBytes, 0, vToSnLength);
        int vToSn = ByteUtil.bytesToInt(vToSnBytes);
        version = (vToSn >>> 0x1E) & 0x03;
        padding = (vToSn >>> 0x1D) & 0x01;
        extension = (vToSn >>> 0x1C) & 0x01;
        csrc = (vToSn >>> 0x18) & 0x0F;
        marker = (vToSn >>> 0x17) & 0x01;
        payloadType = (vToSn >>> 0x10) & 0x7F;
        seqNum = (vToSn & 0xFFFF);

        /* 2) Extract Timestamp */
        int timeStampLength = 4; // 4 bytes arriving, need to store as long
        byte[] timeStampBytes = new byte[timeStampLength];
        System.arraycopy(data, offset, timeStampBytes, 0, timeStampLength);
        byte[] timeStampLongBytes = new byte[8]; // Copy to long byte array
        System.arraycopy(timeStampBytes, 0, timeStampLongBytes, 4, 4);
        timeStamp = ByteUtil.bytesToLong(timeStampLongBytes);
        offset += timeStampLength;

        /* 3) Extract SSRC */
        int ssrcLength = 4; // 4 bytes arriving, need to store as long
        byte[] ssrcBytes = new byte[ssrcLength];
        System.arraycopy(data, offset, ssrcBytes, 0, ssrcLength);
        byte[] ssrcLongBytes = new byte[8]; // Copy to long byte array
        System.arraycopy(ssrcBytes, 0, ssrcLongBytes, 4, 4);
        ssrc = ByteUtil.bytesToLong(ssrcLongBytes);
        offset += ssrcLength;

        /* 4) Extract Payload */
        int payloadLength = (dataLength - offset); // # bytes
        this.payloadLength = payloadLength;
        payload = new byte[payloadLength];
        System.arraycopy(data, offset, payload, 0, payloadLength);

        /*logger.debug("[RTPPacket] Unpacking: {}", ByteUtil.writeBytes(data));
        logger.debug("[RTPPacket] Unpacked V: {}", version);
        logger.debug("[RTPPacket] Unpacked P: {}", padding);
        logger.debug("[RTPPacket] Unpacked X: {}", extension);
        logger.debug("[RTPPacket] Unpacked CC: {}", csrc);
        logger.debug("[RTPPacket] Unpacked M: {}", marker);
        logger.debug("[RTPPacket] Unpacked PT: {}", payloadType);
        logger.debug("[RTPPacket] Unpacked SN: {}", seqNum);
        logger.debug("[RTPPacket] Unpacked TS: {}", timeStamp);
        logger.debug("[RTPPacket] Unpacked SSRC: {}", ssrc);
        logger.debug("[RTPPacket] Unpacked payload: {}", ByteUtil.writeBytes(payload));*/
    }

    /**
     * @fn public byte[] getData()
     * @brief Rtp Packet 을 Packing 하는 함수
     * @return Packed Rtp Data.
     */
    public byte[] getData() {
        int vToSn = 0;
        vToSn |= version; // Add V
        vToSn <<= 0x01; // Make room for P
        vToSn |= padding; // Add P
        vToSn <<= 0x01; // Make room for X
        vToSn |= extension; // Add X
        vToSn <<= 0x04; // Make room for CC
        vToSn |= csrc; // Add CC
        vToSn <<= 0x01; // Make room for M
        vToSn |= marker; // Add M
        vToSn <<= 0x07; // Make room for PT
        vToSn |= payloadType; // Add PT
        vToSn <<= 0x10; // Make room for SN
        vToSn |= seqNum; // Add SN

        byte[] vToSnBytes = ByteUtil.intToBytes(vToSn);
        byte[] timeStampBytes = ByteUtil.intToBytes((int) timeStamp);
        byte[] ssrcBytes = ByteUtil.intToBytes((int) ssrc);

        int totalDataLength = vToSnBytes.length + timeStampBytes.length + ssrcBytes.length + payloadLength;
        byte[] data = new byte[totalDataLength];

        int offset = 0;
        System.arraycopy(vToSnBytes, 0, data, offset, vToSnBytes.length);

        offset += vToSnBytes.length;
        System.arraycopy(timeStampBytes, 0, data, offset, timeStampBytes.length);

        offset += timeStampBytes.length;
        System.arraycopy(ssrcBytes, 0, data, offset, ssrcBytes.length);

        offset += ssrcBytes.length;
        System.arraycopy(payload, 0, data, offset, payloadLength);

        /*logger.debug("[RTPPacket] Packing V: {}", version);
        logger.debug("[RTPPacket] Packing P: {}", padding);
        logger.debug("[RTPPacket] Packing X: {}", extension);
        logger.debug("[RTPPacket] Packing CC: {}", csrc);
		logger.debug("[RTPPacket] Packing M: {}", marker);
        logger.debug("[RTPPacket] Packing PT: {}", payloadType);
        logger.debug("[RTPPacket] Packing SN: {}", seqNum);
        logger.debug("[RTPPacket] Packing TS: {}", timeStamp);
        logger.debug("[RTPPacket] Packing SSRC: {}", ssrc);
        logger.debug("[RTPPacket] Packing payload: {}", ByteUtil.writeBytes(payload));
        logger.debug("[RTPPacket] Packed: " + ByteUtil.writeBytes(data));*/

        return data;
    }

    /**
     * @fn public void setValue
     * @brief Rtp Value 를 한 번에 설정하는 함수
     * @param version Version
     * @param padding Padding
     * @param extension Extension
     * @param csrc CSRC
     * @param marker Marker
     * @param payloadType Payload type (ID)
     * @param seqNum Sequence Number
     * @param timeStamp TimeStamp
     * @param ssrc SSRC
     * @param payload Payload
     * @param payloadLength Payload length
     */
    public void setValue (
            int version, int padding, int extension, int csrc, int marker, int payloadType, int seqNum,
            long timeStamp,
            long ssrc,
            byte[] payload,
            int payloadLength) {
        setVersion(version);
        setPadding(padding);
        setExtension(extension);
        setMarker(marker);
        setCsrc(csrc);
        setSeqNum(seqNum);
        setSsrc(ssrc);
        setTimeStamp(timeStamp);
        setPayloadType(payloadType);
        setPayload(payload, payloadLength);
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @fn public void setVersion(int i) throws IllegalArgumentException
     * @brief Set the version bit.
     * @param i version (1 bit).
     */
    public void setVersion(int i) throws IllegalArgumentException {
        if ((0 <= i) && (i <= ByteUtil.getMaxIntValueForNumBits(2))) {
            version = i;
        } else {
            throw new IllegalArgumentException(RtpException.OUT_OF_RANGE);
        }
    }

    /**
     * @fn public int getVersion()
     * @brief Get the RTP version.
     * @return Version value.
     */
    public int getVersion() {
        return version;

    }

    /**
     * @fn public void setPadding(int i) throws IllegalArgumentException
     * @brief Set the padding bit.
     * @param i padding (1 bit).
     */
    public void setPadding(int i) throws IllegalArgumentException {
        if ((0 <= i) && (i <= ByteUtil.getMaxIntValueForNumBits(1))) {
            padding = i;
        } else {
            throw new IllegalArgumentException(RtpException.OUT_OF_RANGE);
        }
    }

    /**
     * @fn public int getPadding()
     * @brief Get the padding bit.
     * @return Padding value.
     */
    public int getPadding() {
        return padding;
    }

    /**
     * @fn public void setExtension(int i) throws IllegalArgumentException
     * @brief Set the extension.
     * @param i extension (1 bit)
     */
    public void setExtension(int i) throws IllegalArgumentException {
        if ((0 <= i) && (i <= ByteUtil.getMaxIntValueForNumBits(1))) {
            extension = i;
        } else {
            throw new IllegalArgumentException(RtpException.OUT_OF_RANGE);
        }
    }

    /**
     * @fn public int getExtension()
     * @brief Get the extension.
     * @return Extension value.
     */
    public int getExtension() {
        return extension;
    }

    /**
     * @fn public void setCsrc(int i) throws IllegalArgumentException
     * @brief Set the CSRC count.
     * @param i CSRC count (4 bits)
     */
    public void setCsrc(int i) throws IllegalArgumentException {
        if ((0 <= i) && (i <= ByteUtil.getMaxIntValueForNumBits(4))) {
            csrc = i;
        } else {
            throw new IllegalArgumentException(RtpException.OUT_OF_RANGE);
        }
    }

    /**
     * @fn public int getCsrc()
     * @brief Get the CSRC count.
     * @return CSRC count value.
     */
    public int getCsrc() {
        return csrc;
    }

    /**
     * @fn public void setMarker(int i) throws IllegalArgumentException
     * @brief Set the marker.
     * @param i marker (1 bit)
     */
    public void setMarker(int i) throws IllegalArgumentException {
        if ((0 <= i) && (i <= ByteUtil.getMaxIntValueForNumBits(1))) {
            marker = i;
        } else {
            throw new IllegalArgumentException(RtpException.OUT_OF_RANGE);
        }
    }

    /**
     * @fn public int getMarker()
     * @brief Get the marker.
     * @return Marker value.
     */
    public int getMarker() {
        return marker;
    }

    /**
     * @fn public void setPayloadType(int i) throws IllegalArgumentException
     * @brief Set the payload type.
     * @param i payload type (7 bits)
     */
    public void setPayloadType(int i) throws IllegalArgumentException {
        if ((0 <= i) && (i <= ByteUtil.getMaxIntValueForNumBits(7))) {
            payloadType = i;
        } else {
            throw new IllegalArgumentException(RtpException.OUT_OF_RANGE);
        }
    }

    /**
     * @fn public int getPayloadType()
     * @brief Get the payload type.
     * @return Payload type value.
     */
    public int getPayloadType() {
        return payloadType;
    }

    /**
     * @fn public void setSeqNum(int i) throws IllegalArgumentException
     * @brief Set the sequence number.
     * @param i sequence number (16 bits)
     */
    public void setSeqNum(int i) throws IllegalArgumentException {
        if ((0 <= i) && (i <= ByteUtil.getMaxIntValueForNumBits(16))) {
            seqNum = i;
        } else {
            throw new IllegalArgumentException(RtpException.OUT_OF_RANGE);
        }
    }

    /**
     * @fn public int getSeqNum()
     * @brief Get the sequence number.
     * @return Sequence number value.
     */
    public int getSeqNum() {
        return seqNum;
    }

    /**
     * @fn public void setTimeStamp(long timeStamp) throws IllegalArgumentException
     * @brief Set the time stamp.
     * @param timeStamp time stamp (32 bits).
     */
    public void setTimeStamp(long timeStamp) throws IllegalArgumentException {
        if ((0 <= timeStamp) && (timeStamp <= ByteUtil.getMaxLongValueForNumBits(32))) {
            this.timeStamp = timeStamp;
        } else {
            throw new IllegalArgumentException(RtpException.OUT_OF_RANGE);
        }
    }

    /**
     * @fn public long getTimeStamp()
     * @brief Get the time stamp.
     * @return Time stamp value.
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * @fn public void setSsrc(long ssrc) throws IllegalArgumentException
     * @brief Set the synchronization source identifier.
     * @param ssrc synchronization source identifier (32 bits)
     */
    public void setSsrc(long ssrc) throws IllegalArgumentException {
        if ((0 <= ssrc) && (ssrc <= ByteUtil.getMaxLongValueForNumBits(32))) {
            this.ssrc = ssrc;
        } else {
            throw new IllegalArgumentException(RtpException.OUT_OF_RANGE + ssrc);
        }
    }

    /**
     * @fn public long getSsrc()
     * @brief Get the synchronization source identifier.
     * @return the synchronization source identifier.
     */
    public long getSsrc() {
        return ssrc;
    }

    /**
     * @fn public byte[] getPayload()
     * @brief Get the payload of this RTP packet.
     * @return the payload of this RTP packet.
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * @fn public void setPayload(byte[] bytes, int length) throws IllegalArgumentException
     * @brief Set the payload of this RTP packet.
     * @param bytes Byte buffer containing the payload
     * @param length The number of buffer bytes containing the payload.
     */
    public void setPayload(byte[] bytes, int length) throws IllegalArgumentException {
        /*if (length > MAX_PAYLOAD_BUFFER_SIZE) {
            throw new IllegalArgumentException("Payload is too large(" + length + "). Max Size is limited to " + MAX_PAYLOAD_BUFFER_SIZE);
        }*/

        payloadLength = length;
        payload = bytes;
    }

    /**
     * @fn public int getPayloadLength()
     * @brief Get the payload length.
     * @return Payload length value.
     */
    public int getPayloadLength() {
        return payloadLength;
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "RtpPacket{" +
                "version=" + version +
                ", padding=" + padding +
                ", extension=" + extension +
                ", csrc=" + csrc +
                ", marker=" + marker +
                ", payloadType=" + payloadType +
                ", seqNum=" + seqNum +
                ", timeStamp=" + timeStamp +
                ", ssrc=" + ssrc +
                ", payloadLength=" + payloadLength +
                '}';
    }
}
