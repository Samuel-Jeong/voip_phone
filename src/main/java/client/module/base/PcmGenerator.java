package client.module.base;

import client.VoipClient;
import config.ConfigManager;
import media.MediaManager;
import media.module.mixing.base.ConcurrentCyclicFIFO;
import media.protocol.base.ByteUtil;
import media.record.wav.WavFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;
import service.base.TaskUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * @class public class PcmGenerator extends TaskUnit
 * @brief PcmGenerator class
 */
public class PcmGenerator extends TaskUnit {

    private static final Logger logger = LoggerFactory.getLogger(PcmGenerator.class);

    private final int BUFFER_LENGTH; // 8k bitstream > 160, 48k (8k * 6 = 48k) bitstream > 960 (160 * 6)

    /* Audio Data input stream (using fd) */
    private final AudioInputStream stream;
    private AudioInputStream wavStream = null;

    /* Mike Data Buffer */
    private final ConcurrentCyclicFIFO<byte[]> mikeBuffer;

    private boolean isSendWav;

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
                /*if (wavFile.getSampleRate() == 16000) {
                    BUFFER_LENGTH = 640;
                } else {
                    BUFFER_LENGTH = 320;
                }*/

                BUFFER_LENGTH = 320;

                try {
                    wavStream = wavFile.loadWavFileToAudioInputStream();
                } catch (Exception e) {
                    logger.warn("PcmGenerator.Exception", e);
                }
            }
        } else {
            BUFFER_LENGTH = MediaManager.getInstance().getPriorityCodec().equals(MediaManager.AMR_WB) ? 640 : 320;
        }

        /*if (wavStream == null) {
            isSendWav = false;
        }*/
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void run() {
        byte[] data = new byte[BUFFER_LENGTH];

        try {
            if (isSendWav) {
                /*WavFile wavFile = VoipClient.getInstance().getWavFile();
                if (wavFile == null) {
                    return;
                }

                double[] frameData = new double[BUFFER_LENGTH / Double.BYTES];
                int readBytes = wavFile.readFrames(frameData);
                if (readBytes > 0) {
                    data = ByteUtil.convertDoubleArrayToByteArray(frameData, true);
                    if (data.length > 0) {
                        mikeBuffer.offer(data);
                        //logger.debug("{} data: {}", data.length, data);
                    }
                }*/
                if (wavStream.read(data) != -1) {
                    mikeBuffer.offer(data);
                }
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
