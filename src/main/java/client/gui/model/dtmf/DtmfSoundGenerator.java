package client.gui.model.dtmf;

import media.module.mixing.base.ConcurrentCyclicFIFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @class public class DtmfSoundGenerator
 * @brief DtmfSoundGenerator class
 */
public class DtmfSoundGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DtmfSoundGenerator.class);

    private static DtmfSoundGenerator dtmfSoundGenerator = null;

    private final ConcurrentCyclicFIFO<String> playBuffer = new ConcurrentCyclicFIFO<>();
    private ScheduledThreadPoolExecutor playTaskExecutor;

    ////////////////////////////////////////////////////////////////////////////////

    public DtmfSoundGenerator() {
        // Nothing
    }

    public static DtmfSoundGenerator getInstance () {
        if (dtmfSoundGenerator == null) {
            dtmfSoundGenerator = new DtmfSoundGenerator();

        }
        return dtmfSoundGenerator;
    }

    public void start () {
        try {
            if (playTaskExecutor == null) {
                playTaskExecutor = new ScheduledThreadPoolExecutor(5);

                PlayTask playTask = new PlayTask(
                        10
                );

                playTaskExecutor.scheduleAtFixedRate(
                        playTask,
                        playTask.getInterval(),
                        playTask.getInterval(),
                        TimeUnit.MILLISECONDS
                );

                logger.debug("DtmfSoundGenerator PlayTask is added.");
            }
        } catch (Exception e) {
            logger.warn("DtmfSoundGenerator.Exception", e);
        }
    }

    public void stop () {
        if (playTaskExecutor != null) {
            playTaskExecutor.shutdown();
            playTaskExecutor = null;
            logger.debug("DtmfSoundGenerator PlayTask is removed.");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void playTone (int offset) {
        String curToneName;

        switch (offset) {
            case 0:
                curToneName = "zero";
                break;
            case 1:
                curToneName = "one";
                break;
            case 2:
                curToneName = "two";
                break;
            case 3:
                curToneName = "three";
                break;
            case 4:
                curToneName = "four";
                break;
            case 5:
                curToneName = "five";
                break;
            case 6:
                curToneName = "six";
                break;
            case 7:
                curToneName = "seven";
                break;
            case 8:
                curToneName = "eight";
                break;
            case 9:
                curToneName = "nine";
                break;
            case 10:
                curToneName = "star";
                break;
            case 11:
                curToneName = "hash";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + offset);
        }

        playBuffer.offer(curToneName);
    }

    ////////////////////////////////////////////////////////////////////////////////

    private class PlayTask extends TaskUnit {

        protected PlayTask(int interval) {
            super(interval);
        }

        @Override
        public void run() {
            String curToneName = playBuffer.poll();
            if (curToneName == null) {
                return;
            }

            Clip curTone = null;
            try {
                String curUserDir = System.getProperty("user.dir");

                AudioInputStream toneInputStream = AudioSystem.getAudioInputStream(
                        new BufferedInputStream(
                                new FileInputStream(
                                        curUserDir +
                                                "/src/main/resources/dtmf/" + curToneName + ".au"
                                )
                        )
                );

                AudioFormat format = toneInputStream.getFormat();
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                curTone = (Clip) AudioSystem.getLine(info);

                //curTone = AudioSystem.getClip();
                curTone.open(toneInputStream);

                logger.debug("Current Tone : {}", curToneName);
                curTone.start();
                /*while (curTone.isRunning()) {

                }
                curTone.stop();*/
            } catch (Exception e1) {
                logger.warn("DtmfSoundGenerator.PlayTask.Exception (toneName={})", curToneName, e1);
            } finally {
                if (curTone != null) {
                    try {
                        curTone.close();
                    } catch (Exception e2) {
                        logger.warn("Fail to close the tone. (toneName={})", curToneName, e2);
                    }}

            }
        }
    }

}
