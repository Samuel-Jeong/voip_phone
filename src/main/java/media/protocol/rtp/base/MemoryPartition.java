package media.protocol.rtp.base;

import media.module.mixing.base.ConcurrentCyclicFIFO;

/**
 * @class public class Partition
 * @brief Partition class
 */
public class MemoryPartition {

    private final ConcurrentCyclicFIFO<RtpFrame> heap = new ConcurrentCyclicFIFO<>();

    protected int size;

    //////////////////////////////////////////////////////////////////////

    protected MemoryPartition(int size) {
        this.size = size;
    }

    //////////////////////////////////////////////////////////////////////

    protected RtpFrame allocate() {
        RtpFrame result = heap.poll();

        if (result == null) {
            return new RtpFrame(
                    this,
                    new byte[size]
            );
        }

        result.inPartition.set(false);
        return result;
    }

    public void recycle(RtpFrame frame) {
        if(frame.inPartition.getAndSet(true)) {
            return;
        }

        frame.setHeader(null);
        frame.setDuration(Long.MAX_VALUE);
        frame.setEOM(false);
        heap.offer(frame);
    }

}
