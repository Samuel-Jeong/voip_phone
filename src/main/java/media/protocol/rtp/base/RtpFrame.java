package media.protocol.rtp.base;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @class public class RtpFrame
 * @brief RtpFrame class
 */
public class RtpFrame {

    private final MemoryPartition memoryPartition;
    private byte[] data;

    private volatile int offset;
    private volatile int length;

    private volatile long timestamp;
    private volatile long duration = Long.MAX_VALUE;
    private volatile long sn;

    /* End Of Message */
    private volatile boolean eom;

    private volatile RtpFormat rtpFormat;
    private volatile String header;

    protected AtomicBoolean inPartition = new AtomicBoolean(false);

    ////////////////////////////////////////////////////////////////////////////////

    protected RtpFrame(MemoryPartition memoryPartition, byte[] data) {
        this.memoryPartition = memoryPartition;
        this.data = data;
    }

    ////////////////////////////////////////////////////////////////////////////////

    protected void reset() {
        this.timestamp = 0;
        this.duration = 0;
        this.sn = 0;
        this.eom = false;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSequenceNumber(){
        return sn;
    }

    public void setSequenceNumber(long sn) {
        this.sn = sn;
    }

    public boolean isEOM() {
        return this.eom;
    }

    public void setEOM(boolean value) {
        this.eom = value;
    }

    public RtpFormat getRtpFormat() {
        return rtpFormat;
    }

    public void setRtpFormat(RtpFormat rtpFormat) {
        this.rtpFormat = rtpFormat;
    }

    public void recycle() {
        memoryPartition.recycle(this);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public RtpFrame clone() {
        RtpFrame frame = Memory.allocate(data.length);
        if (frame != null) {
            System.arraycopy(data, offset, frame.data, offset, length);
            frame.offset = offset;
            frame.length = length;
            frame.duration = duration;
            frame.sn = sn;
            frame.eom = eom;
            frame.rtpFormat = rtpFormat;
            frame.timestamp = timestamp;
            frame.header = header;
            return frame;
        }

        return this;
    }

    @Override
    public String toString() {
        return "RtpFrame{" +
                "offset=" + offset +
                ", length=" + length +
                ", timestamp=" + timestamp +
                ", duration=" + duration +
                ", sn=" + sn +
                ", eom=" + eom +
                ", rtpFormat=" + rtpFormat +
                ", inPartition=" + inPartition +
                '}';
    }
}
