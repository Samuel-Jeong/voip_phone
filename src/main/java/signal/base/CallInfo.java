package signal.base;

import media.sdp.base.SdpUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.LogFormatter;

import javax.sip.ServerTransaction;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @class public class CallInfo
 * @brief CallInfo Class
 */
public class CallInfo {

    private static final Logger logger = LoggerFactory.getLogger(CallInfo.class);

    private final String sessionId;

    public static final int MAX_SEQ_NUM = 65535;
    public static final int MAX_RANDOM_SEQ_NUM = 1000;
    public static final int MAX_RANDOM_TIME_STAMP = 100000;
    public static final int MAX_RANDOM_SSRC = 100000;

    private final long createdTime = System.currentTimeMillis();

    private CallInfo remoteCallInfo = null;

    private final String callId;
    private final String fromNo;
    private final String toNo;

    private final String fromSipIp;
    private final int fromSipPort;
    private final String toSipIp;
    private final int toSipPort;

    private SdpUnit sdpUnit;

    private CallIdHeader callIdHeader = null;
    private ViaHeader firstViaHeader = null;
    private Request inviteRequest = null;
    private ServerTransaction inviteServerTransaction = null;

    private final AtomicBoolean isInviteUnauthorized = new AtomicBoolean(true);
    private final AtomicBoolean isInviteAccepted = new AtomicBoolean(false);
    private final AtomicBoolean isCallStarted = new AtomicBoolean(false);
    private final AtomicBoolean isCallRecv = new AtomicBoolean(false);
    private final AtomicBoolean isCallCanceled = new AtomicBoolean(false);

    private final AtomicBoolean isRoomEntered = new AtomicBoolean(false);

    private int audioSeqNum;
    private long audioTimestamp;
    private final long audioSsrc;

    private int dtmfSeqNum;
    private long dtmfTimestamp;
    private final long dtmfSsrc;

    private final Random random = new Random();

    private String callCancelHandlerId = null;

    ////////////////////////////////////////////////////////////////////////////////

    public CallInfo(String sessionId, String callId, String fromNo, String fromSipIp, int fromSipPort, String toNo, String toSipIp, int toSipPort) {
        this.sessionId = sessionId;

        this.callId = callId.trim();
        this.fromNo = fromNo;
        this.toNo = toNo;

        this.fromSipIp = fromSipIp.trim();
        this.fromSipPort = fromSipPort;
        this.toSipIp = toSipIp.trim();
        this.toSipPort = toSipPort;

        audioSeqNum = random.nextInt(MAX_RANDOM_SEQ_NUM);
        audioTimestamp = random.nextInt(MAX_RANDOM_TIME_STAMP);
        audioSsrc = random.nextInt(MAX_RANDOM_SSRC);

        dtmfSeqNum = random.nextInt(MAX_RANDOM_SEQ_NUM);
        dtmfTimestamp = random.nextInt(MAX_RANDOM_TIME_STAMP);
        dtmfSsrc = random.nextInt(MAX_RANDOM_SSRC);

        logger.debug("{} CallInfo(callId={}, ip={}, port={}) is created.",
                LogFormatter.getCallLogHeader(sessionId, callId, fromNo, toNo), this.callId, this.toSipIp, this.toSipPort
        );
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getSessionId() {
        return sessionId;
    }

    public int getAudioSeqNum() {
        return audioSeqNum;
    }

    public void setAudioSeqNum(int audioSeqNum) {
        this.audioSeqNum = audioSeqNum;
    }

    public void initAudioSeqNum() {
        this.audioSeqNum = random.nextInt(MAX_RANDOM_SEQ_NUM);
    }

    public long getAudioTimestamp() {
        return audioTimestamp;
    }

    public void setAudioTimestamp(long audioTimestamp) {
        this.audioTimestamp = audioTimestamp;
    }

    public void initAudioTimestamp() {
        this.audioTimestamp = random.nextInt(MAX_RANDOM_TIME_STAMP);
    }

    public long getAudioSsrc() {
        return audioSsrc;
    }

    public int getDtmfSeqNum() {
        return dtmfSeqNum;
    }

    public void setDtmfSeqNum(int dtmfSeqNum) {
        this.dtmfSeqNum = dtmfSeqNum;
    }

    public void initDtmfSeqNum() {
        this.dtmfSeqNum = random.nextInt(MAX_RANDOM_SEQ_NUM);
    }

    public long getDtmfTimestamp() {
        return dtmfTimestamp;
    }

    public void setDtmfTimestamp(long dtmfTimestamp) {
        this.dtmfTimestamp = dtmfTimestamp;
    }

    public void initDtmfTimestamp() {
        this.dtmfTimestamp = random.nextInt(MAX_RANDOM_TIME_STAMP);
    }

    public long getDtmfSsrc() {
        return dtmfSsrc;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public CallInfo getRemoteCallInfo() {
        return remoteCallInfo;
    }

    public void setRemoteCallInfo(CallInfo remoteCallInfo) {
        this.remoteCallInfo = remoteCallInfo;
        if (remoteCallInfo != null) {
            logger.debug("{} Success to set the remote call info. (remoteCallId={})",
                    LogFormatter.getCallLogHeader(sessionId, callId, fromNo, toNo), remoteCallInfo.getCallId());
        } else {
            logger.debug("{} Remote call info is removed.", LogFormatter.getCallLogHeader(sessionId, callId, fromNo, toNo));
        }
    }

    public String getCallId() {
        return callId;
    }

    public String getFromNo() {
        return fromNo;
    }

    public String getToNo() {
        return toNo;
    }

    public String getFromSipIp() {
        return fromSipIp;
    }

    public int getFromSipPort() {
        return fromSipPort;
    }

    public String getToSipIp() {
        return toSipIp;
    }

    public int getToSipPort() {
        return toSipPort;
    }

    public SdpUnit getSdpUnit() {
        return sdpUnit;
    }

    public void setSdpUnit(SdpUnit sdpUnit) {
        this.sdpUnit = sdpUnit;
    }

    public CallIdHeader getCallIdHeader() {
        return callIdHeader;
    }

    public void setCallIdHeader(CallIdHeader callIdHeader) {
        this.callIdHeader = callIdHeader;
    }

    public ViaHeader getFirstViaHeader() {
        return firstViaHeader;
    }

    public void setFirstViaHeader(ViaHeader firstViaHeader) {
        this.firstViaHeader = firstViaHeader;
    }

    public Request getInviteRequest() {
        return inviteRequest;
    }

    public void setInviteRequest(Request inviteRequest) {
        this.inviteRequest = inviteRequest;
    }

    public ServerTransaction getInviteServerTransaction() {
        return inviteServerTransaction;
    }

    public void setInviteServerTransaction(ServerTransaction inviteServerTransaction) {
        this.inviteServerTransaction = inviteServerTransaction;
    }

    public boolean getIsInviteUnauthorized() {
        return isInviteUnauthorized.get();
    }

    public void setIsInviteUnauthorized(boolean isInviteUnauthorized) {
        this.isInviteUnauthorized.set(isInviteUnauthorized);
    }

    public boolean getIsInviteAccepted() {
        return isInviteAccepted.get();
    }

    public void setIsInviteAccepted(boolean isInviteAccepted) {
        this.isInviteAccepted.set(isInviteAccepted);
    }

    public boolean getIsCallStarted() {
        return isCallStarted.get();
    }

    public void setIsCallStarted(boolean isCallStarted) {
        this.isCallStarted.set(isCallStarted);
    }

    public boolean getIsCallRecv() {
        return isCallRecv.get();
    }

    public void setIsCallRecv(boolean isCallRecv) {
        this.isCallRecv.set(isCallRecv);
    }

    public boolean getIsCallCanceled() {
        return isCallCanceled.get();
    }

    public void setIsCallCanceled(boolean isCallCanceled) {
        this.isCallCanceled.set(isCallCanceled);
    }

    public boolean getIsRoomEntered() {
        return isRoomEntered.get();
    }

    public void setIsRoomEntered(boolean isRoomEntered) {
        this.isRoomEntered.set(isRoomEntered);
    }

    public String getCallCancelHandlerId() {
        return callCancelHandlerId;
    }

    public void setCallCancelHandlerId(String callCancelHandlerId) {
        this.callCancelHandlerId = callCancelHandlerId;
    }

    ////////////////////////////////////////////////////////////////////////////////


    @Override
    public String toString() {
        return "CallInfo{" +
                "sessionId='" + sessionId + '\'' +
                ", createdTime=" + createdTime +
                ", remoteCallInfo=" + remoteCallInfo +
                ", callId='" + callId + '\'' +
                ", fromNo='" + fromNo + '\'' +
                ", toNo='" + toNo + '\'' +
                ", fromSipIp='" + fromSipIp + '\'' +
                ", fromSipPort=" + fromSipPort +
                ", toSipIp='" + toSipIp + '\'' +
                ", toSipPort=" + toSipPort +
                ", sdpUnit=" + sdpUnit +
                ", callIdHeader=" + callIdHeader +
                '}';
    }
}
