package media.module.mixing.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @class public class AudioBuffer
 * @brief AudioBuffer class
 */
public class AudioBuffer {

    private static final Logger logger = LoggerFactory.getLogger(AudioBuffer.class);

    /* Concurrent Cyclic FIFO Buffer */
    private final ConcurrentCyclicFIFO<AudioFrame> buffer = new ConcurrentCyclicFIFO<>();
    /* AudioBuffer key > Call-ID */
    private final String id;

    ////////////////////////////////////////////////////////////////////////////////

    public AudioBuffer(String id) {
        this.id = id;

        logger.debug("AudioBuffer is created. (id={})", id);
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getId() {
        return id;
    }

    public AudioFrame evolve() {
        return buffer.poll();
    }

    public void resetBuffer() {
        buffer.clear();
    }

    public void offer(AudioFrame audioFrame) {
        buffer.offer(audioFrame);
    }

}
