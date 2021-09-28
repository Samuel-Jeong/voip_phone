package signal.module;

import media.sdp.base.SdpUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.LogFormatter;
import signal.base.CallInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class public class CallManager
 * @brief CallManager
 */
public class CallManager {

    private static final Logger logger = LoggerFactory.getLogger(CallManager.class);

    private static CallManager callManager = null;

    private final HashMap<String, CallInfo> callMap = new HashMap<>();
    private final ReentrantLock callMapLock = new ReentrantLock();

    ////////////////////////////////////////////////////////////////////////////////

    public CallManager() {
        // Nothing
    }

    public static CallManager getInstance () {
        if (callManager == null) {
            callManager = new CallManager();
        }
        return callManager;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public int getCallMapSize() {
        try {
            callMapLock.lock();

            return callMap.size();
        } catch (Exception e) {
            logger.warn("{} Fail to get the call map size.", LogFormatter.getCallLogHeader(null,null, null, null), e);
            return 0;
        } finally {
            callMapLock.unlock();
        }
    }

    public CallInfo addCallInfo(String sessionId, String callId, String fromNo, String fromSipIp, int fromSipPort, String toNo, String toSipIp, int toSipPort) {
        if (sessionId == null || callId == null || fromNo == null || fromSipIp == null || fromSipPort <= 0 || toNo == null || toSipIp == null || toSipPort <= 0) {
            logger.warn("{} Fail to add the call info. (sessionId={}, callId={}, fromNo={}, toNo={}, fromSipIp={}, fromSipPort={}, toSipIp={}, toSipPort={})",
                    LogFormatter.getCallLogHeader(sessionId, callId, fromNo, toNo), sessionId, callId, fromNo, fromSipIp, fromSipPort, toNo, toSipIp, toSipPort
            );
            return null;
        }

        try {
            callMapLock.lock();

            callMap.putIfAbsent(callId, new CallInfo(sessionId, callId, fromNo, fromSipIp, fromSipPort, toNo, toSipIp, toSipPort));
            return callMap.get(callId);
        } catch (Exception e) {
            logger.warn("{} Fail to add the call info.", LogFormatter.getCallLogHeader(sessionId, callId, fromNo, toNo), e);
            return null;
        } finally {
            callMapLock.unlock();
        }
    }

    public CallInfo deleteCallInfo(String callId) {
        if (callId == null) { return null; }

        try {
            callMapLock.lock();

            return callMap.remove(callId);
        } catch (Exception e) {
            logger.warn("{} Fail to delete the call map.", LogFormatter.getCallLogHeader(null, callId, null, null), e);
            return null;
        } finally {
            callMapLock.unlock();
        }
    }

    public Map<String, CallInfo> getCloneCallMap( ) {
        HashMap<String, CallInfo> cloneMap;

        try {
            callMapLock.lock();

            cloneMap = (HashMap<String, CallInfo>) callMap.clone();
        } catch (Exception e) {
            logger.warn("{} Fail to clone the call map.", LogFormatter.getCallLogHeader(null,null, null, null), e);
            cloneMap = callMap;
        } finally {
            callMapLock.unlock();
        }

        return cloneMap;
    }

    public CallInfo getCallInfo(String callId) {
        if (callId == null) { return null; }

        try {
            callMapLock.lock();

            return callMap.get(callId);
        } catch (Exception e) {
            logger.warn("{} Fail to get the call info.", LogFormatter.getCallLogHeader(null, callId, null, null), e);
            return null;
        } finally {
            callMapLock.unlock();
        }
    }

    public void clearCallInfoMap() {
        try {
            callMapLock.lock();

            callMap.clear();
            logger.debug("Success to clear the call map.");
        } catch (Exception e) {
            logger.warn("{} Fail to clear the call map.", LogFormatter.getCallLogHeader(null, null, null, null), e);
        } finally {
            callMapLock.unlock();
        }
    }

    public CallInfo findOtherCallInfo(String sessionId, String curCallId) {
        try {
            callMapLock.lock();

            for (Map.Entry<String, CallInfo> entry : callMap.entrySet()) {
                CallInfo callInfo = entry.getValue();
                if (callInfo != null) {
                    if (callInfo.getSessionId().equals(sessionId) && !callInfo.getCallId().equals(curCallId)) {
                        return callInfo;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("{} Fail to get the other call info.", LogFormatter.getCallLogHeader(sessionId, curCallId, null, null), e);
            return null;
        } finally {
            callMapLock.unlock();
        }

        return null;
    }

    public CallInfo findCallInfoByMediaAddress(String ip, int port) {
        try {
            callMapLock.lock();

            for (Map.Entry<String, CallInfo> entry : callMap.entrySet()) {
                CallInfo callInfo = entry.getValue();
                if (callInfo != null) {
                    SdpUnit sdpUnit = callInfo.getSdpUnit();
                    if (sdpUnit == null) { continue; }
                    if (sdpUnit.getRemoteIp().equals(ip) && sdpUnit.getRemotePort() == port) {
                        return callInfo;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("{} Fail to get the call info by ip and port. (ip={}, port={})", LogFormatter.getCallLogHeader(null, null, null, null), ip, port, e);
            return null;
        } finally {
            callMapLock.unlock();
        }

        return null;
    }

    public CallInfo findCallInfoByFromMdn(String mdn) {
        try {
            callMapLock.lock();

            for (Map.Entry<String, CallInfo> entry : callMap.entrySet()) {
                CallInfo callInfo = entry.getValue();
                if (callInfo != null) {
                    if (callInfo.getFromNo().equals(mdn)) {
                        return callInfo;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("{} Fail to get the call info by from mdn.", LogFormatter.getCallLogHeader(null, null, mdn, null), e);
            return null;
        } finally {
            callMapLock.unlock();
        }

        return null;
    }

    public CallInfo findCallInfoByToMdn(String mdn) {
        try {
            callMapLock.lock();

            for (Map.Entry<String, CallInfo> entry : callMap.entrySet()) {
                CallInfo callInfo = entry.getValue();
                if (callInfo != null) {
                    if (callInfo.getToNo().equals(mdn)) {
                        return callInfo;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("{} Fail to get the call info by to mdn.", LogFormatter.getCallLogHeader(null, null, null, mdn), e);
            return null;
        } finally {
            callMapLock.unlock();
        }

        return null;
    }

    public void addSdpUnitIntoCallInfo(String callId, SdpUnit sdpUnit) {
        if (callId == null || sdpUnit == null) { return; }

        try {
            callMapLock.lock();

            CallInfo callInfo = getCallInfo(callId);
            if (callInfo != null) {
                callInfo.setSdpUnit(sdpUnit);
            }
        } catch (Exception e) {
            logger.warn("{} Fail to set the sdp unit into the call info. (sdpUnit={})", LogFormatter.getCallLogHeader(null, callId, null, null), sdpUnit, e);
        } finally {
            callMapLock.unlock();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

}
