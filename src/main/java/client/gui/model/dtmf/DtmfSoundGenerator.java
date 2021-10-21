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

    private int mode = 0;
    private Clip clip;

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

    ////////////////////////////////////////////////////////////////////////////////

    public void play(int digit) {
        String curToneName = getDtmfNumberStringFromDigit(digit);

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

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(curUserDir + curToneName + ".au"));
            clip = AudioSystem.getClip();
            if (clip != null) {
                clip.open(audioInputStream);

                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            }
        } catch (Exception e) {
            logger.warn("DtmfSoundGenerator.PlayTask.Exception (toneName={})", curToneName, e);
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getDtmfNumberStringFromDigit(int digit) {
        String curToneName;

        switch (digit) {
            case 0: curToneName = "zero"; break;
            case 1: curToneName = "one"; break;
            case 2: curToneName = "two"; break;
            case 3: curToneName = "three"; break;
            case 4: curToneName = "four"; break;
            case 5: curToneName = "five"; break;
            case 6: curToneName = "six"; break;
            case 7: curToneName = "seven"; break;
            case 8: curToneName = "eight"; break;
            case 9: curToneName = "nine"; break;
            case 10: curToneName = "star"; break;
            case 11: curToneName = "hash"; break;
            default: throw new IllegalStateException("Unexpected value: " + digit);
        }

        return curToneName;
    }

}
