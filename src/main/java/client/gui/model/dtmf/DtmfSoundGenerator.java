package client.gui.model.dtmf;

import media.module.mixing.base.ConcurrentCyclicFIFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;
import system.SystemManager;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @class public class DtmfSoundGenerator
 * @brief DtmfSoundGenerator class
 */
public class DtmfSoundGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DtmfSoundGenerator.class);

    private static DtmfSoundGenerator dtmfSoundGenerator = null;

    private final ConcurrentCyclicFIFO<String> playBuffer = new ConcurrentCyclicFIFO<>();
    private ScheduledThreadPoolExecutor playTaskExecutor;

    private int mode = 0;

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

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void start () {
        try {
            if (playTaskExecutor == null) {
                playTaskExecutor = new ScheduledThreadPoolExecutor(10);

                PlayTask playTask = new PlayTask(
                        5
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

            Clip curTone;
            try {
                String curUserDir = System.getProperty("user.dir");

                if (mode == 0) {
                    curUserDir += "/src/main/resources/dtmf/";
                } else if (mode == 1) {
                    if (curUserDir.endsWith("bin")) {
                        curUserDir = curUserDir.substring(0, curUserDir.lastIndexOf("bin") - 1);
                    }

                    if (SystemManager.getInstance().getOs().equals("win")) {
                        curUserDir += "\\resources\\dtmf\\";
                    } else {
                        curUserDir += "/resources/dtmf/";
                    }
                }

                curTone = AudioSystem.getClip();
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                        new File(
                                curUserDir + curToneName + ".au"
                        )
                );

                byte[] data = new byte[(int) audioInputStream.getFrameLength()];
                if (audioInputStream.read(data) > 0) {
                    logger.debug("DTMF[{}] byte data: {}", curToneName, data);
                }

                curTone.open(audioInputStream);

                //curTone.loop(1);
                curTone.start();
                /*for (int i = 0; i < 10; i++) {
                    logger.debug("...");
                }
                curTone.stop();*/

                //logger.debug("({}) ms={}, len={}", curToneName, curTone.getMicrosecondLength(), curTone.getFrameLength());
            } catch (Exception e1) {
                logger.warn("DtmfSoundGenerator.PlayTask.Exception (toneName={})", curToneName, e1);
            }
            // close 코드있으면 재생이 안됨...
            /*finally {
                if (curTone != null) {
                    try {
                        curTone.close();
                    } catch (Exception e2) {
                        logger.warn("Fail to close the tone. (toneName={})", curToneName, e2);
                    }
                }
            }*/
        }
    }

}
