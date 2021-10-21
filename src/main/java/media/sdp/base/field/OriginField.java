package media.sdp.base.field;

/**
 * @class public class OriginField
 * @brief OriginField class
 */
public class OriginField {

    private char originType;
    private String originUserName;
    private String originAddress;
    private String originAddressType;
    private String originNetworkType;
    private long sessionId;
    private long sessionVersion;

    public OriginField(char originType, String originUserName, String originAddress, String originAddressType, String originNetworkType, long sessionId, long sessionVersion) {
        this.originType = originType;
        this.originUserName = originUserName;
        this.originAddress = originAddress;
        this.originAddressType = originAddressType;
        this.originNetworkType = originNetworkType;
        this.sessionId = sessionId;
        this.sessionVersion = sessionVersion;
    }

    public char getOriginType() {
        return originType;
    }

    public void setOriginType(char originType) {
        this.originType = originType;
    }

    public String getOriginUserName() {
        return originUserName;
    }

    public void setOriginUserName(String originUserName) {
        this.originUserName = originUserName;
    }

    public String getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(String originAddress) {
        this.originAddress = originAddress;
    }

    public String getOriginAddressType() {
        return originAddressType;
    }

    public void setOriginAddressType(String originAddressType) {
        this.originAddressType = originAddressType;
    }

    public String getOriginNetworkType() {
        return originNetworkType;
    }

    public void setOriginNetworkType(String originNetworkType) {
        this.originNetworkType = originNetworkType;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getSessionVersion() {
        return sessionVersion;
    }

    public void setSessionVersion(long sessionVersion) {
        this.sessionVersion = sessionVersion;
    }

    @Override
    public String toString() {
        return "OriginField{" +
                "originType=" + originType +
                ", originUserName='" + originUserName + '\'' +
                ", originAddress='" + originAddress + '\'' +
                ", originAddressType='" + originAddressType + '\'' +
                ", originNetworkType='" + originNetworkType + '\'' +
                ", sessionId=" + sessionId +
                ", sessionVersion=" + sessionVersion +
                '}';
    }
}
