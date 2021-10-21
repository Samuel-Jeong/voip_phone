package media.sdp.base.field;

/**
 * @class public class BandwidthField
 * @brief BandwidthField class
 */
public class BandwidthField {

    char bandwidthType;
    String modifier;
    int value;

    public BandwidthField(char bandwidthType, String modifier, int value) {
        this.bandwidthType = bandwidthType;
        this.modifier = modifier;
        this.value = value;
    }

    public char getBandwidthType() {
        return bandwidthType;
    }

    public void setBandwidthType(char bandwidthType) {
        this.bandwidthType = bandwidthType;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BandwidthField{" +
                "bandwidthType=" + bandwidthType +
                ", modifier='" + modifier + '\'' +
                ", value=" + value +
                '}';
    }
}
