package client.gui.model.dtmf;

import media.module.mixing.base.ConcurrentCyclicFIFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.base.TaskUnit;

import javax.sound.sampled.*;
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

    private final ConcurrentCyclicFIFO<Clip> playBuffer = new ConcurrentCyclicFIFO<>();
    private ScheduledThreadPoolExecutor playTaskExecutor;

    private Clip toneZero;
    private Clip toneOne;
    private Clip toneTwo;
    private Clip toneThree;
    private Clip toneFour;
    private Clip toneFive;
    private Clip toneSix;
    private Clip toneSeven;
    private Clip toneEight;
    private Clip toneNine;
    private Clip toneStar;
    private Clip toneHash;

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

    public void open () {
        String curUserDir = System.getProperty("user.dir");

        try {
            toneZero = AudioSystem.getClip();
            toneZero.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/zero.au")));

            toneOne = AudioSystem.getClip();
            toneOne.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/one.au")));

            toneTwo = AudioSystem.getClip();
            toneTwo.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/two.au")));

            toneThree = AudioSystem.getClip();
            toneThree.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/three.au")));

            toneFour = AudioSystem.getClip();
            toneFour.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/four.au")));

            toneFive = AudioSystem.getClip();
            toneFive.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/five.au")));

            toneSix = AudioSystem.getClip();
            toneSix.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/six.au")));

            toneSeven = AudioSystem.getClip();
            toneSeven.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/seven.au")));

            toneEight = AudioSystem.getClip();
            toneEight.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/eight.au")));

            toneNine = AudioSystem.getClip();
            toneNine.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/nine.au")));

            toneStar = AudioSystem.getClip();
            toneStar.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/star.au")));

            toneHash = AudioSystem.getClip();
            toneHash.open(AudioSystem.getAudioInputStream(new File(curUserDir + "/src/main/resources/dtmf/hash.au")));
        } catch (Exception e) {
            logger.warn("DtmfSoundGenerator.open.Exception", e);
        } finally {
            close();
        }
    }

    public void close () {
        if (toneZero != null) { try { toneZero.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneZero, e2); } }
        if (toneOne != null) { try { toneOne.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneOne, e2); } }
        if (toneTwo != null) { try { toneTwo.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneTwo, e2); } }
        if (toneThree != null) { try { toneThree.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneThree, e2); } }
        if (toneFour != null) { try { toneFour.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneFour, e2); } }
        if (toneFive != null) { try { toneFive.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneFive, e2); } }
        if (toneSix != null) { try { toneSix.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneSix, e2); } }
        if (toneSeven != null) { try { toneSeven.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneSeven, e2); } }
        if (toneEight != null) { try { toneEight.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneEight, e2); } }
        if (toneNine != null) { try { toneNine.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneNine, e2); } }
        if (toneStar != null) { try { toneStar.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneStar, e2); } }
        if (toneHash != null) { try { toneHash.close(); } catch (Exception e2) { logger.warn("Fail to close the tone. ({})", toneHash, e2); } }
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
        switch (offset) {
            case 0:
                playBuffer.offer(toneZero);
                break;
            case 1:
                playBuffer.offer(toneOne);
                break;
            case 2:
                playBuffer.offer(toneTwo);
                break;
            case 3:
                playBuffer.offer(toneThree);
                break;
            case 4:
                playBuffer.offer(toneFour);
                break;
            case 5:
                playBuffer.offer(toneFive);
                break;
            case 6:
                playBuffer.offer(toneSix);
                break;
            case 7:
                playBuffer.offer(toneSeven);
                break;
            case 8:
                playBuffer.offer(toneEight);
                break;
            case 9:
                playBuffer.offer(toneNine);
                break;
            case 10:
                playBuffer.offer(toneStar);
                break;
            case 11:
                playBuffer.offer(toneHash);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + offset);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    private class PlayTask extends TaskUnit {

        protected PlayTask(int interval) {
            super(interval);
        }

        @Override
        public void run() {
            Clip curTone = playBuffer.poll();
            if (curTone == null) {
                return;
            }

            try {
                //curTone.loop(1);
                curTone.start();
                /*for (int i = 0; i < curTone.getFrameLength(); i++) {
                    logger.debug("...");
                }
                curTone.stop();*/

                //logger.debug("({}) ms={}, len={}", curToneName, curTone.getMicrosecondLength(), curTone.getFrameLength());
            } catch (Exception e1) {
                logger.warn("DtmfSoundGenerator.PlayTask.Exception ({})", curTone, e1);
            }
        }
    }

}
