package media.module.codec.evs.base;

import client.module.base.MediaFrame;
import media.module.mixing.base.ConcurrentCyclicFIFO;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @class public class EvsTaskUnit
 * @brief EvsTaskUnit class
 */
public class EvsTaskUnit {

    private ScheduledThreadPoolExecutor taskExecutor;
    private final ConcurrentCyclicFIFO<byte[]> inputBuffer = new ConcurrentCyclicFIFO<>();
    private ConcurrentCyclicFIFO<byte[]> outputByteBuffer = null;
    private ConcurrentCyclicFIFO<MediaFrame> outputMediaFrameBuffer = null;

    private final int mergeCount;

    ////////////////////////////////////////////////////////////////////////////////

    public EvsTaskUnit(int mergeCount) {
        this.mergeCount = mergeCount;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public ScheduledThreadPoolExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(ScheduledThreadPoolExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public ConcurrentCyclicFIFO<byte[]> getInputBuffer() {
        return inputBuffer;
    }

    public void clearInputBuffer() {
        inputBuffer.clear();
    }

    public ConcurrentCyclicFIFO<byte[]> getOutputByteBuffer() {
        return outputByteBuffer;
    }

    public ConcurrentCyclicFIFO<MediaFrame> getOutputMediaFrameBuffer() {
        return outputMediaFrameBuffer;
    }

    public void clearOutputByteBuffer() {
        if (outputByteBuffer != null) {
            outputByteBuffer.clear();
        }
    }

    public void clearOutputMediaFrameBuffer() {
        if (outputMediaFrameBuffer != null) {
            outputMediaFrameBuffer.clear();
        }
    }

    public void setOutputByteBuffer(ConcurrentCyclicFIFO<byte[]> outputBuffer) {
        this.outputByteBuffer = outputBuffer;
    }

    public void setOutputMediaFrameBuffer(ConcurrentCyclicFIFO<MediaFrame> outputBuffer) {
        this.outputMediaFrameBuffer = outputBuffer;
    }

    public int getMergeCount() {
        return mergeCount;
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "EvsTaskUnit{" +
                "mergeCount=" + mergeCount +
                '}';
    }
}
