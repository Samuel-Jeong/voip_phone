package media.dtmf.module;

import client.VoipClient;
import client.module.SoundHandler;
import client.module.base.MediaFrame;
import client.module.base.UdpSender;
import config.ConfigManager;
import media.dtmf.base.DtmfInbandTone;
import media.dtmf.base.DtmfUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;

import javax.sound.sampled.SourceDataLine;

/**
 * @class public class DtmfHandler
 * @brief DtmfHandler class
 */
public class DtmfHandler {

    private static final Logger logger = LoggerFactory.getLogger(DtmfHandler.class);

    public static void handle (int digit, int volume, int eventDuration, boolean isFinished, byte[] data) {
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (!configManager.isUseClient()) {
            return;
        }

        // 1) 내 스피커로 DTMF 오디오 데이터 송출
        SourceDataLine sourceDataLine = VoipClient.getInstance().getSourceLine();
        if (sourceDataLine != null) {
            sourceDataLine.write(data, 0, data.length);
        }

        if (VoipClient.getInstance().isStarted()) {
            SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
            if (soundHandler == null) {
                return;
            }

            DtmfUnit dtmfUnit = new DtmfUnit(
                    digit,
                    isFinished,
                    false,
                    volume,
                    eventDuration
            );
            logger.debug("DTMF UNIT: {}", dtmfUnit);

            // 2) 상대방한테 DTMF 오디오 데이터 송출
            UdpSender udpSender = soundHandler.getUdpSender();
            if (udpSender != null) {
                udpSender.getSendBuffer().offer(
                        new MediaFrame(
                                true,
                                dtmfUnit.getData()
                        )
                );
            }
        }
    }

    public static DtmfInbandTone convertDtmfUnitToInbandTone(int digit) {
        DtmfInbandTone dtmfInbandTone;
        switch (digit) {
            case 0: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_0; break;
            case 1: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_1; break;
            case 2: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_2; break;
            case 3: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_3; break;
            case 4: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_4; break;
            case 5: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_5; break;
            case 6: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_6; break;
            case 7: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_7; break;
            case 8: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_8; break;
            case 9: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_9; break;
            case 10: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_STAR; break;
            case 11: dtmfInbandTone = DtmfInbandTone.DTMF_INBAND_SHARP; break;
            default:
                dtmfInbandTone = null;
        }

        return dtmfInbandTone;
    }

}
