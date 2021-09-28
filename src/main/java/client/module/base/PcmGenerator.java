package client.module.base;

import media.MediaManager;
import media.module.mixing.base.ConcurrentCyclicFIFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;

import javax.sound.sampled.AudioInputStream;

/**
 * @class public class PcmGenerator extends TaskUnit
 * @brief PcmGenerator class
 */
public class PcmGenerator extends TaskUnit {

    private static final Logger logger = LoggerFactory.getLogger(PcmGenerator.class);

    private final int BUFFER_LENGTH; // 8k bitstream > 160, 48k (8k * 6 = 48k) bitstream > 960 (160 * 6)

    /* Audio Data input stream (using fd) */
    private final AudioInputStream stream;

    /* Mike Data Buffer */
    private final ConcurrentCyclicFIFO<byte[]> mikeBuffer;

    public PcmGenerator(ConcurrentCyclicFIFO<byte[]> mikeBuffer, AudioInputStream stream, int interval) {
        super(interval);

        BUFFER_LENGTH = MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB)? 640 : 320;

        this.stream = stream;
        this.mikeBuffer = mikeBuffer;
    }

    @Override
    public void run() {
        byte[] data = new byte[BUFFER_LENGTH]; // RtpPacket.MAX_PAYLOAD_BUFFER_SIZE not used

        try {
            if (stream.read(data) != -1) {
                mikeBuffer.offer(data);
            }
        } catch (Exception e) {
            logger.warn("PcmGenerator.run.Exception", e);
        }
    }

}
