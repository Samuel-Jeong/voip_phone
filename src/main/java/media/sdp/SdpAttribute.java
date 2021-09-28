package media.sdp;

/**
 * @class public class SdpAttribute
 * @brief SdpAttribute class
 */
public class SdpAttribute {

    public static final String NAME_RTPMAP = "rtpmap";
    public static final String NAME_FMTP = "fmtp";

    public static final String NAME_SENDRECV = "sendrecv";
    public static final String NAME_SENDONLY = "sendonly";

    private String name = null;
    private String payloadId = null;
    private String description;
    private String codec = null;
    private String sampleRate = null;

    ////////////////////////////////////////////////////////////////////////////////

    public SdpAttribute ( ) {
        // Do nothing
    }

    public SdpAttribute (SdpAttribute sdpAttribute) {
        this.name = sdpAttribute.name;
        this.payloadId = sdpAttribute.payloadId;
        setDescription(sdpAttribute.description);
    }

    public SdpAttribute (String description) {
        setDescription(description);
    }

    public SdpAttribute (String name, String description) {
        this.name = name;
        setDescription(description);
    }

    public SdpAttribute (String name, String payloadId, String description) {
        this.name = name;
        this.payloadId = payloadId;
        setDescription(description);
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getName ( ) {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getPayloadId ( ) {
        return payloadId;
    }

    public void setPayloadId (String payloadId) {
        this.payloadId = payloadId;
    }

    public String getCodec ( ) {
        return codec;
    }

    public void setCodec (String codec) {
        this.codec = codec;
    }

    public String getSampleRate ( ) {
        return sampleRate;
    }

    public void setSampleRate (String sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getDescription ( ) {
        return description;
    }

    public void setDescription (String description) {
        this.description = description;
        if (description != null && description.contains("/")) {
            this.codec = description.substring(0, description.indexOf('/')).trim();
            String sampleRateStr = description.substring(description.indexOf('/') + 1).trim();
            if (sampleRateStr.contains("/")) {
                sampleRate = sampleRateStr.substring(0, sampleRateStr.indexOf('/')).trim();
            } else {
                sampleRate = sampleRateStr;
            }
        }
    }

    @Override
    public String toString ( ) {
        return "SdpAttribute{" +
                "name='" + name + '\'' +
                ", payloadId='" + payloadId + '\'' +
                ", description='" + description + '\'' +
                ", codec='" + codec + '\'' +
                ", sampleRate=" + sampleRate +
                '}';
    }

}
