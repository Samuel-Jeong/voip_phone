package media.sdp.base;

import signal.module.NonceGenerator;

import java.util.HashMap;

/**
 * @class public class SdpUnit
 * @brief SdpUnit class
 */
public class SdpUnit {

    /* Unique id */
    private final String id = NonceGenerator.createRandomNonce();

    /* Call-ID */
    private String callId = null;
    /* Destination IP */
    private String remoteIp = null;
    /* Destination Port */
    private int remotePort = 0;

    /* SDP Info Map */
    /* Key: MediaType(Audio or video), value: SdpInfo */
    HashMap<String, SdpInfo> sdpInfoMap = new HashMap<>();

    /////////////////////////////////////////////////////////////////////

    public SdpUnit() {
        // Nothing
    }

    /////////////////////////////////////////////////////////////////////

    public String getId() {
        return id;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public void addSdpInfo (String mediaType, SdpInfo sdpInfo) {
        if (sdpInfo == null || sdpInfoMap.get(mediaType) != null) {
            return;
        }

        sdpInfoMap.putIfAbsent(mediaType, sdpInfo);
    }

    public SdpInfo getSdpInfo (String mediaType) {
        return sdpInfoMap.get(mediaType);
    }

    /////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "SdpUnit{" +
                "callId='" + callId + '\'' +
                ", remoteIp='" + remoteIp + '\'' +
                ", remotePort=" + remotePort +
                ", sdpInfoMap=" + sdpInfoMap +
                '}';
    }
}
