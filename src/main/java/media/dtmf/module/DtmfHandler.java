package media.dtmf.module;

import client.VoipClient;
import client.gui.model.dtmf.DtmfSoundGenerator;
import client.module.SoundHandler;
import client.module.base.MediaFrame;
import client.module.base.UdpSender;
import config.ConfigManager;
import media.dtmf.base.DtmfInbandTone;
import media.dtmf.base.DtmfUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;

/**
 * @class public class DtmfHandler
 * @brief DtmfHandler class
 */
public class DtmfHandler {

    private static final Logger logger = LoggerFactory.getLogger(DtmfHandler.class);

    public static void handle (int digit, int volume, int eventDuration, boolean isFinished) {
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (!configManager.isUseClient()) {
            return;
        }

        if (VoipClient.getInstance().isStarted()) {
            SoundHandler soundHandler = VoipClient.getInstance().getSoundHandler();
            if (soundHandler == null) {
                return;
            }

            // 상대방한테 DTMF 오디오 데이터 송출
            UdpSender udpSender = soundHandler.getUdpSender();
            if (udpSender != null) {
                DtmfUnit dtmfUnit = new DtmfUnit(digit, isFinished, false, volume, eventDuration);
                udpSender.getSendBuffer().offer(
                        new MediaFrame(
                                true,
                                dtmfUnit.getData()
                        )
                );
                logger.debug("Send DTMF UNIT: {}", dtmfUnit);
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
