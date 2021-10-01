package client.module.base;

import client.VoipClient;
import config.ConfigManager;
import media.MediaManager;
import media.module.mixing.base.ConcurrentCyclicFIFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import service.base.TaskUnit;

import javax.sound.sampled.AudioInputStream;
import java.io.File;

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

    private boolean isSendWav;

    public PcmGenerator(ConcurrentCyclicFIFO<byte[]> mikeBuffer, AudioInputStream stream, int interval) {
        super(interval);

        this.stream = stream;
        this.mikeBuffer = mikeBuffer;

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        this.isSendWav = configManager.isSendWav();

        if (isSendWav) {
            File wavFile = VoipClient.getInstance().getSendWavFile();
            if (wavFile == null) {
                BUFFER_LENGTH = MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB) ? 640 : 320;
                isSendWav = false;
            } else {
                BUFFER_LENGTH = 320;
            }
        } else {
            BUFFER_LENGTH = MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB) ? 640 : 320;
        }
    }

    @Override
    public void run() {
        byte[] data = new byte[BUFFER_LENGTH]; // RtpPacket.MAX_PAYLOAD_BUFFER_SIZE not used

        try {
            if (isSendWav) {
                // TODO


                mikeBuffer.offer(data);
            } else {
                if (stream.read(data) != -1) {
                    mikeBuffer.offer(data);
                }
            }
        }
        catch (Exception e) {
            logger.warn("PcmGenerator.run.Exception", e);
        }
    }

}
