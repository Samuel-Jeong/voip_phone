package client.gui.model.dtmf;

import client.VoipClient;
import client.module.SoundHandler;
import client.module.base.MediaFrame;
import client.module.base.UdpReceiver;
import client.module.base.UdpSender;
import config.ConfigManager;
import media.dtmf.DtmfInbandTone;
import media.dtmf.DtmfUnit;
import media.protocol.rtp.util.RtpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AppInstance;

import javax.sound.sampled.SourceDataLine;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @class public class DtmfListener implements ActionListener
 * @brief DtmfListener class
 */
public class DtmfListener implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(DtmfListener.class);

    private final int digit;
    private final int volume;
    private final byte[] data;

    private boolean isFinished = false;
    private int eventDuration = 0;

    ////////////////////////////////////////////////////////////////////////////////

    public DtmfListener(int digit, int volume) {
        this.digit = digit;
        this.volume = volume;

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

        if (dtmfInbandTone != null) {
            this.data =  RtpUtil.shortToByte(
                    dtmfInbandTone.getAudioSamples(8000, 16),
                    true
            );
        } else {
            this.data =  RtpUtil.shortToByte(
                    DtmfInbandTone.DTMF_INBAND_1.getAudioSamples(8000, 16),
                    true
            );
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void actionPerformed(ActionEvent e) {
        // Client 만 사용 가능
        ConfigManager configManager = AppInstance.getInstance().getConfigManager();
        if (!configManager.isUseClient()) {
            return;
        }

        //DtmfSoundGenerator.getInstance().playTone(digit);
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

            if (isFinished) {
                isFinished = false;
                eventDuration = 0;
                logger.debug("[{}] FINISHED", digit);
            } else {
                eventDuration += 80;
                logger.debug("[{}] ONGOING", digit);
            }

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
}