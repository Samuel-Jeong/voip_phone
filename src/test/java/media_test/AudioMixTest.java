package media_test;

import config.ConfigManager;
import media.module.mixing.AudioMixManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @class public class AudioMixTest
 * @brief AudioMixTest class
 */
public class AudioMixTest {

    private static final Logger logger = LoggerFactory.getLogger(AudioMixTest.class);

    public void testStart ( ) {
        mixTest();
    }

    ////////////////////////////////////////////////////////////////////////////////

    private void mixTest( ) {
        try {
            ConfigManager configManager = AppInstance.getInstance().getConfigManager();
            String sessionId = "session1";
            String callId = "call1";

            byte[] pcm1Data = Files.readAllBytes(Paths.get(configManager.getRecordPath() + "\\01012345678_01056781234_2021072216_send.wav"));
            byte[] pcm2Data = Files.readAllBytes(Paths.get(configManager.getRecordPath() + "\\01056781234_01012345678_2021072216_send.wav"));

            /*AudioMixManager.getInstance().addAudioMixer(
                    sessionId,
                    configManager.getRecordPath() + "\\V_" + sessionId + "_mix.wav",
                    8000,
                    16
            );*/

            //AudioMixManager.getInstance().mix(sessionId, pcm1Data);
            //AudioMixManager.getInstance().mix(sessionId, pcm2Data);

            AudioMixManager.getInstance().removeAudioMixer(
                    sessionId,
                    callId
            );
        } catch (Exception e) {
            logger.warn("Fail to mix.", e);
        }
    }

}
