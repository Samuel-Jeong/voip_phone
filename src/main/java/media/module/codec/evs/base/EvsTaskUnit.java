package media.module.codec.evs.base;

import media.module.mixing.base.ConcurrentCyclicFIFO;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @class public class EvsTaskUnit
 * @brief EvsTaskUnit class
 */
public class EvsTaskUnit {

    private ScheduledThreadPoolExecutor taskExecutor;
    private final ConcurrentCyclicFIFO<byte[]> inputBuffer = new ConcurrentCyclicFIFO<>();
    private ConcurrentCyclicFIFO<byte[]> outputBuffer = null;

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

    public ConcurrentCyclicFIFO<byte[]> getOutputBuffer() {
        return outputBuffer;
    }

    public void clearOutputBuffer() {
        if (outputBuffer != null) {
            outputBuffer.clear();
        }
    }

    public void setOutputBuffer(ConcurrentCyclicFIFO<byte[]> outputBuffer) {
        this.outputBuffer = outputBuffer;
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
