package client.module.base;

import client.VoipClient;
import config.ConfigManager;
import media.MediaManager;
import media.module.mixing.base.ConcurrentCyclicFIFO;
import media.record.wav.WavFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
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

    private boolean isSendWav;
    private int wavDataOffset = 0;

    ////////////////////////////////////////////////////////////////////////////////

    public PcmGenerator(ConcurrentCyclicFIFO<byte[]> mikeBuffer, AudioInputStream stream, int interval) {
        super(interval);

        this.stream = stream;
        this.mikeBuffer = mikeBuffer;

        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        this.isSendWav = configManager.isSendWav();

        if (isSendWav) {
            WavFile wavFile = VoipClient.getInstance().getWavFile();
            if (wavFile == null) {
                BUFFER_LENGTH = MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB) ? 640 : 320;
                isSendWav = false;
            } else {
                if (wavFile.getSampleRate() == 16000) {
                    BUFFER_LENGTH = 640;
                } else {
                    BUFFER_LENGTH = 320;
                }
            }
        } else {
            BUFFER_LENGTH = MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB) ? 640 : 320;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void run() {
        try {
            if (isSendWav) {
                WavFile wavFile = VoipClient.getInstance().getWavFile();
                if (wavFile == null) {
                    return;
                }

                /*double[] frameData = new double[BUFFER_LENGTH / Double.BYTES];
                int readBytes = wavFile.readFrames(frameData);
                if (readBytes > 0) {
                    //logger.debug("READ: {}, [{}]", readBytes, frameData);
                    byte[] data = ByteUtil.convertDoubleArrayToByteArray(frameData);
                    if (data.length > 0) {
                        //logger.debug("SEND: {}, [{}]", data.length, data);
                        mikeBuffer.offer(data);
                    }
                }*/

                /*byte[] data = wavFile.convertWavToRawAll(
                        wavFile.audioToByte()
                );*/

                byte[] data = wavFile.audioToBytePartially(
                        wavDataOffset,
                        wavDataOffset + BUFFER_LENGTH
                );

                if (data != null && data.length > 0) {
                    mikeBuffer.offer(data);
                }
            } else {
                byte[] data = new byte[BUFFER_LENGTH];
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
