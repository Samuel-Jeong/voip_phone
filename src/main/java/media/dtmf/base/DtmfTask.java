package media.dtmf.base;

import media.dtmf.module.DtmfHandler;
import media.protocol.rtp.util.RtpUtil;
import service.base.TaskUnit;

/**
 * @class public class DtmfTask extends TaskUnit
 * @brief DtmfTask class
 */
public class DtmfTask extends TaskUnit {

    private final int digit;
    private final int volume;
    private final byte[] data;

    private int eventDuration = 0;

    ////////////////////////////////////////////////////////////////////////////////

    public DtmfTask(int digit, int volume, int interval) {
        super(interval);

        this.digit = digit;
        this.volume = volume;

        DtmfInbandTone dtmfInbandTone = DtmfHandler.convertDtmfUnitToInbandTone(digit);
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

    @Override
    public void run() {
        DtmfHandler.handle(
                digit,
                volume,
                eventDuration,
                false
        );

        eventDuration += 80;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public int getDigit() {
        return digit;
    }

    public int getVolume() {
        return volume;
    }

    public int getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(int eventDuration) {
        this.eventDuration = eventDuration;
    }

    public byte[] getData() {
        return data;
    }

}
