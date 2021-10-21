package media.sdp.base.attribute.base;

import java.util.List;

/**
 * @class public class RtpMapAttributeFactory extends AttributeFactory
 * @brief RtpMapAttributeFactory class
 */
public class RtpMapAttributeFactory extends AttributeFactory {

    public static final String AMR_WB = "AMR-WB";
    public static final String AMR_NB = "AMR";
    public static final String EVS = "EVS";
    public static final String DTMF = "telephone-event";

    String codecName = null;
    String samplingRate = null;
    boolean isOctetAlign = false;

    public RtpMapAttributeFactory(char type, String name, String value, List<String> mediaFormats) {
        super(type, name, value, mediaFormats);

        String[] spl = value.split(" ");
        if (spl.length >= 2) {
            spl = spl[1].split("/");
            codecName = spl[0];
            samplingRate = spl[1];
            if (spl.length > 2) {
                isOctetAlign = Integer.parseInt(spl[2]) == 1;
            } else {
                isOctetAlign = false;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public String getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(String samplingRate) {
        this.samplingRate = samplingRate;
    }

    public boolean isOctetAlign() {
        return isOctetAlign;
    }

    public void setOctetAlign(boolean octetAlign) {
        if (!isOctetAlign && octetAlign) {
            value += "/1";
        }

        isOctetAlign = octetAlign;
    }

}
